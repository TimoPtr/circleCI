/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account

import com.kolibree.account.profile.ProfileDeletedHook
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.DEFAULT_TEST_ACCOUNT_ID
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.sdkws.account.AccountManager
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.threeten.bp.LocalDate
import timber.log.Timber

class ProfileFacadeImplTest : BaseUnitTest() {
    private val connector: IKolibreeConnector = mock()
    private val accountFacade: AccountFacade = mock()
    private val profileManager: ProfileManager = mock()
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase = mock()
    private val accountManager: AccountManager = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private var facade = initWithHooks()

    /*
    picturePath
     */
    @Test
    fun `uploadPicture returns downloadExternalPicture single if pictureUrl starts with http`() {
        val expectedSingle = SingleSubject.create<ProfileInternal>()
        whenever(profileManager.downloadExternalPicture(profileInternal, picturePath))
            .thenReturn(expectedSingle)

        assertEquals(expectedSingle, facade.uploadPicture(profileInternal, picturePath))
    }

    @Test
    fun `uploadPicture returns changeProfilePicture single if pictureUrl does not start with http`() {
        val localFilePath = "file://dada"
        val expectedSingle = SingleSubject.create<ProfileInternal>()
        whenever(profileManager.changeProfilePicture(profileInternal, localFilePath))
            .thenReturn(expectedSingle)

        assertEquals(expectedSingle, facade.uploadPicture(profileInternal, localFilePath))
    }

    /*
    createProfile
     */

    @Test
    fun `createProfile emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.createProfile(profile).test().assertError(NoAccountException)
    }

    @Test
    fun `create profile inits brushingMode for profile and downloads picture if profile has pictureUrl`() {
        facade = spy(facade)

        prepareGetAccountSingle()

        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(profileInternal))

        val uploadPictureSubject = SingleSubject.create<ProfileInternal>()
        doReturn(uploadPictureSubject)
            .whenever(facade)
            .uploadPicture(profileInternal, picturePath)

        Timber.d("Expected createdProfile profile is $profileInternal")
        val initBrushingModeSubject = CompletableSubject.create()
        whenever(synchronizeBrushingModeUseCase.initBrushingModeForProfile(profileInternal.id))
            .thenReturn(initBrushingModeSubject)

        val observer = facade.createProfile(profile).test().assertEmpty()

        assertTrue(initBrushingModeSubject.hasObservers())
        assertFalse(uploadPictureSubject.hasObservers())
        initBrushingModeSubject.onComplete()

        observer.assertEmpty()
        assertTrue(uploadPictureSubject.hasObservers())

        uploadPictureSubject.onSuccess(profileInternal)

        observer.assertComplete()
            .assertNoErrors()
            .assertValue(profile)
    }

    @Test
    fun createProfileWithExternalUrl() {
        prepareGetAccountSingle()

        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(profileInternal))

        facade.createProfile(profile).map {
            verify(profileManager).downloadExternalPicture(
                profileInternal,
                profileInternal.pictureUrl!!
            )
        }.test()
    }

    @Test
    fun createProfileWithLocalUrl() {
        prepareGetAccountSingle()

        val url = "mypath"
        profileInternal.pictureUrl = url

        val profile = profileInternal.exportProfile()

        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(profileInternal))

        facade.createProfile(profile).map {
            verify(profileManager).changeProfilePicture(
                profileInternal,
                profileInternal.pictureUrl!!
            )
        }.test()

        argumentCaptor<CreateProfileData> {
            verify(profileManager).createProfile(capture(), any())

            assertEquals(profile.firstName, firstValue.firstName)
            assertEquals(profile.gender.serializedName, firstValue.gender)
            assertEquals(profile.handedness.serializedName, firstValue.handedness)
        }

        verify(profileManager).changeProfilePicture(any(), eq(url))
    }

    @Test
    fun `createProfile emits created profile even if profileManager createProfile returns profile with null pictureUrl`() {
        prepareGetAccountSingle()

        val profile = profileInternal.copy(pictureUrl = null).exportProfile()

        val createdProfile = profileInternal
        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(createdProfile))

        whenever(synchronizeBrushingModeUseCase.initBrushingModeForProfile(profileInternal.id))
            .thenReturn(Completable.complete())

        val expectedProfile = createdProfile.exportProfile()
        facade.createProfile(profile)
            .test()
            .assertValue(expectedProfile)
    }

    @Test
    fun `createProfile emits created profile even if profileManager createProfile returns profile with empty pictureUrl`() {
        prepareGetAccountSingle()

        val profile = profileInternal.copy(pictureUrl = "").exportProfile()

        val createdProfile = profileInternal
        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(createdProfile))

        whenever(synchronizeBrushingModeUseCase.initBrushingModeForProfile(profileInternal.id))
            .thenReturn(Completable.complete())

        val expectedProfile = createdProfile.exportProfile()
        facade.createProfile(profile)
            .test()
            .assertValue(expectedProfile)
    }

    @Test
    fun `createProfile doesn't use the deprecated setGender method`() {
        prepareGetAccountSingle()

        whenever(profileManager.createProfile(any(), any()))
            .thenReturn(Single.just(profileInternal))
        whenever(profileManager.downloadExternalPicture(any(), any()))
            .thenReturn(Single.just(profileInternal))
        whenever(synchronizeBrushingModeUseCase.initBrushingModeForProfile(any()))
            .thenReturn(Completable.complete())

        val creatingProfile = ProfileBuilder
            .create()
            .withGender(Gender.UNKNOWN)
            .withBirthday(LocalDate.now())
            .build()

        facade
            .createProfile(creatingProfile)
            .test()
            .assertNoErrors()
            .assertComplete()

        argumentCaptor<CreateProfileData> {
            verify(profileManager).createProfile(capture(), any())
            assertEquals(Gender.UNKNOWN.serializedName, lastValue.gender)
        }
    }

    /*
    deleteProfile
     */

    @Test
    fun `deleteProfile emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.deleteProfile(profileInternal.id).test()
            .assertError(NoAccountException)
    }

    @Test
    fun `deleteProfile invokes profile manager's deleteProfile and emits its result`() {
        val expectedResult = true
        prepareGetAccountSingle()

        whenever(connector.setActiveProfileCompletable(any()))
            .thenReturn(Completable.complete())

        whenever(profileManager.deleteProfile(any(), any()))
            .thenReturn(Single.just(expectedResult))

        doReturn(Completable.complete())
            .whenever(facade)
            .onProfileDeletedHooks(any())

        facade.deleteProfile(profileInternal.id)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(expectedResult)
    }

    @Test
    fun `deleteProfile runs onProfileDeletedHooks when deleteProfile emits true`() {
        val expectedProfileId = 1986L
        prepareGetAccountSingle()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(ProfileBuilder.create().withId(expectedProfileId).build()))
        whenever(profileManager.deleteProfile(any(), any())).thenReturn(Single.just(true))

        val hookSubject = CompletableSubject.create()
        doReturn(hookSubject)
            .whenever(facade)
            .onProfileDeletedHooks(expectedProfileId)

        val observer = facade.deleteProfile(expectedProfileId)
            .test()
            .assertNotComplete()
            .assertNoErrors()

        assertTrue(hookSubject.hasObservers())

        hookSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `deleteProfile never invokes onProfileDeletedHooks when deleteProfile emits false`() {
        prepareGetAccountSingle()
        whenever(profileManager.deleteProfile(any(), any())).thenReturn(Single.just(false))

        facade.deleteProfile(1983L)
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(facade, never()).onProfileDeletedHooks(any())
    }

    /*
    onProfileDeletedHooks
     */

    @Test
    fun `onProfileDeletedHooks returns Completable from merging hooks`() {
        val profileId = ProfileBuilder.DEFAULT_ID

        val hook1 = TestProfileDeletedHook()
        val hook2 = TestProfileDeletedHook()

        facade = initWithHooks(hooks = setOf(hook1, hook2))

        val observer = facade.onProfileDeletedHooks(profileId).test()
            .assertNotComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook1.completableSubject.onComplete()
        hook2.completableSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `onProfileDeletedHooks runs all hooks, even if one errors`() {
        val profileId = ProfileBuilder.DEFAULT_ID

        val hook1 = TestProfileDeletedHook()
        val hook2 = TestProfileDeletedHook()

        facade = initWithHooks(hooks = setOf(hook1, hook2))

        val observer = facade.onProfileDeletedHooks(profileId).test()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook1.completableSubject.onError(TestForcedException())
        hook2.completableSubject.onComplete()

        observer.assertError(TestForcedException::class.java)

        assertTrue(hook2.completableRun)
        assertFalse(hook1.completableRun)
    }

    /*
    changeProfilePicture
     */

    @Test
    fun `changeProfilePicture emits NoSuchElementException if there's no profile`() {
        prepareNoProfileSingle()

        facade.changeProfilePicture(profile, picturePath).test()
            .assertError(NoSuchElementException::class.java)
    }

    @Test
    fun `changeProfilePicture inserts to local DB a profile with updated pictureUrl before attempting remote update`() {
        prepareGetAccountSingle()

        whenever(profileManager.getProfileInternalLocally(profile.id))
            .thenReturn(Single.just(profileInternal))

        val newPicturePath = "dada"
        val expectedProfile = profileInternal.copy(pictureUrl = newPicturePath)

        val subject = SingleSubject.create<ProfileInternal>()
        whenever(profileManager.updateOrInsertProfileLocally(expectedProfile))
            .thenReturn(subject)

        whenever(profileManager.downloadExternalPicture(any(), any()))
            .thenReturn(Single.just(profileInternal))

        facade.changeProfilePicture(profile, newPicturePath).test()

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `changeProfilePicture returns value from downloadExternalPicture`() {
        prepareGetAccountSingle()

        whenever(profileManager.getProfileInternalLocally(profile.id))
            .thenReturn(Single.just(profileInternal))

        whenever(profileManager.updateOrInsertProfileLocally(profileInternal))
            .thenReturn(Single.just(profileInternal))

        whenever(profileManager.downloadExternalPicture(any(), any()))
            .thenReturn(Single.just(profileInternal))

        facade.changeProfilePicture(profile, picturePath).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.id == profileInternal.id }
    }

    /*
    editProfile
     */

    @Test
    fun `editProfile emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.editProfile(profile).test()
            .assertError(NoAccountException)
    }

    @Test
    fun editProfile() {
        prepareGetAccountSingle()

        whenever(profileManager.editProfile(any(), any())).thenReturn(Single.just(profileInternal))
        facade.editProfile(profile).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue {
                it.id == profileInternal.id &&
                    it.pictureUrl == profileInternal.pictureUrl &&
                    it.pictureLastModifier == profileInternal.pictureLastModifier &&
                    it.country == profileInternal.addressCountry
            }
    }

    @Test
    fun `editProfile doesn't use the deprecated setGender method`() {
        prepareGetAccountSingle()

        whenever(profileManager.editProfile(any(), any()))
            .thenReturn(Single.just(profileInternal.copy(pictureUrl = null)))

        val editedProfile = ProfileBuilder
            .create()
            .withUnknownGender()
            .withBirthday(LocalDate.now())
            .build()

        facade
            .editProfile(editedProfile)
            .test()
            .assertNoErrors()
            .assertComplete()

        argumentCaptor<EditProfileData> {
            verify(profileManager).editProfile(capture(), any())
            assertEquals(Gender.UNKNOWN.serializedName, lastValue.gender)
        }
    }

    /*
    getProfile
     */

    @Test
    fun `getProfile emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.getProfile(1).test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getProfile emits NoSuchElementException if account doesn't contain the profile`() {
        prepareGetAccountSingle(profiles = listOf(profileInternal.copy(id = 2)))

        val nonExistingId = 12312312123
        facade.getProfile(nonExistingId).test()
            .assertError(NoSuchElementException::class.java)
    }

    @Test
    fun `getProfile emits expected profile after updating local storage, if account contains the profileId`() {
        val localProfileId = 12312312123
        val expectedProfile = profileInternal.copy(id = localProfileId)
        prepareGetAccountSingle(profiles = listOf(expectedProfile))

        facade.getProfile(localProfileId).test()
            .assertValue(expectedProfile.exportProfile())
    }

    /*
    getRemoteProfile
     */

    @Test
    fun `getRemoteProfile emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.getRemoteProfile(profileInternal.id).test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getRemoteProfile emits NoSuchElementException if remote account doesn't contain the profile`() {
        val localProfileId = 12312312123
        prepareGetAccountSingle(profiles = listOf(profileInternal.copy(id = localProfileId)))

        whenever(profileManager.updateOrInsertProfileLocally(profileInternal))
            .thenReturn(Single.just(profileInternal))

        val remoteAccount = prepareGetAccountSingle(profiles = listOf(profileInternal))
        assertNull(remoteAccount.getProfileInternalWithId(localProfileId))

        whenever(accountManager.getAccount(any())).thenReturn(Single.just(remoteAccount))
        whenever(profileManager.getProfileLocally(any())).thenReturn(Single.just(profileInternal.exportProfile()))

        facade.getRemoteProfile(localProfileId).test()
            .assertError(NoSuchElementException::class.java)
    }

    @Test
    fun `getRemoteProfile emits expected profile after updating local storage, if remote account contains the profileId`() {
        val accountInternal = prepareGetAccountSingle(profiles = listOf(profileInternal))

        whenever(profileManager.updateOrInsertProfileLocally(profileInternal))
            .thenReturn(Single.just(profileInternal))
        whenever(accountManager.getAccount(any())).thenReturn(Single.just(accountInternal))

        val updateProfileSubject = SingleSubject.create<ProfileInternal>()
        whenever(profileManager.updateOrInsertProfileLocally(any()))
            .thenReturn(updateProfileSubject)

        val observer = facade.getRemoteProfile(profileInternal.id).test().assertEmpty()

        assertTrue(updateProfileSubject.hasObservers())
        updateProfileSubject.onSuccess(profileInternal)

        observer.assertComplete()
            .assertNoErrors()
            .assertValue(profile)
    }

    /*
    getProfilesList
     */

    @Test
    fun `getProfilesList emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.getProfilesList().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getProfilesList emits local profiles`() {
        val expectedProfiles = listOf(profileInternal, profileInternal.copy(firstName = "random"))
        prepareGetAccountSingle(profiles = expectedProfiles)

        facade.getProfilesList().test()
            .assertValue(expectedProfiles.map { it.exportProfile() })
    }

    /*
    getRemoteProfiles
     */

    @Test
    fun `getRemoteProfiles emits NoAccountException if there's no account`() {
        prepareNoAccountSingle()

        facade.getRemoteProfiles().test()
            .assertError(NoAccountException)
    }

    @Test
    fun `getRemoteProfiles emits expected profiles after updating local storage`() {
        val expectedProfiles = listOf(profileInternal, profileInternal.copy(firstName = "random"))
        val accountInternal = prepareGetAccountSingle(profiles = expectedProfiles)

        whenever(profileManager.updateOrInsertProfileLocally(any()))
            .thenAnswer {
                Single.just(it.getArgument(0) as ProfileInternal)
            }
        whenever(accountManager.getAccount(any())).thenReturn(Single.just(accountInternal))

        whenever(profileManager.updateOrInsertProfileLocally(any()))
            .thenAnswer {
                Single.just(it.getArgument(0) as ProfileInternal)
            }

        val observer = facade.getRemoteProfiles().test()
            .assertValue(expectedProfiles.map { it.exportProfile() })
    }

    /*
    activeProfileFlowable
     */
    @Test
    fun `activeProfileFlowable emits currentProfileProvider Flowable items`() {
        val subject = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(subject)

        val observer = facade.activeProfileFlowable().test().assertEmpty()

        val firstExpectedProfile = profileInternal.exportProfile()
        subject.onNext(firstExpectedProfile)

        observer.assertValue(firstExpectedProfile)

        val secondExpectedProfile = mock<Profile>()
        subject.onNext(secondExpectedProfile)

        observer.assertValues(firstExpectedProfile, secondExpectedProfile)
    }

    /*
    getProfilePicture
     */
    @Test
    fun `getProfilePicture calls through profileManager`() {
        whenever(profileManager.getProfilePicture(anyLong(), anyLong()))
            .thenReturn(Single.just(mock()))

        val accountId = 9L
        val profileId = 24L
        facade.getProfilePicture(accountId, profileId)

        verify(profileManager).getProfilePicture(accountId, profileId)
    }

    /*
    Utils
     */

    private fun prepareNoAccountSingle() {
        whenever(accountFacade.getAccountSingle()).thenReturn(Single.error(NoAccountException))
    }

    private fun prepareNoProfileSingle() {
        whenever(profileManager.getProfileInternalLocally(any()))
            .thenReturn(Single.error(NoSuchElementException()))
    }

    private fun prepareGetAccountSingle(profiles: List<ProfileInternal> = listOf(profileInternal)): AccountInternal {
        val accountInternal = createAccountInternal(profiles = profiles)
        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(accountInternal.toAccount()))

        return accountInternal
    }

    private val profileInternal = ProfileInternal(
        id = ProfileBuilder.DEFAULT_ID,
        firstName = ProfileBuilder.DEFAULT_NAME,
        addressCountry = addressCountry,
        gender = gender,
        points = 0,
        accountId = DEFAULT_TEST_ACCOUNT_ID.toInt(),
        handedness = handedness,
        pictureUrl = picturePath,
        pictureLastModifier = pictureLastModifier,
        creationDate = createdDate,
        brushingTime = brushingGoalTime,
        age = age,
        birthday = birthday,
        isOwnerProfile = true
    )

    private val profile = profileInternal.exportProfile() as IProfile

    private fun initWithHooks(hooks: Set<ProfileDeletedHook> = setOf()): ProfileFacadeImpl {
        return spy(
            ProfileFacadeImpl(
                connector,
                accountFacade,
                profileManager,
                synchronizeBrushingModeUseCase,
                accountManager,
                currentProfileProvider,
                hooks
            )
        )
    }
}

internal val birthday = DateConvertersString().getLocalDateFromString("1990-02-04")
internal const val gender = "M"
internal const val handedness = "L"
internal const val age = 28
internal const val picturePath = "https://my_amazing.url"
internal const val pictureLastModifier = "2020-06-10T06:23:26.190095"
internal const val addressCountry = "FR"
internal const val brushingGoalTime: Int = 200
internal const val createdDate: String = "1990-08-08T10:00:20+0000"

private class TestProfileDeletedHook : ProfileDeletedHook {
    var completableRun = false

    val completableSubject = CompletableSubject.create()
    override fun onProfileDeleted(profileId: Long): Completable = completableSubject
        .doOnComplete { completableRun = true }
}
