package com.kolibree.account

import com.google.gson.JsonObject
import com.kolibree.account.eraser.UserSessionManager
import com.kolibree.account.logout.AccountDoesNotExist
import com.kolibree.account.logout.ForceLogoutReason
import com.kolibree.account.logout.RefreshTokenFailed
import com.kolibree.account.logout.ShouldLogoutUseCase
import com.kolibree.account.phone.PhoneNumberLink
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.android.utils.PhoneNumberChecker
import com.kolibree.sdkws.account.AccountManager
import com.kolibree.sdkws.account.models.PrivateAccessToken
import com.kolibree.sdkws.api.response.VerificationTokenResponse
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.core.OnUserLoggedInCallback
import com.kolibree.sdkws.core.SynchronizationScheduler
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class AccountFacadeImplTest {

    private lateinit var facade: AccountFacadeImpl

    private val accountDatastore = mock<AccountDatastore>()
    private val accountManager = mock<AccountManager>()
    private val kolibreeConnector = mock<InternalKolibreeConnector>()
    private val onUserLoggedInCallback = mock<OnUserLoggedInCallback>()
    private val shouldLogoutUseCase = mock<ShouldLogoutUseCase>()
    private val synchronizationScheduler = mock<SynchronizationScheduler>()
    private val userSessionManager = mock<UserSessionManager>()
    private val phoneNumberVerifier = mock<PhoneNumberChecker>()

    @Before
    fun setUp() {
        facade = spy(
            AccountFacadeImpl(
                kolibreeConnector,
                accountDatastore,
                accountManager,
                phoneNumberVerifier,
                onUserLoggedInCallback,
                shouldLogoutUseCase,
                synchronizationScheduler,
                userSessionManager
            )
        )
    }

    /*
    shouldLogout
     */

    @Test
    fun `shouldLogout returns emits the first item from shouldLogoutStream`() {
        val subject = PublishSubject.create<ForceLogoutReason>()
        whenever(shouldLogoutUseCase.shouldLogoutStream).thenReturn(subject)

        val observer = facade.shouldLogout().test().assertEmpty()

        val expectedItem = AccountDoesNotExist
        subject.onNext(expectedItem)
        subject.onNext(RefreshTokenFailed)

        observer.assertValueCount(1).assertValue(expectedItem).assertComplete()
    }

    /*
    DELETE ACCOUNT
     */

    @Test
    fun `deleteAccount invokes connector deleteAccount`() {
        whenever(kolibreeConnector.deleteAccount()).thenReturn(Completable.complete())

        facade.deleteAccount().test()

        verify(kolibreeConnector).deleteAccount()
    }

    /*
    checkPhoneNumber
     */

    @Test
    fun `checkPhoneNumber completes when the PhoneNumberVerifier validates the number`() {
        whenever(phoneNumberVerifier.isValid(any())).thenReturn(true)
        facade.checkPhoneNumber("+33611223344").test().assertComplete()
    }

    @Test
    fun `checkPhoneNumber emits IllegalArgumentException when the phone number is invalid`() {
        whenever(phoneNumberVerifier.isValid(any())).thenReturn(false)
        facade
            .checkPhoneNumber("0611223344")
            .test()
            .assertError(IllegalArgumentException::class.java)
    }

    /*
    persistPhoneNumber
     */

    @Test
    fun `persistPhoneNumber updates account then calls data store's updatePhoneNumber`() {
        val phoneNumber = "+3366778899"
        val account = mock<AccountInternal>()
        facade.persistPhoneNumber(account, phoneNumber).test().assertComplete()
        verify(account).phoneNumber = phoneNumber
        verify(accountDatastore).updatePhoneNumber(account)
    }

    /*
    verifyPhoneNumber
     */

    @Test
    fun `verifyPhoneNumber calls sendSmsCode and emits good data`() {
        val phoneNumber = "+33622778899"
        val verificationToken = "Hola Bonjour Dzien Dobry"
        whenever(phoneNumberVerifier.isValid(phoneNumber)).thenReturn(true)
        whenever(accountManager.sendSmsCode(phoneNumber))
            .thenReturn(Single.just(VerificationTokenResponse(verificationToken)))

        val expectedValue = PhoneNumberLink(verificationToken, phoneNumber)
        facade.verifyPhoneNumber(phoneNumber).test().assertValue(expectedValue)
        verify(accountManager).sendSmsCode(phoneNumber)
    }

    @Test
    fun `verifyPhoneNumber emits IllegalArgumentException if phone number is not valid`() {
        val phoneNumber = "+33622778899"
        whenever(phoneNumberVerifier.isValid(phoneNumber)).thenReturn(false)

        val sendSmsCodeSubject = SingleSubject.create<VerificationTokenResponse>()
        whenever(accountManager.sendSmsCode(phoneNumber))
            .thenReturn(sendSmsCodeSubject)

        facade.verifyPhoneNumber(phoneNumber).test()
            .assertError(IllegalArgumentException::class.java)

        assertFalse(sendSmsCodeSubject.hasObservers())
    }

    /*
    unlinkPhoneNumber
     */

    @Test
    fun `unlinkPhoneNumber with no account emits NoAccountException`() {
        prepareNoAccountSingle()

        facade.unlinkPhoneNumber().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `unlinkPhoneNumber with account calls AccountManager removePhoneNumber with account ID then updates database`() {
        val currentAccount = spy(createAccountInternal(profiles = listOf(profileInternal)))
        prepareGetAccountSingle(accountInternal = currentAccount)

        val subject = CompletableSubject.create()
        whenever(accountManager.removePhoneNumber(currentAccount.id))
            .thenReturn(subject)

        val observer = facade.unlinkPhoneNumber().test().assertNotComplete()

        assertTrue(subject.hasObservers())

        verify(currentAccount, never()).phoneNumber = null
        verify(accountDatastore, never()).updatePhoneNumber(any())

        subject.onComplete()

        observer.assertComplete()

        verify(currentAccount).phoneNumber = null
        verify(accountDatastore).updatePhoneNumber(any())
    }

    /*
    linkPhoneNumber
     */

    @Test
    fun `linkPhoneNumber with no account emits NoAccountException`() {
        prepareNoAccountSingle()

        facade.linkPhoneNumber(mock(), 0).test()
            .assertError(NoAccountException)
    }

    @Test
    fun `linkPhoneNumber with account calls AccountManager setPhoneNumber with account ID then updates database`() {
        val verificationToken = "TOKEN"

        val currentAccount = spy(createAccountInternal(profiles = listOf(profileInternal)))
        prepareGetAccountSingle(accountInternal = currentAccount)

        val phoneNumber = "1234"
        val setPhoneNumberSubject = CompletableSubject.create()
        whenever(accountManager.setPhoneNumber(eq(currentAccount.id), any()))
            .thenReturn(setPhoneNumberSubject)
        val link = PhoneNumberLink(verificationToken, phoneNumber)

        val observer = facade.linkPhoneNumber(link, 0)
            .test()
            .assertNotComplete()

        assertTrue(setPhoneNumberSubject.hasObservers())

        verify(currentAccount, never()).phoneNumber = phoneNumber
        verify(accountDatastore, never()).updatePhoneNumber(any())

        setPhoneNumberSubject.onComplete()
        observer.assertComplete()

        verify(currentAccount).phoneNumber = phoneNumber
        verify(accountDatastore).updatePhoneNumber(any())
    }

    /*
    persistWeChatData
     */

    @Test
    fun `persistWeChatData calls AccountDataStore's updateWeChatData method`() {
        val account = mock<AccountInternal>()
        facade.persistWeChatData(account).test().assertComplete()
        verify(accountDatastore).updateWeChatData(account)
    }

    /*
    nullifyWeChatData
     */

    @Test
    fun `nullifyWeChatData nullifies all WeChat related properties`() {
        val account = mock<AccountInternal>()
        facade.nullifyWeChatData(account).test().assertComplete()
        verify(account).wcOpenId = null
        verify(account).wcUnionId = null
        verify(account).wcAccessToken = null
        verify(account).wcRefreshToken = null
        verify(account).wcExpiresIn = null
        verify(account).wcScope = null
    }

    /*
    linkWeChat
     */

    @Test
    fun `linkWeChat with no account emits NoAccountException`() {
        prepareNoAccountSingle()

        facade.linkWeChat("").test().assertError(NoAccountException)
    }

    @Test
    fun `linkWeChat calls manager's setWeChat method then persists backend-served properties`() {
        val code = "WeChat code lalala"
        val currentAccount = prepareGetAccountSingle()
        val currentAccountId = currentAccount.id

        val backendAccount = mock<AccountInternal>()
        whenever(backendAccount.id).thenReturn(currentAccountId)
        whenever(accountManager.setWeChat(currentAccountId, code))
            .thenReturn(Single.just(backendAccount))

        facade.linkWeChat(code).test().assertComplete()

        verify(accountManager).setWeChat(currentAccountId, code)
        verify(facade).persistWeChatData(backendAccount)
    }

    /*
    attemptLoginWithWechat
     */

    @Test
    fun `attemptLoginWithWechat stores account on success`() {
        val code = "dada"
        val expectedAccount = createAccountInternal()
        whenever(accountManager.internalAttemptLoginWithWechat(code)).thenReturn(
            Single.just(
                expectedAccount
            )
        )

        facade.attemptLoginWithWechat(code).test().assertValueCount(1)

        verify(accountDatastore).setAccount(expectedAccount)
    }

    @Test
    fun `attemptLoginWithWechat returns expected Account on success`() {
        val code = "dada"
        val currentAccountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        whenever(accountManager.internalAttemptLoginWithWechat(code)).thenReturn(
            Single.just(
                currentAccountInternal
            )
        )

        val expectedWeChatData = WeChatData(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        val expectedAccount = Account(
            ownerProfileId = currentAccountInternal.ownerProfileId,
            phoneNumber = currentAccountInternal.phoneNumber,
            email = currentAccountInternal.email,
            backendId = currentAccountInternal.id,
            pubId = currentAccountInternal.pubId,
            weChatData = expectedWeChatData,
            profiles = currentAccountInternal.internalProfiles.map { it.exportProfile() }
        )

        facade.attemptLoginWithWechat(code).test().assertValue(expectedAccount)
    }

    @Test
    fun `attemptLoginWithWechat invokes onSuccessfulWechatLogin on success`() {
        val code = "dada"
        val currentAccountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        whenever(accountManager.internalAttemptLoginWithWechat(code)).thenReturn(
            Single.just(
                currentAccountInternal
            )
        )

        facade.attemptLoginWithWechat(code).test()

        verify(facade).onSuccessfulWechatLogin()
    }

    @Test
    fun `attemptLoginWithWechat doesn't store account on error`() {
        val code = "dada"
        val expectedError = WeChatAccountNotRecognizedException(code)
        whenever(accountManager.internalAttemptLoginWithWechat(code)).thenReturn(
            Single.error(
                expectedError
            )
        )

        facade.attemptLoginWithWechat(code).test().assertError(expectedError)

        verify(accountDatastore, never()).setAccount(any())
    }

    @Test
    fun `attemptLoginWithWechat doesn't invoke initWatch on error`() {
        val code = "dada"
        val expectedError = WeChatAccountNotRecognizedException(code)
        whenever(accountManager.internalAttemptLoginWithWechat(code)).thenReturn(
            Single.error(
                expectedError
            )
        )

        facade.attemptLoginWithWechat(code).test().assertError(expectedError)

        verify(facade, never()).onSuccessfulWechatLogin()
    }

    /*
    onSuccessfulWechatLogin
     */

    @Test
    fun `onSuccessfulWechatLogin invokes initWatch`() {
        facade.onSuccessfulWechatLogin()

        verify(onUserLoggedInCallback).onUserLoggedIn()
    }

    @Test
    fun `onSuccessfulWechatLogin invokes syncNow`() {
        facade.onSuccessfulWechatLogin()

        verify(synchronizationScheduler).syncNow()
    }

    /*
    loginWithWechat
     */

    @Test
    fun `loginWithWechat invokes onSuccessfulWeChatLogin on success`() {
        val code = "dada"
        val currentAccountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        whenever(accountManager.legacyLoginWithWechat(code)).thenReturn(
            Single.just(
                currentAccountInternal
            )
        )

        facade.loginWithWechat(code).test()

        verify(facade).onSuccessfulWechatLogin()
    }

    @Test
    fun `loginWithWechat doesn't invoke onSuccessfulWechatLogin on error`() {
        whenever(accountManager.legacyLoginWithWechat(any())).thenReturn(
            Single.error(
                TestForcedException()
            )
        )

        facade.loginWithWechat("da").test()

        verify(facade, never()).onSuccessfulWechatLogin()
    }

    /*
    logout
     */

    @Test
    fun `logout invokes connector logout`() {
        whenever(kolibreeConnector.logout()).thenReturn(Completable.complete())

        facade.logout().test()

        verify(kolibreeConnector).logout()
    }

    @Test
    fun `logout invokes userSessionManager reset after kolibreeConnector logout`() {
        val subject = CompletableSubject.create()
        whenever(kolibreeConnector.logout()).thenReturn(subject)

        facade.logout().test()

        verify(userSessionManager, never()).reset()

        subject.onComplete()

        verify(userSessionManager).reset()
    }

    @Test
    fun `deleteAccount invokes userSessionManager reset after kolibreeConnector deleteAccount`() {
        val subject = CompletableSubject.create()
        whenever(kolibreeConnector.deleteAccount()).thenReturn(subject)

        facade.deleteAccount().test()

        verify(userSessionManager, never()).reset()

        subject.onComplete()

        verify(userSessionManager).reset()
    }

    /*
    getPrivateAccessToken
     */
    @Test
    fun `getPrivateAccessToken emits NoAccountException if if there's no logged in account`() {
        prepareNoAccountSingle()

        facade.getPrivateAccessToken().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getPrivateAccessToken returns privateAccessToken from accountManager`() {
        val account = prepareGetAccountSingle()

        val expectedToken = PrivateAccessToken(accessToken = "da", expiryDate = "la")
        whenever(accountManager.getPrivateAccessToken(account.id))
            .thenReturn(Single.just(expectedToken))

        facade.getPrivateAccessToken().test()
            .assertValue(expectedToken)
    }

    /*
    getAccountSingle
     */

    @Test
    fun `getAccountSingle emits NoAccountException if accountDatastore emits NoSuchElementException`() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.error(NoSuchElementException()))

        facade.getAccountSingle().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getAccountSingle emits NoAccountException if accountDatastore emits completed maybe`() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        facade.getAccountSingle().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getAccountSingle emits AccountInternal mapped to Account if accountDatastore emits an AccountInternal`() {
        val accountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(accountInternal))

        val expectedAccount = accountInternal.toAccount()

        facade.getAccountSingle().test()
            .assertValue(expectedAccount)
    }

    /*
    getAccountStream
     */
    @Test
    fun `getAccountStream emits AccountInternal mapped to Account when there is a change`() {
        val accountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )
        val accountInternalUpdated = accountInternal.copy(id = 911)

        whenever(accountDatastore.accountFlowable()).thenReturn(
            Flowable.fromIterable(
                listOf(
                    accountInternal,
                    accountInternalUpdated
                )
            )
        )

        val expectedAccount = accountInternal.toAccount()
        val expectedUpdatedAccount = accountInternalUpdated.toAccount()

        facade.getAccountStream().test()
            .assertValues(expectedAccount, expectedUpdatedAccount)
    }

    /*
    getAccount
     */

    @Test
    fun `getAccount returns null if there's no AccountInternal`() {
        whenever(facade.currentAccount()).thenReturn(null)

        assertNull(facade.getAccount())
    }

    @Test
    fun `getAccount creates Account from AccountInternal`() {
        val currentAccountInternal = createAccountInternal(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        val expectedWeChatData = WeChatData(
            openId = WECHAT_OPEN_ID,
            unionId = WECHAT_UNION_ID,
            accessToken = WECHAT_ACCESS_TOKEN,
            refreshToken = WECHAT_REFRESH_TOKEN,
            expiresIn = WECHAT_EXPIRES_IN,
            scope = WECHAT_SCOPE
        )

        val expectedAccount = Account(
            ownerProfileId = currentAccountInternal.ownerProfileId,
            phoneNumber = currentAccountInternal.phoneNumber,
            email = currentAccountInternal.email,
            backendId = currentAccountInternal.id,
            pubId = currentAccountInternal.pubId,
            weChatData = expectedWeChatData,
            profiles = currentAccountInternal.internalProfiles.map { it.exportProfile() }
        )

        whenever(facade.currentAccount()).thenReturn(currentAccountInternal)

        assertEquals(expectedAccount, facade.getAccount())
    }

    /*
    unlinkWeChat
     */

    @Test
    fun `unlinkWeChat with no account emits NoAccountException`() {
        prepareNoAccountSingle()

        facade.unlinkWeChat().test().assertError(NoAccountException)
    }

    @Test
    fun `unlinkWeChat calls manager's removeWeChat method then persists nullified properties`() {
        val currentAccount = prepareGetAccountSingle()
        val currentAccountId = currentAccount.id

        whenever(accountManager.removeWeChat(currentAccountId)).thenReturn(Completable.complete())

        facade.unlinkWeChat().test().assertComplete()

        verify(accountManager).removeWeChat(currentAccountId)
        verify(facade).nullifyWeChatData(currentAccount)
        verify(facade).persistWeChatData(currentAccount)
    }

/*
internalRegisterWechat
*/

    @Test
    fun `internalRegisterWechat uses Gender's serializedName`() {
        whenever(accountManager.registerWithWechat(any()))
            .thenReturn(Single.just(createAccountInternal()))

        val expectedGender = Gender.FEMALE
        val body = mock<JsonObject>()
        val profile = ProfileBuilder
            .create()
            .withGender(expectedGender)
            .withBirthday(LocalDate.now())
            .build()

        facade
            .internalRegisterWechat("", profile, body)
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(body).addProperty("gender", expectedGender.serializedName)
    }

    /*
    Utils
     */

    private fun prepareNoAccountSingle() {
        doReturn(Single.error<AccountInternal>(NoAccountException))
            .whenever(facade)
            .getAccountInternalSingle()
    }

    private fun prepareGetAccountSingle(
        profiles: List<ProfileInternal> = listOf(profileInternal),
        accountInternal: AccountInternal = createAccountInternal(profiles = profiles)
    ): AccountInternal {

        doReturn(Single.just(accountInternal))
            .whenever(facade)
            .getAccountInternalSingle()

        return accountInternal
    }

    companion object {

        private const val WECHAT_OPEN_ID = "WECHAT_OPEN_ID"
        private const val WECHAT_UNION_ID = "WECHAT_UNION_ID"
        private const val WECHAT_ACCESS_TOKEN = "WECHAT_ACCESS_TOKEN"
        private const val WECHAT_REFRESH_TOKEN = "WECHAT_REFRESH_TOKEN"
        private const val WECHAT_EXPIRES_IN = 134
        private const val WECHAT_SCOPE = "wcScope"
    }

    private val profileInternal = ProfileInternal(
        id = ProfileBuilder.DEFAULT_ID,
        firstName = ProfileBuilder.DEFAULT_NAME,
        addressCountry = addressCountry,
        gender = gender,
        points = 0,
        accountId = 42,
        handedness = handedness,
        pictureUrl = picturePath,
        creationDate = createdDate,
        brushingTime = brushingGoalTime,
        age = age,
        birthday = birthday,
        isOwnerProfile = true
    )
}
