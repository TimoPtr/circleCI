package com.kolibree.sdkws.profile

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.android.room.DateConvertersString
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.sync.IntegerSyncableField
import com.kolibree.sdkws.core.sync.SyncableField
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.data.model.EditProfileData.UNSET
import com.kolibree.sdkws.internal.OfflineUpdateDatastore
import com.kolibree.sdkws.internal.OfflineUpdateInternal
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock

class ProfileManagerImplTest : BaseUnitTest() {

    @Mock
    internal lateinit var offlineUpdateDatastore: OfflineUpdateDatastore

    @Mock
    lateinit var kolibreeUtils: KolibreeUtils

    @Mock
    lateinit var avatarCache: AvatarCache

    @Mock
    lateinit var fileDownloader: FileDownloader

    @Mock
    internal lateinit var profileRepository: ProfileRepository
    @Mock
    internal lateinit var profileInternalMapper: ProfileInternalMapper

    private lateinit var profileManager: ProfileManagerImpl

    override fun setup() {
        super.setup()

        profileManager = spy(
            ProfileManagerImpl(
                offlineUpdateDatastore,
                kolibreeUtils,
                fileDownloader,
                avatarCache,
                profileRepository,
                profileInternalMapper
            )
        )
    }

    /*
    UPLOAD EXTERNAL PICTURE
     */
    @Test
    fun uploadExternalPicture_getProfileAssociatedToThePicturesNotNull_uploadPictureThrowsException_propagatesError() {
        val pictureUploadUrl = "picture upload url"
        val profileInternal = mockProfileInternal(pictureUploadUrl = pictureUploadUrl)

        val picturePath = "bla"

        doReturn(Single.just(profileInternal)).whenever(profileManager)
            .getProfileAssociatedToThePictures(profileInternal, picturePath)

        val expectedError = Throwable("Test forced error")

        whenever(kolibreeUtils.uploadPicture(eq(pictureUploadUrl), any()))
            .thenAnswer { throw expectedError }

        doNothing().whenever(profileManager).onProfilePictureUpdateFailed(any(), any())

        profileManager.uploadExternalPicture(profileInternal, picturePath).test()
            .assertError(expectedError)
    }

    @Test
    fun uploadExternalPicture_getProfileAssociatedToThePicturesNotNull_uploadPictureThrowsException_invokesOnProfilePictureUpdateFailed() {
        val pictureUploadUrl = "picture upload url"
        val profileId = 99L
        val profileInternal =
            mockProfileInternal(profileId = profileId, pictureUploadUrl = pictureUploadUrl)

        val picturePath = "path"

        doReturn(Single.just(profileInternal)).whenever(profileManager)
            .getProfileAssociatedToThePictures(profileInternal, picturePath)

        val expectedError = Throwable("Test forced error")

        whenever(kolibreeUtils.uploadPicture(eq(pictureUploadUrl), any()))
            .thenAnswer { throw expectedError }

        doNothing().whenever(profileManager).onProfilePictureUpdateFailed(any(), any())

        profileManager.uploadExternalPicture(profileInternal, picturePath).test()
            .assertError(expectedError)

        verify(profileManager).onProfilePictureUpdateFailed(picturePath, profileId)
    }

    /*
    CHANGE PROFILE PICTURE
     */

    @Test
    fun changeProfilePicture_nullOrEmptyPicturePath_returnsFalse() {
        val profile = mockProfileInternal()
        profileManager.changeProfilePicture(profile, null).test().assertValue(profile)
        profileManager.changeProfilePicture(profile, "").test().assertValue(profile)
    }

    @Test
    fun changeProfilePicture_picturePathStartsWithHttp_uploadExternalPictureSucceeds_returnsConfirmPictureUrl() {
        val path = "httppath"

        val profile = mockProfileInternal()
        val expectedSingle = Single.just(true)
        doReturn(expectedSingle).whenever(profileManager).confirmPicture(profile, path, path)

        assertEquals(expectedSingle, profileManager.changeProfilePicture(profile, path))
    }

    @Test
    fun changeProfilePicture_picturePathStartsWithHttpNotKolibree_uploadExternalPictureSucceeds_returnsConfirmPictureUrl() {
        val path = "httppath"

        val profile = mockProfileInternal()
        val expectedSingle = Single.just(profile)

        val mockFile = mock<File>()
        whenever(mockFile.absolutePath).thenReturn(path)

        whenever(fileDownloader.download(anyString())).thenReturn(mockFile)
        doReturn(expectedSingle).whenever(profileManager).confirmPicture(profile, path, path)

        profileManager.downloadExternalPicture(profile, path).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it == profile }
    }

    @Test
    fun changeProfilePicture_picturePathDoesNotStartsWithHttp_uploadExternalPictureReturnsProfileWithEmptyGetPictureUrl_returnsError() {
        val path = "path"

        val profile = mockProfileInternal()
        val uploadExternalProfile = mockProfileInternal()
        doReturn(Single.just(uploadExternalProfile)).whenever(profileManager)
            .uploadExternalPicture(profile, path)

        assertNull(uploadExternalProfile.pictureGetUrl)

        profileManager.changeProfilePicture(profile, path).test().assertError(Throwable::class.java)
    }

    @Test
    fun changeProfilePicture_picturePathDoesNotStartsWithHttp_uploadExternalPictureReturnsProfileWithGetPictureUrl_returnsConfirmPicture() {
        val path = "path"

        val profile = mockProfileInternal()
        val pictureGetUrl = "blabla"
        val uploadExternalProfile = mockProfileInternal(getPictureUrl = pictureGetUrl)
        doReturn(Single.just(uploadExternalProfile)).whenever(profileManager)
            .uploadExternalPicture(profile, path)

        assertNotNull(uploadExternalProfile.pictureGetUrl)

        doReturn(Single.just(uploadExternalProfile)).whenever(profileManager)
            .confirmPicture(uploadExternalProfile, pictureGetUrl, path)

        profileManager.changeProfilePicture(profile, path).test().assertNoErrors()
            .assertValue { profileValue ->
                profileValue.pictureGetUrl == uploadExternalProfile.pictureGetUrl
            }
    }

    /*
    GET PROFILE ASSOCIATED TO THE PICTURES
     */

    @Test
    fun getProfileAssociatedToThePictures_profileHasPictureUploadUrl_returnsProfileWithoutExtraRequests() {
        val pictureUploadUrl = "picture upload url"
        val profileInternal = mockProfileInternal(pictureUploadUrl = pictureUploadUrl)

        profileManager.getProfileAssociatedToThePictures(profileInternal, "ignore")
            .test()
            .assertNoErrors()
            .assertValue(profileInternal)
    }

    @Test
    fun getProfileAssociatedToThePictures_profileDoesNotHavePictureUploadUrl_getPictureUploadUrlNoErrors_returnsProfileRepositoryGetPictureUploadUrl() {
        val accountId = 4
        val profileId = 99L
        val profileInternal = mockProfileInternal(accountId, profileId)

        val path = "path"

        val expectedProfileInternal: ProfileInternal = mock()
        whenever(profileRepository.getPictureUploadUrl(accountId, profileId))
            .thenReturn(Single.just(expectedProfileInternal))

        profileManager.getProfileAssociatedToThePictures(profileInternal, path)
            .test()
            .assertNoErrors()
            .assertValue(expectedProfileInternal)
    }

    @Test
    fun getProfileAssociatedToThePictures_profileDoesNotHavePictureUploadUrl_withGetPictureUploadUrlError_returnsError() {
        val accountId = 4
        val profileId = 99L
        val profileInternal = mockProfileInternal(accountId, profileId)

        val path = "ignore"

        val expectedError = Throwable("Test forced error")
        whenever(profileRepository.getPictureUploadUrl(accountId, profileId))
            .thenReturn(Single.error(expectedError))

        doNothing().whenever(profileManager).onProfilePictureUpdateFailed(any(), any())

        profileManager.getProfileAssociatedToThePictures(profileInternal, path)
            .test()
            .assertError(expectedError)
    }

    @Test
    fun getProfileAssociatedToThePictures_profileDoesNotHavePictureUploadUrl_withGetPictureUploadUrlError_invokesOnProfilePictureUpdateFailed() {
        val accountId = 4
        val profileId = 99L
        val profileInternal = mockProfileInternal(accountId, profileId)

        val path = "path"

        val expectedError = Throwable("Test forced error")
        whenever(profileRepository.getPictureUploadUrl(accountId, profileId))
            .thenReturn(Single.error(expectedError))

        doNothing().whenever(profileManager).onProfilePictureUpdateFailed(any(), any())

        profileManager.getProfileAssociatedToThePictures(profileInternal, path).test()

        verify(profileManager).onProfilePictureUpdateFailed(path, profileId)
    }

    /*
    UPDATE PROFILE
     */
    @Test
    fun `update profile completes even if it receives ApiError from profileRepository`() {
        val accountId = 2L

        val profileInternal = createProfileInternal()
        whenever(profileRepository.getProfileLocally(profile.id)).thenReturn(
            Single.just(
                profileInternal
            )
        )
        whenever(profileRepository.updateProfileLocally(profileInternal.copyProfile(profile))).thenReturn(
            Single.just(
                profileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId,
                profileInternal
            )
        ).thenReturn(
            Single.error(networkError())
        )

        doReturn(true).whenever(profileManager).syncUpdatedFields(any(), any())

        profileManager.updateProfile(
            accountId, profile
        ).test().assertNoErrors().assertComplete()
    }

    @Test
    fun `update profile only updates locally once if it receives ApiError from profileRepository`() {
        val accountId = 2L

        val profileInternal = createProfileInternal()
        whenever(profileRepository.getProfileLocally(profile.id)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        val expectedProfileInternal = profileInternal.copyProfile(profile)
        whenever(profileRepository.updateProfileLocally(expectedProfileInternal)).thenReturn(
            Single.just(
                expectedProfileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId,
                expectedProfileInternal
            )
        ).thenReturn(
            Single.error(networkError())
        )

        doReturn(true).whenever(profileManager).syncUpdatedFields(any(), any())

        profileManager.updateProfile(accountId, profile).test()

        verify(profileRepository).updateProfileLocally(expectedProfileInternal)
    }

    @Test
    fun `update profile updates locally twice if profileRepository remote call succeeds`() {
        val accountId = 2L

        val profileInternal = createProfileInternal()
        whenever(profileRepository.getProfileLocally(profile.id)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        val updatedProfileInternal = createProfileInternal()
        whenever(profileRepository.updateProfileLocally(profileInternal.copyProfile(profile))).thenReturn(
            Single.just(
                updatedProfileInternal
            )
        )

        val remoteProfileInternal = createProfileInternal()
        whenever(
            profileRepository.updateProfile(
                accountId,
                updatedProfileInternal
            )
        ).thenReturn(Single.just(remoteProfileInternal))

        profileManager.updateProfile(accountId, profile).test()

        verify(profileRepository).updateProfileLocally(updatedProfileInternal)
        verify(profileRepository).updateProfileLocally(remoteProfileInternal)
    }

    @Test
    fun `update profile stores profile for offline sync if it receives ApiError from profileRepository`() {
        val accountId = 2L

        val localProfileInternal = createProfileInternal()
        whenever(profileRepository.getProfileLocally(profile.id)).thenReturn(
            Single.just(
                localProfileInternal
            )
        )

        val updatedProfileInternal = localProfileInternal.copyProfile(profile)
        whenever(profileRepository.updateProfileLocally(updatedProfileInternal)).thenReturn(
            Single.just(
                updatedProfileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId,
                updatedProfileInternal
            )
        ).thenReturn(
            Single.error(networkError())
        )

        doReturn(true).whenever(profileManager).syncUpdatedFields(any(), any())

        profileManager.updateProfile(accountId, profile).test()

        argumentCaptor<ProfileSyncableFields> {
            verify(profileManager).syncUpdatedFields(capture(), eq(localProfileInternal))

            val syncableFields = firstValue

            assertEquals(BRUSHING_NUMBER, syncableFields.brushingNumber)
        }
    }

    private fun networkError() = ApiError("Test forced error", ApiErrorCode.NETWORK_ERROR, "bla")
    private fun otherError() = ApiError("Test forced error", ApiErrorCode.UNKNOWN_ERROR, "bla")

    @Test
    fun `update profile fails if it receives error different than ApiError NETWORK from profileRepository`() {
        val accountId = 2L

        val profileInternal = createProfileInternal()
        whenever(profileRepository.getProfileLocally(profile.id)).thenReturn(
            Single.just(
                profileInternal
            )
        )
        whenever(profileRepository.updateProfileLocally(profileInternal.copyProfile(profile))).thenReturn(
            Single.just(
                profileInternal
            )
        )

        val expectedError = otherError()
        whenever(
            profileRepository.updateProfile(
                accountId,
                profileInternal
            )
        ).thenAnswer {
            throw expectedError
        }

        doReturn(true).whenever(profileManager).syncUpdatedFields(any(), any())

        profileManager.updateProfile(accountId, profile).test().assertError(expectedError)
    }

    /*
    EDIT PERSONAL INFORMATION
     */
    @Test
    fun editPersonalInformation_updateProfileLocallyError_emitsError() {
        val editProfilData: EditProfileData = mock()
        val profileInternal = mockProfileInternal()
        whenever(profileInternalMapper.copyEditData(profileInternal, editProfilData)).thenReturn(profileInternal)

        val expectedError = Throwable("Test forced error")
        whenever(profileRepository.updateProfileLocally(profileInternal)).thenReturn(
            Single.error(
                expectedError
            )
        )

        profileManager.editPersonalInformation(editProfilData, profileInternal).test()
            .assertError(expectedError)
    }

    @Test
    fun editPersonalInformation_updateProfileLocallySuccess_updateProfileError_invokesSyncUpdatedFields() {
        val editProfilData: EditProfileData = mock()
        val accountId = 9
        val profileId = 24L
        val profileInternal = mockProfileInternal(accountId = accountId, profileId = profileId)
        whenever(profileInternalMapper.copyEditData(profileInternal, editProfilData)).thenReturn(profileInternal)

        whenever(profileRepository.updateProfileLocally(profileInternal)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId.toLong(),
                profileInternal
            )
        )
            .thenReturn(Single.error(Throwable("Test forced error")))

        doReturn(false).whenever(profileManager).syncUpdatedFields(editProfilData, profileInternal)

        profileManager.editPersonalInformation(editProfilData, profileInternal).test()

        verify(profileManager).syncUpdatedFields(editProfilData, profileInternal)
    }

    @Test
    fun editPersonalInformation_updateProfileLocallySuccess_updateProfileError_syncUpdatedFieldsReturnsFalse_ReturnsTrue() {
        val editProfilData: EditProfileData = mock()
        val accountId = 9
        val profileId = 24L
        val profileInternal = mockProfileInternal(accountId = accountId, profileId = profileId)
        whenever(profileInternalMapper.copyEditData(profileInternal, editProfilData)).thenReturn(profileInternal)

        whenever(profileRepository.updateProfileLocally(profileInternal)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId.toLong(),
                profileInternal
            )
        )
            .thenReturn(Single.error(Throwable("Test forced error")))

        doReturn(false).whenever(profileManager).syncUpdatedFields(editProfilData, profileInternal)

        profileManager.editPersonalInformation(editProfilData, profileInternal).test()
            .assertValue(true)

        verify(profileManager).syncUpdatedFields(editProfilData, profileInternal)
    }

    @Test
    fun editPersonalInformation_updateProfileLocallySuccess_updateProfileError_syncUpdatedFieldsReturnsTrue_returnsTrue() {
        val editProfilData: EditProfileData = mock()
        val accountId = 9
        val profileId = 24L
        val profileInternal = mockProfileInternal(accountId = accountId, profileId = profileId)
        whenever(profileInternalMapper.copyEditData(profileInternal, editProfilData)).thenReturn(profileInternal)

        whenever(profileRepository.updateProfileLocally(profileInternal)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId.toLong(),
                profileInternal
            )
        )
            .thenReturn(Single.error(Throwable("Test forced error")))

        doReturn(true).whenever(profileManager).syncUpdatedFields(editProfilData, profileInternal)

        profileManager.editPersonalInformation(editProfilData, profileInternal).test()
            .assertValue(true)

        verify(profileManager).syncUpdatedFields(editProfilData, profileInternal)
    }

    @Test
    fun editPersonalInformation_updateProfileLocallySuccess_updateProfileSuccess_emitsTrue() {
        val editProfilData: EditProfileData = mock()
        val accountId = 9
        val profileId = 24L
        val profileInternal = mockProfileInternal(accountId = accountId, profileId = profileId)
        whenever(profileInternalMapper.copyEditData(profileInternal, editProfilData)).thenReturn(profileInternal)

        whenever(profileRepository.updateProfileLocally(profileInternal)).thenReturn(
            Single.just(
                profileInternal
            )
        )

        whenever(
            profileRepository.updateProfile(
                accountId.toLong(),
                profileInternal
            )
        )
            .thenReturn(Single.just(profileInternal))

        profileManager.editPersonalInformation(editProfilData, profileInternal).test()
            .assertValue(true)
    }

    /*
    EDIT PROFILE
     */
    @Test
    fun editProfile_dataIsTypeFieldsFalse_isTypePictureFalse_emitsFalse() {
        val editProfileData: EditProfileData = mock()
        val profileInternal = mockProfileInternal()

        whenever(editProfileData.isTypeFields).thenReturn(false)
        whenever(editProfileData.isTypePicture).thenReturn(false)

        profileManager.editProfile(editProfileData, profileInternal).test().assertValueCount(1)
    }

    @Test
    fun editProfile_dataIsTypeFields_editPersonalInformationReturnsTrue_isTypePictureFalse_neverAttemptsToUpdatePictureAndReturnsTrue() {
        val editProfileData: EditProfileData = mock()
        val profileInternal = mockProfileInternal()

        whenever(editProfileData.isTypeFields).thenReturn(true)
        whenever(editProfileData.isTypePicture).thenReturn(false)

        doReturn(Single.just(true))
            .whenever(profileManager)
            .editPersonalInformation(editProfileData, profileInternal)

        profileManager.editProfile(editProfileData, profileInternal).test().assertValueCount(1)

        verify(profileManager, never()).changeProfilePicture(eq(profileInternal), any())
    }

    @Test
    fun editProfile_dataIsTypeFields_editPersonalInformationReturnsTrue_isTypePictureTrue_returnsChangeProfilePicture() {
        val editProfileData: EditProfileData = mock()
        val profileInternal = mockProfileInternal()

        whenever(editProfileData.isTypeFields).thenReturn(true)
        whenever(editProfileData.isTypePicture).thenReturn(true)

        val path = "ignore"
        whenever(editProfileData.picturePath).thenReturn(path)
        val newProfileInternal = mockProfileInternal(pictureUploadUrl = path)

        doReturn(Single.just(true))
            .whenever(profileManager)
            .editPersonalInformation(editProfileData, profileInternal)

        doReturn(Single.just(newProfileInternal))
            .whenever(profileManager)
            .changeProfilePicture(profileInternal, path)

        profileManager.editProfile(editProfileData, profileInternal).test()
            .assertValue(newProfileInternal)

        verify(profileManager).changeProfilePicture(profileInternal, path)
    }

    @Test
    fun editProfile_dataIsTypePicture__returnsChangeProfilePicture() {
        val editProfileData: EditProfileData = mock()
        val profileInternal = mockProfileInternal()

        whenever(editProfileData.isTypeFields).thenReturn(false)
        whenever(editProfileData.isTypePicture).thenReturn(true)

        val path = "ignore"
        whenever(editProfileData.picturePath).thenReturn(path)
        val newProfileInternal = mockProfileInternal(pictureUploadUrl = path)

        doReturn(Single.just(newProfileInternal))
            .whenever(profileManager)
            .changeProfilePicture(profileInternal, path)

        profileManager.editProfile(editProfileData, profileInternal).test()
            .assertValue(newProfileInternal)

        verify(profileManager).changeProfilePicture(profileInternal, path)
    }

    /*
    SYNC UPDATED FIELDS
     */
    @Test
    fun `syncUpdatedFields doesn't store any field for a fresh EditProfileData`() {
        doReturn(mock<OfflineUpdateInternal>()).whenever(profileManager).createOfflineUpdateInternal(any(), any())

        val profileInternal = mockProfileInternal()
        profileManager.syncUpdatedFields(EditProfileData(), profileInternal)

        argumentCaptor<ArrayList<SyncableField<*>>> {
            verify(profileManager).createOfflineUpdateInternal(capture(), eq(profileInternal))

            assertTrue(firstValue.isEmpty())
        }
    }

    @Test
    fun `syncUpdatedFields adds brushingNumber if it's not UNSET`() {
        doReturn(mock<OfflineUpdateInternal>()).whenever(profileManager).createOfflineUpdateInternal(any(), any())

        val data = EditProfileData()
        val expectedBrushingNumber = 56
        data.brushingNumber = expectedBrushingNumber

        val oldNumber = 1
        val profileInternal = mockProfileInternal(brushingNumber = oldNumber)

        profileManager.syncUpdatedFields(data, profileInternal)

        argumentCaptor<ArrayList<SyncableField<*>>> {
            verify(profileManager).createOfflineUpdateInternal(capture(), eq(profileInternal))

            val fields = firstValue

            assertEquals(1, fields.size)

            val field = fields.first() as IntegerSyncableField
            assertEquals(EditProfileData.FIELD_BRUSHING_NUMBER, field.fieldName)
            assertEquals(oldNumber, field.snapshotValue)
            assertEquals(expectedBrushingNumber, field.newValue)
        }
    }

    /*
    getProfilePicture
     */

    @Test
    fun `getProfilePicture calls through profileRepository`() {
        whenever(profileRepository.getProfilePicture(anyLong(), anyLong()))
            .thenReturn(Single.just(mock()))

        val accountId = 9L
        val profileId = 24L
        profileManager.getProfilePicture(accountId, profileId)

        verify(profileRepository).getProfilePicture(accountId, profileId)
    }

    /*
    UTILS
     */

    private fun createProfileInternal(): ProfileInternal {
        return ProfileInternal(
            id = profileId,
            accountId = 0,
            birthday = null,
            brushingTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS,
            creationDate = "1990-01-01T12:00:00+0000",
            firstName = "Test",
            points = 0,
            brushingNumber = 0
        )
    }

    private fun mockProfileInternal(
        accountId: Int = 0,
        profileId: Long = 0,
        pictureUploadUrl: String = "",
        getPictureUrl: String = "",
        brushingNumber: Int = 0
    ): ProfileInternal {
        val profileInternal: ProfileInternal = mock()
        if (accountId > 0) whenever(profileInternal.accountId).thenReturn(accountId)
        if (profileId > 0) whenever(profileInternal.id).thenReturn(profileId)
        if (pictureUploadUrl.isNotBlank()) whenever(profileInternal.pictureUploadUrl).thenReturn(
            pictureUploadUrl
        )
        if (getPictureUrl.isNotBlank()) whenever(profileInternal.pictureGetUrl).thenReturn(
            getPictureUrl
        )

        whenever(profileInternal.brushingNumber).thenReturn(if (brushingNumber == 0) UNSET else brushingNumber)
        whenever(profileInternal.brushingTime).thenReturn(BRUSHING_TIME)
        whenever(profileInternal.firstName).thenReturn(FIRST_NAME)
        whenever(profileInternal.gender).thenReturn(GENDER)
        return profileInternal
    }

    /*
    UPDATE OR INSERT LOCALLY
     */

    @Test
    fun updateOrInsertProfileLocally_existingProfile_updates() {
        val updatedProfile = mock<ProfileInternal>()
        val existingProfile = mock<ProfileInternal>()
        whenever(profileRepository.getProfileLocally(anyLong()))
            .thenReturn(Single.just(existingProfile))
        profileManager.updateOrInsertProfileLocally(updatedProfile)
        verify(profileRepository).updateProfileLocally(updatedProfile)
        verify(profileRepository, never()).insertProfileLocally(any())
    }

    @Test
    fun updateOrInsertProfileLocally_notExistingProfile_inserts() {
        val updatedProfile = mock<ProfileInternal>()
        whenever(profileRepository.getProfileLocally(anyLong()))
            .thenReturn(Single.error(Exception()))
        profileManager.updateOrInsertProfileLocally(updatedProfile)
        verify(profileRepository, never()).updateProfileLocally(updatedProfile)
        verify(profileRepository).insertProfileLocally(updatedProfile)
    }

    companion object {
        private const val BRUSHING_NUMBER = 7
        private const val BRUSHING_TIME = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
        private const val FIRST_NAME = "john"
        private const val GENDER = "M"

        private const val profileId = 42L
        val profile = Profile(
            brushingGoalTime = 120,
            id = profileId,
            firstName = "John",
            gender = Gender.MALE,
            handedness = Handedness.LEFT_HANDED,
            coachTransitionSounds = true,
            deletable = false,
            points = 0,
            exactBirthdate = false,
            age = 42,
            createdDate = "1990-02-02T12:00:00+0000",
            birthday = DateConvertersString().getLocalDateFromString("1990-02-04"),
            brushingNumber = BRUSHING_NUMBER
        )
    }
}
