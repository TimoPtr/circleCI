package com.kolibree.sdkws.profile

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.sync.IntegerSyncableField
import com.kolibree.sdkws.core.sync.StringSyncableField
import com.kolibree.sdkws.core.sync.SyncableField
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.internal.OfflineUpdateDatastore
import com.kolibree.sdkws.internal.OfflineUpdateInternal
import com.kolibree.sdkws.profile.models.PictureResponse
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import timber.log.Timber

internal class ProfileManagerImpl @Inject
constructor(
    private val offlineUpdateDatastore: OfflineUpdateDatastore,
    private val kolibreeUtils: KolibreeUtils,
    private val fileDownloader: FileDownloader,
    private val avatarCache: AvatarCache,
    private val profileRepository: ProfileRepository,
    private val profileInternalMapper: ProfileInternalMapper
) : ProfileManager {

    /**
     * Updates profile with id matching profile.id with the content of the profile parameter.
     *
     * This includes several steps
     * 1. Update profile locally
     * 2. Update profile remotely
     * 3. Update profile locally with the values returned from backend
     *
     * If remote profile fails for whatever reason, we store the values for future update
     *
     * @return a Single that will emit a Profile when all of the steps are completed. If the remote call fails due to
     * lack of connectivity, it will emit the updated local Profile. Any other error will be emitted.
     */
    override fun updateProfile(accountId: Long, profile: Profile): Single<Profile> {
        return profileRepository.getProfileLocally(profileId = profile.id)
            .flatMap { localProfileSnapshot ->
                Single.just(localProfileSnapshot.copyProfile(profile))
                    .flatMap { updatedProfile ->
                        profileRepository.updateProfileLocally(updatedProfile)
                            .flatMap {
                                profileRepository.updateProfile(accountId, it)
                                    .flatMap { profileFromRemote ->
                                        // update from profile returned from server
                                        profileRepository.updateProfileLocally(
                                            profileFromRemote
                                        )
                                    }
                                    .onErrorReturn { throwable ->
                                        maybeStoreForOfflineUpdate(
                                            throwable,
                                            updatedProfile,
                                            localProfileSnapshot
                                        )

                                        onUpdateProfileErrorReturn(throwable, updatedProfile)
                                    }
                            }
                    }
            }
            .map { it.exportProfile() }
    }

    private fun onUpdateProfileErrorReturn(
        throwable: Throwable,
        updatedProfile: ProfileInternal
    ): ProfileInternal {
        // if it's a network error, don't throw it
        return if (throwable is ApiError && throwable.isNetworkError)
            updatedProfile
        else
            throw throwable
    }

    /*
    This is a quite limited solution and check. If the exception truly wasn't recoverable, we should handle the case
    better. For example, if we get a 404, the resource no longer exists and should be deleted locally
     */
    private fun maybeStoreForOfflineUpdate(
        throwable: Throwable,
        updatedProfile: ProfileInternal,
        oldProfileSnapshot: ProfileInternal
    ) {
        if (isRecoverableException(throwable)) {
            // on error, store for offline update
            syncUpdatedFields(
                ProfileSyncableMapper.toProfileSyncableFields(updatedProfile),
                oldProfileSnapshot
            )
        }
    }

    private fun isRecoverableException(throwable: Throwable): Boolean {
        if (throwable is ApiError && throwable.isNetworkError) return true

        return false
    }

    /**
     * Create new profile for current account
     *
     * @param data Profile data
     */
    override fun createProfile(data: CreateProfileData, accountId: Long): Single<ProfileInternal> {
        return profileRepository.createProfile(data, accountId)
            .flatMap { profile ->
                changeProfilePicture(profile, data.picturePath).map { profile }
            }
    }

    override fun editProfile(
        data: EditProfileData,
        profile: ProfileInternal
    ): Single<ProfileInternal> {
        return when {
            data.isTypeFields -> editPersonalInformation(data, profile).flatMap {
                if (data.isTypePicture) return@flatMap changeProfilePicture(
                    profile,
                    data.picturePath
                )

                Single.just(profile)
            }
            data.isTypePicture -> changeProfilePicture(profile, data.picturePath)
            else -> Single.just(profile)
        }
    }

    // First we save old profile data in case of sync failure
    // Then we update local version
    @VisibleForTesting
    internal fun editPersonalInformation(
        data: EditProfileData,
        profile: ProfileInternal
    ): Single<Boolean> {
        return profileRepository.updateProfileLocally(
            profileInternalMapper.copyEditData(
                profile,
                data
            )
        )
            .flatMap {
                profileRepository.updateProfile(profile.accountId.toLong(), it)
                    .map { true }
                    /*
                    If there's an error, store for future update and return true, since
                    we did updated it locally

                    syncUpdatedFields return value isn't meaningful, so we ignore it
                     */
                    .doOnError { syncUpdatedFields(data, profile) }
                    .onErrorReturnItem(true)
            }
    }

    @VisibleForTesting
    internal fun syncUpdatedFields(data: ProfileSyncableFields, profile: ProfileInternal): Boolean {
        val fields = ArrayList<SyncableField<*>>()

        if (data.firstName != null) {
            fields.add(
                StringSyncableField(
                    EditProfileData.FIELD_FIRST_NAME, profile.firstName,
                    data.firstName
                )
            )
        }

        if (data.gender != null) {
            fields.add(
                StringSyncableField(
                    EditProfileData.FIELD_GENDER, profile.gender,
                    data.gender
                )
            )
        }
        if (data.age != EditProfileData.UNSET) {
            fields.add(
                IntegerSyncableField(
                    EditProfileData.FIELD_AGE, profile.age,
                    data.age
                )
            )
        }
        if (data.brushingTime != EditProfileData.UNSET) {
            fields.add(
                IntegerSyncableField(
                    EditProfileData.FIELD_BRUSHING_GOAL_TIME,
                    profile.brushingTime, data.brushingTime
                )
            )
        }
        if (data.handedness != null) {
            fields.add(
                StringSyncableField(
                    EditProfileData.FIELD_SURVEY_HANDEDNESS,
                    profile.handedness, data.handedness
                )
            )
        }
        if (data.countryCode != null) {
            fields.add(
                StringSyncableField(
                    EditProfileData.FIELD_COUNTRY, profile.addressCountry,
                    data.countryCode
                )
            )
        }
        if (data.brushingNumber != EditProfileData.UNSET) {
            fields.add(
                IntegerSyncableField(
                    EditProfileData.FIELD_BRUSHING_NUMBER,
                    profile.brushingNumber,
                    data.brushingNumber
                )
            )
        }

        val inserted =
            offlineUpdateDatastore.insertOrUpdate(createOfflineUpdateInternal(fields, profile))
        Timber.d("Profile with id %d is not synchronized, saving updates", profile.id)

        /*
         If no previous update for this profile (if there is one it will be updated so no need to
         increment data version)
          */
        if (!inserted) {
            Timber.d("There was an update for profile with id %d, merging", profile.id)
        }
        return inserted
    }

    @VisibleForTesting // dummy method needed for mocking
    fun createOfflineUpdateInternal(
        fields: ArrayList<SyncableField<*>>,
        profile: ProfileInternal
    ) = OfflineUpdateInternal(fields, profile.id)

    override fun changeProfilePicture(
        profile: ProfileInternal,
        picturePath: String?
    ): Single<ProfileInternal> {
        return when {
            picturePath.isNullOrEmpty() -> Single.just(profile)
            // The picture has been taken and is a file, need to upload
            !picturePath.startsWith("http") -> {
                uploadExternalPicture(profile, picturePath)
                    .flatMap { profileFromRemote ->
                        profileFromRemote.pictureGetUrl?.let { pictureGetUrl ->
                            confirmPicture(profileFromRemote, pictureGetUrl, picturePath)
                        } ?: Single.error(Throwable("Backend returned empty pictureGetUrl"))
                    }
            }
            else -> confirmPicture(profile, picturePath, picturePath)
        }
    }

    @VisibleForTesting
    internal fun uploadExternalPicture(
        profile: ProfileInternal,
        picturePath: String
    ): Single<ProfileInternal> {
        return getProfileAssociatedToThePictures(profile, picturePath)
            .flatMap {
                Single.fromCallable {
                    val pictureFile = File(picturePath)

                    kolibreeUtils.uploadPicture(it.pictureUploadUrl, pictureFile)

                    it
                }.doOnError(Timber::e)
                    .onErrorResumeNext(onErrorUploadingPicture(profile, picturePath))
            }
    }

    @VisibleForTesting
    internal fun getProfileAssociatedToThePictures(
        profile: ProfileInternal,
        picturePath: String
    ): Single<ProfileInternal> {
        if (!profile.pictureUploadUrl.isNullOrEmpty()) return Single.just(profile)

        return profileRepository.getPictureUploadUrl(profile.accountId, profile.id)
            .onErrorResumeNext(onErrorUploadingPicture(profile, picturePath))
    }

    private fun onErrorUploadingPicture(
        profile: ProfileInternal,
        picturePath: String
    ): (Throwable) -> Single<ProfileInternal> {
        return { t ->
            Timber.e("Failed to upload picture for profile id %s, adding update", profile.id)
            onProfilePictureUpdateFailed(picturePath, profile.id)

            Single.error(t)
        }
    }

    @VisibleForTesting
    internal fun confirmPicture(
        profile: ProfileInternal,
        confirmPictureUrl: String,
        picturePath: String
    ): Single<ProfileInternal> {
        return profileRepository.confirmPictureUrl(profile.accountId, profile.id, confirmPictureUrl)
            .map { pictureRespone ->
                pictureRespone.copy(picture = pictureRespone.picture.replace("\"", ""))
            }
            .flatMap { pictureResponse ->
                profileRepository.updateProfileLocally(
                    profile.copy(
                        pictureUrl = pictureResponse.picture,
                        pictureLastModifier = pictureResponse.pictureLastModifier
                    )
                )
            }
            .map {
                avatarCache(it)

                // Delete local file if any
                if (!picturePath.startsWith("http")) {
                    File(picturePath).deleteOnExit()
                }
                it
            }
            .doOnError {
                it.printStackTrace()
                Timber.d(
                    "Failed to confirm picture update for profile id %d, adding update",
                    profile.id
                )
                onProfilePictureUpdateFailed(picturePath, profile.id)
            }
    }

    override fun avatarCache(profile: ProfileInternal) {
        avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
    }

    override fun downloadExternalPicture(
        profile: ProfileInternal,
        picturePath: String
    ): Single<ProfileInternal> {
        return Single.just(fileDownloader.download(picturePath))
            .flatMap { file -> changeProfilePicture(profile, file.absolutePath) }
    }

    @VisibleForTesting
    internal fun onProfilePictureUpdateFailed(picturePath: String, profileId: Long) {
        offlineUpdateDatastore.insertOrUpdate(OfflineUpdateInternal(picturePath, profileId))
    }

    override fun getProfilesLocally(): Single<List<Profile>> {
        return profileRepository.getProfilesLocally()
            .map { internalProfiles ->
                internalProfiles.map { it.exportProfile() }
            }
    }

    override fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse> {
        return profileRepository.getProfilePicture(accountId, profileId)
    }

    @SuppressLint("CheckResult")
    override fun updateOrInsertProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal> {
        try { // Check if the profile exists
            profileRepository.getProfileLocally(profileInternal.id).blockingGet()
        } catch (_: Exception) {
            return profileRepository.insertProfileLocally(profileInternal)
        }

        return profileRepository.updateProfileLocally(profileInternal)
    }

    override fun deleteProfileLocally(profileId: Long) =
        profileRepository.deleteProfileLocally(profileId)

    override fun deleteProfile(profile: ProfileInternal) =
        profileRepository.deleteProfile(profile.accountId.toLong(), profile.id)

    override fun deleteProfile(accountId: Long, profileId: Long) =
        profileRepository.deleteProfile(accountId, profileId)

    override fun getProfileLocally(profileId: Long) =
        getProfileInternalLocally(profileId).map { it.exportProfile() }

    override fun getProfileInternalLocally(profileId: Long): Single<ProfileInternal> {
        return profileRepository.getProfileLocally(profileId)
    }
}

private object ProfileSyncableMapper {
    fun toProfileSyncableFields(profileInternal: ProfileInternal): ProfileSyncableFields {
        return object : ProfileSyncableFields {
            override var firstName: String?
                get() = profileInternal.firstName
                set(_) {}
            override var gender: String?
                get() = profileInternal.gender
                set(_) {}
            override var age: Int
                get() = profileInternal.age
                set(_) {}
            override var brushingTime: Int
                get() = profileInternal.brushingTime
                set(_) {}
            override var brushingNumber: Int
                get() = profileInternal.brushingNumber
                set(_) {}
            override var handedness: String?
                get() = profileInternal.handedness
                set(_) {}
            override var countryCode: String?
                get() = profileInternal.addressCountry
                set(_) {}
        }
    }
}
