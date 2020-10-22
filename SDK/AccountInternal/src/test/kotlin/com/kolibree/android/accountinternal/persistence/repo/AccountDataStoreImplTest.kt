/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.repo

import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.dao.AccountDao
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DATETIME_PATTERN
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import java.util.Collections
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

/** [AccountDatastoreImpl] tests */
class AccountDataStoreImplTest : BaseUnitTest() {

    private val accountDao = mock<AccountDao>()

    private val profileDatastore = mock<ProfileDatastore>()

    private lateinit var dataStore: AccountDatastoreImpl

    override fun setup() {
        super.setup()

        dataStore = spy(
            AccountDatastoreImpl(
                accountDao,
                profileDatastore
            )
        )
    }

    /*
    GET ACCOUNT MAYBE
     */

    @Test
    fun getAccountMaybe_returnsAccountDaoGetAccountMaybeIncludingProfiles() {
        val profileList = Collections.singletonList(mock<ProfileInternal>())
        whenever(profileDatastore.getProfiles()).thenReturn(Single.just(profileList))

        val mockedAccount = mock<AccountInternal>()
        whenever(accountDao.getAccountMaybe()).thenReturn(Maybe.just(mockedAccount))

        dataStore.getAccountMaybe().test()

        verify(accountDao).getAccountMaybe()

        verify(mockedAccount).internalProfiles = profileList
    }

    /*
     ACCOUNT FLOWABLE
     */
    @Test
    fun accountFlowable_emptyAccountTable_emptyProfiles_emitsNothing() {
        whenever(accountDao.getAccountFlowable()).thenReturn(PublishProcessor.create<AccountInternal>())
        whenever(profileDatastore.profilesFlowable()).thenReturn(PublishProcessor.create<List<ProfileInternal>>())

        dataStore.accountFlowable().test().assertEmpty()
    }

    @Test
    fun accountFlowable_emptyAccountTable_withProfiles_emitsNothing() {
        whenever(accountDao.getAccountFlowable()).thenReturn(PublishProcessor.create<AccountInternal>())
        whenever(profileDatastore.profilesFlowable())
            .thenReturn(BehaviorProcessor.createDefault(listOf()))

        dataStore.accountFlowable().test().assertEmpty()
    }

    @Test
    fun accountFlowable_withAccountInTable_emptyProfiles_emitsNothing() {
        val mockedAccount = mock<AccountInternal>()
        whenever(accountDao.getAccountFlowable())
            .thenReturn(BehaviorProcessor.createDefault(mockedAccount))
        whenever(profileDatastore.profilesFlowable()).thenReturn(PublishProcessor.create<List<ProfileInternal>>())

        dataStore.accountFlowable().test().assertEmpty()
    }

    @Test
    fun accountFlowable_withAccountInTable_withProfiles_emitsAccountIncludingProfiles() {
        val accountProcessor = PublishProcessor.create<AccountInternal>()
        val profileProcessor = PublishProcessor.create<List<ProfileInternal>>()
        whenever(accountDao.getAccountFlowable()).thenReturn(accountProcessor)
        whenever(profileDatastore.profilesFlowable()).thenReturn(profileProcessor)

        val observer = dataStore.accountFlowable().test()

        observer.assertEmpty()

        val mockedAccount = mock<AccountInternal>()
        accountProcessor.onNext(mockedAccount)

        // waiting for profileDataStore to emit profiles
        observer.assertEmpty()

        val expectedProfiles = listOf<ProfileInternal>()
        profileProcessor.onNext(expectedProfiles)

        observer.assertValueCount(1)

        verify(mockedAccount).internalProfiles = expectedProfiles
    }

    @Test
    fun accountFlowable_profileDataStoreEmitsNewProfileList_emitsNewAccount() {
        val initialList = listOf<ProfileInternal>()
        val mockedAccount = mock<AccountInternal>()

        val accountProcessor = BehaviorProcessor.createDefault(mockedAccount)
        val profileProcessor = BehaviorProcessor.createDefault(initialList)
        whenever(accountDao.getAccountFlowable()).thenReturn(accountProcessor)
        whenever(profileDatastore.profilesFlowable()).thenReturn(profileProcessor)

        val observer = dataStore.accountFlowable().test()

        observer.assertValue(mockedAccount)

        verify(mockedAccount).internalProfiles = initialList

        val newList = listOf<ProfileInternal>(mock())

        profileProcessor.onNext(newList)

        verify(mockedAccount).internalProfiles = newList
    }

    @Test
    fun accountFlowable_accountDaoEmitsNewAccount_emitsNewAccount() {
        val profileList = listOf<ProfileInternal>()
        val initialAccount = mock<AccountInternal>()

        val accountProcessor = BehaviorProcessor.createDefault(initialAccount)
        val profileProcessor = BehaviorProcessor.createDefault(profileList)
        whenever(accountDao.getAccountFlowable()).thenReturn(accountProcessor)
        whenever(profileDatastore.profilesFlowable()).thenReturn(profileProcessor)

        val observer = dataStore.accountFlowable().test()

        observer.assertValue(initialAccount)

        val newAccount = mock<AccountInternal>()

        accountProcessor.onNext(newAccount)

        observer.assertValues(initialAccount, newAccount)
    }

    /*
     TRUNCATE
     */

    @Test
    fun truncate_invokesAccountDaoTruncateAndDeleteAllProfilesInOrder() {
        val inOrder = inOrder(accountDao, profileDatastore)

        whenever(profileDatastore.deleteAll()).thenReturn(Completable.complete())

        dataStore.truncate()
        inOrder.verify(accountDao).truncate()
        inOrder.verify(profileDatastore).deleteAll()
    }

    /*
     SET ACCOUNT
     */

    @Test
    fun setAccount_previousAccountNull_insertsProfilesAndInsertsAccountInCorrectOrder() {
        val profileList = Collections.emptyList<ProfileInternal>()
        val newAccount = mock<AccountInternal>()
        whenever(newAccount.internalProfiles).thenReturn(profileList)

        doReturn(Maybe.empty<AccountInternal>()).whenever(dataStore).getAccountMaybe()

        val inOrder = inOrder(dataStore, accountDao, profileDatastore)
        dataStore.setAccount(newAccount)
        verify(dataStore, never()).truncate()
        inOrder.verify(profileDatastore).addProfiles(profileList)
        inOrder.verify(accountDao).insert(newAccount)
    }

    @Test
    fun setAccount_previousAccountEquals_doesNothing() {
        val profileList = Collections.emptyList<ProfileInternal>()
        val newAccount = mock<AccountInternal>()
        val accountId = 5L
        whenever(newAccount.internalProfiles).thenReturn(profileList)
        whenever(newAccount.id).thenReturn(accountId)

        doReturn(Maybe.just(newAccount)).whenever(dataStore).getAccountMaybe()

        val inOrder = inOrder(dataStore, accountDao, profileDatastore)
        dataStore.setAccount(newAccount)
        verify(dataStore, never()).truncate()
        inOrder.verify(profileDatastore, never()).addProfiles(profileList)
        inOrder.verify(accountDao, never()).insert(newAccount)
    }

    @Test
    fun setAccount_previousAccountNotEquals_sameId_doesNotTruncate_insertsProfilesAndAccount() {
        val profileList = Collections.emptyList<ProfileInternal>()
        val newAccount = mock<AccountInternal>()
        val accountId = 5L
        whenever(newAccount.internalProfiles).thenReturn(profileList)
        whenever(newAccount.id).thenReturn(accountId)

        val storedAccount = mock<AccountInternal>()
        whenever(storedAccount.id).thenReturn(accountId)
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        val inOrder = inOrder(dataStore, accountDao, profileDatastore)
        dataStore.setAccount(newAccount)
        verify(dataStore, never()).truncate()
        inOrder.verify(profileDatastore).addProfiles(profileList)
        inOrder.verify(accountDao).insert(newAccount)
    }

    @Test
    fun setAccount_previousAccountNotEquals_differentId_truncates_insertsProfilesAndAccount() {
        val profileList = Collections.emptyList<ProfileInternal>()
        val newAccount = mock<AccountInternal>()
        val accountId = 5L
        whenever(newAccount.internalProfiles).thenReturn(profileList)
        whenever(newAccount.id).thenReturn(accountId)

        val storedAccount = mock<AccountInternal>()
        whenever(storedAccount.id).thenReturn(6L)
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        doNothing().whenever(dataStore).truncate()

        val inOrder = inOrder(dataStore, accountDao, profileDatastore)
        dataStore.setAccount(newAccount)
        verify(dataStore).truncate()
        inOrder.verify(profileDatastore).addProfiles(profileList)
        inOrder.verify(accountDao).insert(newAccount)
    }

    @Test
    fun `setAccount sets owner as current profile if currentProfileId is null and stored profile is null`() {
        val newAccount =
            createAccountInternal()
        newAccount.currentProfileId = null

        val storedAccount =
            createAccountInternal()
        storedAccount.currentProfileId = null
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        dataStore.setAccount(newAccount)

        argumentCaptor<AccountInternal> {
            verify(accountDao).insert(capture())

            val insertedAccount = firstValue

            assertEquals(DEFAULT_OWNER_ID, insertedAccount.currentProfileId)
        }
    }

    @Test
    fun `setAccount sets owner as current profile if currentProfileId is null and stored account referrred to a different user`() {
        val newAccount =
            createAccountInternal()
        newAccount.currentProfileId = null

        val oldAccountId = DEFAULT_TEST_ACCOUNT_ID - 1
        val oldProfileId = DEFAULT_OWNER_ID - 1
        val storedAccount =
            createAccountInternal(
                accountId = oldAccountId,
                ownerId = oldProfileId
            )
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        doNothing().whenever(dataStore).truncate()

        dataStore.setAccount(newAccount)

        argumentCaptor<AccountInternal> {
            verify(accountDao).insert(capture())

            val insertedAccount = firstValue

            assertEquals(DEFAULT_OWNER_ID, insertedAccount.currentProfileId)
        }
    }

    @Test
    fun `setAccount sets stored currentProfileId as current profile if newAccount currentProfileId is null`() {
        val newAccount =
            createAccountInternal()
        newAccount.currentProfileId = null

        val expectedProfileId = 1983L
        val storedAccount =
            createAccountInternal(
                ownerId = expectedProfileId
            )
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        dataStore.setAccount(newAccount)

        argumentCaptor<AccountInternal> {
            verify(accountDao).insert(capture())

            val insertedAccount = firstValue

            assertEquals(expectedProfileId, insertedAccount.currentProfileId)
        }
    }

    @Test
    fun `setAccount uses newAccount currentProfileId if it's not null, even if storedAccount's is nuot null`() {
        val expectedProfileId = 1983L
        val newAccount =
            createAccountInternal(
                ownerId = expectedProfileId
            )

        val storedProfileId = 661L
        val storedAccount =
            createAccountInternal(
                ownerId = storedProfileId
            )
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        dataStore.setAccount(newAccount)

        argumentCaptor<AccountInternal> {
            verify(accountDao).insert(capture())

            val insertedAccount = firstValue

            assertEquals(expectedProfileId, insertedAccount.currentProfileId)
        }
    }

    @Test
    fun `setAccount does not change the current value of data collecting`() {
        val expectedProfileId = 1983L
        val newAccount =
            createAccountInternal(
                ownerId = expectedProfileId,
                isAllowDataCollecting = true
            )

        val storedProfileId = 661L
        val storedAccount =
            createAccountInternal(
                ownerId = storedProfileId,
                isAllowDataCollecting = false
            )
        doReturn(Maybe.just(storedAccount)).whenever(dataStore).getAccountMaybe()

        dataStore.setAccount(newAccount)

        argumentCaptor<AccountInternal> {
            verify(accountDao).insert(capture())

            val insertedAccount = firstValue

            assertEquals(storedAccount.isAllowDataCollecting, insertedAccount.isAllowDataCollecting)
        }
    }

    /*
     UPDATE CURRENT PROFILE
     */

    @Test
    fun updateCurrentProfileId_callsAccountDaoUpdateCurrentProfileId() {
        val currentProfileId = 1983L
        val account = mock<AccountInternal>()
        whenever(account.currentProfileId).thenReturn(currentProfileId)
        dataStore.updateCurrentProfileId(account)
        verify(accountDao).updateCurrentProfileId(currentProfileId)
    }

    @Test
    fun `updateCurrentProfileId sets owner as current profile if currentProfileId is null`() {
        val newAccount =
            createAccountInternal()
        newAccount.currentProfileId = null

        dataStore.updateCurrentProfileId(newAccount)

        argumentCaptor<Long> {
            verify(accountDao).updateCurrentProfileId(capture())

            assertEquals(DEFAULT_OWNER_ID, firstValue)
        }
    }

    /*
    UpdateTokens
     */

    @Test
    fun updateTokens_callsAccountDaoUpdateTokens() {
        val accessToken = "Hola"
        val refreshToken = "Dzien Dobry"
        val account = mock<AccountInternal>()
        whenever(account.accessToken).thenReturn(accessToken)
        whenever(account.refreshToken).thenReturn(refreshToken)
        dataStore.updateTokens(account)
        verify(accountDao).updateTokens(accessToken, refreshToken)
    }

    @Test
    fun updateEmail_callAccountDaoUpdateEmail() {
        val email = "a@b.c"
        val account = mock<AccountInternal>()
        whenever(account.email).thenReturn(email)
        dataStore.updateEmail(account)
        verify(accountDao).updateEmail(email)
    }

    @Test
    fun updateFacebookId_callAccountDaoUpdateFacebookId() {
        val facebookId = "azerty"
        val account = mock<AccountInternal>()
        whenever(account.facebookId).thenReturn(facebookId)
        dataStore.updateFacebookId(account)
        verify(accountDao).updateFacebookId(facebookId)
    }

    @Test
    fun setUpdateAllowDataCollecting_callAccountDaoUpdateAllowDataCollecting() {
        val allowsRawData = true
        val account = mock<AccountInternal>()
        whenever(account.isAllowDataCollecting).thenReturn(allowsRawData)
        dataStore.setUpdateAllowDataCollecting(account)
        verify(accountDao).updateAllowDataCollecting(allowsRawData)
    }

    @Test
    fun updateAllowDigest_callAccountDaoUpdateAllowDigest() {
        val allowsDigest = true
        val account = mock<AccountInternal>()
        whenever(account.isDigestEnabled).thenReturn(allowsDigest)
        dataStore.updateAllowDigest(account)
        verify(accountDao).updateAllowDigest(allowsDigest)
    }
}

internal const val DEFAULT_TEST_ACCOUNT_ID = 54L
internal const val DEFAULT_OWNER_ID = 7879L
internal const val DEFAULT_ACCOUNT_PUB_ID = "7654321"

internal const val DEFAULT_NAME = "Dummy"
internal const val DEFAULT_ID = 34L
internal val DEFAULT_GENDER = Gender.MALE
internal val DEFAULT_HANDEDNESS = Handedness.LEFT_HANDED
internal val DEFAULT_BIRTHDAY = LocalDate.now().minusYears(20)

internal const val DEFAULT_BRUSHING_TIME = 120
internal val CREATE_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN)

internal fun createAccountInternal(
    accountId: Long = DEFAULT_TEST_ACCOUNT_ID,
    ownerId: Long = DEFAULT_OWNER_ID,
    profiles: List<ProfileInternal> = listOf(
        createProfileInternal(
            accountId = accountId,
            id = ownerId
        )
    ),
    pubId: String = DEFAULT_ACCOUNT_PUB_ID,
    phoneNumber: String? = null,
    openId: String = "",
    unionId: String = "",
    accessToken: String = "",
    refreshToken: String = "",
    expiresIn: Int = 0,
    scope: String = "",
    isAllowDataCollecting: Boolean = true
): AccountInternal {
    val account = AccountInternal(
        id = accountId,
        ownerProfileId = profiles.first().id,
        phoneNumber = phoneNumber,
        pubId = pubId,
        wcOpenId = openId,
        wcUnionId = unionId,
        wcAccessToken = accessToken,
        wcRefreshToken = refreshToken,
        wcExpiresIn = expiresIn,
        wcScope = scope,
        isAllowDataCollecting = isAllowDataCollecting
    )

    account.internalProfiles = profiles
    account.setOwnerProfileAsCurrent()

    return account
}

internal fun createProfileInternal(
    id: Long = DEFAULT_OWNER_ID,
    firstName: String = DEFAULT_NAME,
    isOwnerProfile: Boolean = true,
    gender: String = DEFAULT_GENDER.toString(),
    accountId: Long = DEFAULT_TEST_ACCOUNT_ID,
    brushingTime: Int = DEFAULT_BRUSHING_TIME,
    birthday: LocalDate = DEFAULT_BIRTHDAY,
    creationDate: ZonedDateTime = TrustedClock.getNowZonedDateTime()
) = ProfileInternal(
    id = id,
    firstName = firstName,
    gender = gender,
    accountId = accountId.toInt(),
    birthday = birthday,
    age = getAgeFromBirthDate(birthday),
    brushingTime = brushingTime,
    creationDate = creationDate.format(CREATE_DATE_FORMATTER),
    isOwnerProfile = isOwnerProfile,
    brushingNumber = 0,
    points = 0
)
