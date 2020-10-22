/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.account.profile.ProfileDeletedHook
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.sdkws.account.AccountManager
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileOperations
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileManager
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

@Keep
interface ProfileFacade : ProfileOperations {

    /**
     * Fetch the profile associated to a profileId from the backend
     *
     * @param profileId profileID associated to the profile to get
     *
     * @return [Single] that will emit the [IProfile] from the backend and update the local storage
     *
     * Emits [NoSuchElementException] if there's no profile with [profileId]
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    fun getRemoteProfile(profileId: Long): Single<IProfile>

    /**
     * Delete the profile associated to a profile ID
     * If the deleted profile was the active one, the owner profile will be set as the current one.
     *
     * Note that the owner profile (the one that has been created when the account has been
     * registered) can't be deleted.
     *
     * @param profileId ID of the profile to delete
     * @return [Boolean] [Single] true if the profile was deleted, false otherwise (owner profile)
     */
    fun deleteProfile(profileId: Long): Single<Boolean>

    /**
     * Add a new profile to the current Account
     *
     * @return [Single] that will emit the created [IProfile]
     *
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    fun createProfile(profile: IProfile): Single<IProfile>

    /**
     * Edit a profile
     * Your object needs to implement the IProfile interface to be able to
     * use this method.
     *
     * @param profile profile to edit
     * @return non null [Boolean] [Single] success
     */
    fun editProfile(profile: IProfile): Single<IProfile>

    /**
     * Change the profile picture for a given profile
     *
     * @return non null [IProfile] [Single] profile updated
     */
    fun changeProfilePicture(profile: IProfile, picturePath: String): Single<IProfile>

    /**
     * Emits the active profile, as well as future active profile changes
     *
     * @return non null [Flowable] [IProfile]
     */
    fun activeProfileFlowable(): Flowable<IProfile>

    /**
     * Get the profile associated to a profileId from local storage
     *
     * @return [Single] that will emit the [IProfile] with [profileId].
     *
     * Emits [NoSuchElementException] if there's no profile with [profileId]
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    fun getProfile(profileId: Long): Single<IProfile>

    /**
     * Get the profiles from local storage
     *
     * @return [Single] that will emit a list of all local [IProfile]
     *
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    fun getProfilesList(): Single<List<IProfile>>

    /**
     * Fetch the logged in account profiles from the backend
     *
     * @return [Single] that will emit a list of all [IProfile] from the backend and update the local
     * storage
     *
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    fun getRemoteProfiles(): Single<List<IProfile>>

    fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse>
}

/**
 * Expose all operations related to Account
 */
internal class ProfileFacadeImpl
@Inject constructor(
    connector: IKolibreeConnector,
    private val accountFacade: AccountFacade,
    private val profileManager: ProfileManager,
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase,
    private val accountManager: AccountManager,
    private val currentProfileProvider: CurrentProfileProvider,
    private val profileDeletedHooks: Set<@JvmSuppressWildcards ProfileDeletedHook>
) : ProfileFacade, ProfileOperations by connector {
    private fun currentAccountSingle() = accountFacade.getAccountSingle()

    /**
     * Create a new profile
     *
     * @return [Single] that will emit the created [IProfile]
     *
     * Emits [NoAccountException] if there's no [Account] logged in
     */
    override fun createProfile(profile: IProfile): Single<IProfile> {
        return currentAccountSingle()
            .flatMap { account ->
                val newProfile = CreateProfileData()
                newProfile.firstName = profile.firstName
                newProfile.setGender(profile.gender)
                newProfile.setHandedness(profile.handedness)

                val birthday = profile.birthday
                if (birthday != null) {
                    newProfile.birthday = profile.birthday
                }
                newProfile.age = birthday?.let { getAgeFromBirthDate(it) } ?: Profile.DEFAULT_AGE

                profileManager.createProfile(newProfile, account.backendId)
                    .flatMap { createdProfile ->
                        val postBrushingModeSingle =
                            if (!profile.pictureUrl.isNullOrEmpty()) {
                                uploadPicture(createdProfile, profile.pictureUrl!!)
                            } else {
                                Single.just(createdProfile)
                            }

                        synchronizeBrushingModeUseCase.initBrushingModeForProfile(createdProfile.id)
                            .andThen(postBrushingModeSingle)
                    }
                    .map { it.exportProfile() }
            }
    }

    @VisibleForTesting
    fun uploadPicture(
        profile: ProfileInternal,
        picturePath: String
    ): Single<ProfileInternal> {
        return when {
            // The picture is an external link and needs to be uploaded and sign first
            picturePath.startsWith("http") -> profileManager.downloadExternalPicture(
                profile,
                picturePath
            )
            else -> profileManager.changeProfilePicture(profile, picturePath)
        }
    }

    override fun deleteProfile(profileId: Long): Single<Boolean> =
        currentAccountSingle()
            .flatMap { account ->
                profileManager.deleteProfile(account.backendId, profileId)
                    .flatMap { profileHasBeenDeleted ->
                        if (profileHasBeenDeleted) {
                            onProfileDeletedHooks(profileId).toSingleDefault(true)
                        } else {
                            Single.just(false)
                        }
                    }
            }

    @VisibleForTesting
    fun onProfileDeletedHooks(profileId: Long): Completable {
        return Completable.mergeDelayError(profileDeletedHooks.map { it.onProfileDeleted(profileId) })
    }

    override fun editProfile(profile: IProfile): Single<IProfile> = with(Profile.of(profile)) {
        return@with currentAccountSingle().flatMap { account ->
            val newProfile = EditProfileData()
            newProfile.firstName = firstName
            newProfile.brushingTime = brushingGoalTime
            newProfile.handedness = profile.handedness.serializedName
            newProfile.setGender(gender)

            val newProfileBirthday = birthday
            if (newProfileBirthday != null) {
                newProfile.setBirthday(newProfileBirthday)
            }

            val profileInternal = ProfileInternal(
                id = id,
                gender = newProfile.gender,
                birthday = newProfileBirthday,
                firstName = firstName,
                points = 0,
                brushingTime = brushingGoalTime,
                creationDate = createdDate,
                handedness = newProfile.handedness,
                accountId = account.backendId.toInt(),
                pictureUrl = profile.pictureUrl,
                pictureLastModifier = profile.pictureLastModifier,
                addressCountry = profile.country
            )

            profileManager.editProfile(newProfile, profileInternal)
                .map { it.exportProfile() as IProfile }
        }
    }

    override fun changeProfilePicture(profile: IProfile, picturePath: String): Single<IProfile> {
        return profileManager.getProfileInternalLocally(profile.id)
            .map { localProfile -> localProfile.copy(pictureUrl = picturePath) }
            .flatMap { profileWithUpdatedPicture ->
                profileManager.updateOrInsertProfileLocally(profileWithUpdatedPicture)
            }
            .flatMap { updatedLocalProfile ->
                uploadPicture(updatedLocalProfile, picturePath).map { it.exportProfile() }
            }
    }

    override fun getProfile(profileId: Long): Single<IProfile> {
        return currentAccountSingle()
            .map { account -> account.profiles.first { it.id == profileId } }
    }

    override fun getRemoteProfile(profileId: Long): Single<IProfile> {
        return currentAccountSingle()
            .flatMap { currentAccount ->
                accountManager.getAccount(currentAccount.backendId)
                    .flatMap { remoteAccount ->
                        remoteAccount.getProfileInternalWithId(profileId)?.let {
                            updateOrInsertFetchedProfileLocally(it)
                        } ?: Single.error(NoSuchElementException())
                    }
            }
    }

    override fun getProfilesList(): Single<List<IProfile>> {
        return currentAccountSingle()
            .map { account -> account.profiles }
    }

    override fun getRemoteProfiles(): Single<List<IProfile>> {
        return currentAccountSingle()
            .flatMap { currentAccount ->
                accountManager.getAccount(currentAccount.backendId)
                    .flatMap { remoteAccount ->
                        Flowable.fromIterable(remoteAccount.internalProfiles)
                            .flatMapSingle { remoteProfile ->
                                updateOrInsertFetchedProfileLocally(remoteProfile)
                            }
                            .toList()
                    }
            }
    }

    override fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse> {
        return profileManager.getProfilePicture(accountId, profileId)
    }

    @VisibleForTesting
    fun updateOrInsertFetchedProfileLocally(profileInternal: ProfileInternal): Single<Profile> {
        return profileManager.updateOrInsertProfileLocally(profileInternal)
            .map { p -> p.exportProfile() }
    }

    override fun activeProfileFlowable(): Flowable<IProfile> =
        currentProfileProvider.currentProfileFlowable().map { it }
}
