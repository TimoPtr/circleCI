/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core;

import static com.kolibree.android.commons.BrushingConstantsKt.DEFAULT_BRUSHING_GOAL;
import static com.kolibree.android.commons.JobServiceIdConstants.SYNC_IMMEDIATE;
import static com.kolibree.android.commons.JobServiceIdConstants.SYNC_WHEN_NETWORK;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.synchronizator.Synchronizator;
import com.kolibree.sdkws.appdata.AppDataManager;
import com.kolibree.sdkws.core.sync.IntegerSyncableField;
import com.kolibree.sdkws.core.sync.StringSyncableField;
import com.kolibree.sdkws.data.model.EditProfileData;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateInternal;
import com.kolibree.sdkws.profile.ProfileApi;
import com.kolibree.sdkws.profile.ProfileManager;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import kotlin.Unit;
import timber.log.Timber;

/** Created by miguelaragues on 9/3/18. */
@SuppressLint("SpecifyJobSchedulerIdRange")
public class SynchronizerJobService extends JobService {

  @VisibleForTesting JobParameters jobParameters;

  @Inject InternalKolibreeConnector kolibreeConnector;

  @Inject AvatarCache avatarCache;

  @VisibleForTesting Disposable syncDisposable;

  @VisibleForTesting AccountInternal currentAccount;

  @Inject ProfileDatastore profileDatastore;

  @Inject ProfileApi profileApi;

  @Inject AccountDatastore accountDatastore;

  @Inject OfflineUpdateDatastore offlineUpdateDatastore;

  @Inject Synchronizator synchronizator;

  @Inject AppDataManager appDataManager;

  @Inject ProfileManager profileManager;

  static JobInfo syncWhenNetworkAvailableJobInfo(@NonNull Context context) {
    return baseBuilder(context, SYNC_WHEN_NETWORK).setPersisted(true).build();
  }

  static JobInfo syncImmediatelyJobInfo(@NonNull Context context) {
    return baseBuilder(context, SYNC_IMMEDIATE)
        .setOverrideDeadline(0) // run immediately
        .setPersisted(false)
        .build();
  }

  @NonNull
  private static JobInfo.Builder baseBuilder(@NonNull Context context, int jobId) {
    return new JobInfo.Builder(jobId, new ComponentName(context, SynchronizerJobService.class))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
  }

  @Override
  public void onCreate() {
    AndroidInjection.inject(this);
    super.onCreate();
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    Timber.d("Enforcer synchronizer onStartJob");
    jobParameters = params;

    currentAccount = kolibreeConnector.currentAccount();

    if (currentAccount == null) {
      completeJobWithoutReschedule();

      return false;
    }

    sync();

    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    if (syncDisposable != null && !syncDisposable.isDisposed()) {
      syncDisposable.dispose();
    }

    return false;
  }

  @VisibleForTesting
  void sync() {
    syncDisposable =
        Completable.fromAction(
                () -> {
                  // /!\ Changing the order of the invocations below can lead to a bad user
                  // experience (data not yet available when he reaches a tab or a screen)
                  synchronizeProfiles();

                  synchronizeBrushings();

                  synchronizator.synchronize();

                  synchronizePirate();

                  synchronizeExternalAppData();
                })
            .subscribeOn(Schedulers.io())
            /*
            Even if there's an error, we want to invoke onSynchronizationCompleted.

            We don't know where the error comes from, maybe there's some new content
             */
            .onErrorResumeNext(
                throwable -> {
                  throwable.printStackTrace();

                  return Completable.complete();
                })
            .subscribe(this::onSynchronizationCompleted, Throwable::printStackTrace);
  }

  @VisibleForTesting
  void onSynchronizationCompleted() {
    kolibreeConnector.sendRefreshBroadcast();

    completeJobWithoutReschedule();
  }

  @SuppressLint("CheckResult")
  @VisibleForTesting
  void synchronizeBrushings() {
    for (ProfileInternal p : currentAccount.getInternalProfiles()) {
      try {
        kolibreeConnector.synchronizeBrushing(p.getId()).blockingGet();
      } catch (Exception e) {
        // if a recoverable error happens (e.g. 404 profile no longer exists), we don't want the
        // whole sync to halt
        Timber.e(e);
      }
    }
  }

  @VisibleForTesting
  void synchronizePirate() {
    for (ProfileInternal p : currentAccount.getInternalProfiles()) {
      kolibreeConnector.synchronizeGoPirate(p.getId());
    }
  }

  @VisibleForTesting
  void synchronizeExternalAppData() {
    for (ProfileInternal p : currentAccount.getInternalProfiles()) {
      try {
        appDataManager.synchronize(p.getId()).blockingAwait();
      } catch (Exception e) {
        // if a recoverable error happens (e.g. 404 profile no longer exists), we don't want the
        // whole sync to halt
        Timber.e(e);
      }
    }
  }

  @VisibleForTesting
  void synchronizeProfiles() {
    // First let's check if we have any local profiles that needs update
    AccountInternal localAccount = kolibreeConnector.currentAccount();
    if (localAccount != null) {
      for (ProfileInternal profile : localAccount.getInternalProfiles()) {
        if (profile.getNeedsUpdate()) {
          profileApi
              .updateProfile(localAccount.getId(), profile.getId(), profile)
              .ignoreElement()
              .andThen(profileDatastore().markAsUpdated(profile))
              .blockingAwait();
        }
      }
    }

    final OfflineUpdateDatastore offlineUpdateDao = getOfflineUpdateDatastore();

    // First upload avatars (blocking operation)  to avoid incoming changes overwriting local values
    uploadAvatarIfUpdatedWhileOffline(offlineUpdateDao);

    // Then we get the updated remote version
    AccountInternal remoteAccount = getRemoteAccount();

    if (remoteAccount != null) {
      // Then we check for new or deleted profiles
      checkForNewOrDeletedProfiles(remoteAccount);
    }

    if (remoteAccount != null) {
      // Then we check all updatable fields
      checkUpdatableFields(remoteAccount, offlineUpdateDao);

      // remote account could change after local changes update
      AccountInternal updatedRemoteAccount = remoteOrDefault(remoteAccount);

      // Persist the current profile ID which is only a local field
      updateCurrentProfileIdFromLocalAccount(updatedRemoteAccount);

      // Finally we update all local fields
      updateBrushingSettingsSoundsFromInternalProfiles(updatedRemoteAccount);

      // Save the account
      kolibreeConnector.saveAccount(updatedRemoteAccount);
    }

    refreshAvatarCache();
  }

  @VisibleForTesting
  @NonNull
  AccountInternal remoteOrDefault(@NonNull AccountInternal defaultAccount) {
    AccountInternal remoteAccount = getRemoteAccount();
    if (remoteAccount != null) {
      return remoteAccount;
    }
    return defaultAccount;
  }

  @VisibleForTesting
  @Nullable
  AccountInternal getRemoteAccount() {
    try {
      return kolibreeConnector.getRemoteAccount(currentAccount.getId()).blockingGet();
    } catch (Exception e) {
      Timber.e(e);
    }

    return null;
  }

  /**
   * Update Coach+ fields that are stored locally after we retrieve each profile from the server so
   * far - TransitionSounds & CoachMusic fields
   *
   * @param remoteAccount The internal account to be updated
   */
  @VisibleForTesting
  void updateBrushingSettingsSoundsFromInternalProfiles(@NonNull AccountInternal remoteAccount) {
    for (ProfileInternal pi : remoteAccount.getInternalProfiles()) {
      ProfileInternal profileInternalLocal = profileDatastore.getProfile(pi.getId()).blockingGet();
      pi.setTransitionSounds(profileInternalLocal.getTransitionSounds());
      pi.setCoachMusic(profileInternalLocal.getCoachMusic());
    }
  }

  @VisibleForTesting
  void updateCurrentProfileIdFromLocalAccount(@NonNull AccountInternal remoteAccount) {
    if (currentAccount != null && currentAccount.getCurrentProfileId() != null) {
      remoteAccount.setCurrentProfileId(currentAccount.getCurrentProfileId());
    } else {
      Timber.e("Current account was null, or there was no current profile ID");
    }
  }

  @VisibleForTesting
  void checkUpdatableFields(
      @NonNull AccountInternal remoteAccount, OfflineUpdateDatastore offlineUpdateDao) {
    final ProfileDatastore profileDatastore = profileDatastore();

    for (ProfileInternal localProfile : currentAccount.getInternalProfiles()) {
      // We get the remote version of this profile
      final ProfileInternal remoteProfile =
          remoteAccount.getProfileInternalWithId(localProfile.getId());

      // Profile has been deleted with another device, delete it locally
      if (remoteProfile == null) {
        profileDatastore.deleteProfile(localProfile.getId());
        resetActiveProfile();
      } else {
        // We check if there is a local update for this profile
        final OfflineUpdateInternal update =
            offlineUpdateDao.getOfflineUpdateForProfileId(
                localProfile.getId(), OfflineUpdateInternal.TYPE_FIELDS);

        if (update == null) { // No local update, set with remote values
          updateLocalProfile(localProfile, remoteProfile);
        } else { // We have to compare each field
          onProfileUpdatedLocally(localProfile, remoteProfile, update);
        }
      }
    }
  }

  @VisibleForTesting
  void onProfileUpdatedLocally(
      ProfileInternal localProfile, ProfileInternal remoteProfile, OfflineUpdateInternal update) {
    final EditProfileData data = emptyEditProfileData();

    // First name
    final StringSyncableField firstNameSF =
        (StringSyncableField) update.getUpdateForField(EditProfileData.FIELD_FIRST_NAME);

    if (firstNameSF != null) { // We have local changes
      if (remoteProfile
          .getFirstName()
          .equals(firstNameSF.getSnapshotValue())) { // Remote version untouched
        data.setFirstName(firstNameSF.getNewValue());
      } else { // Priority to remote version
        localProfile.setFirstName(remoteProfile.getFirstName());
      }
    }

    // Handedness
    final StringSyncableField handednessSF =
        (StringSyncableField) update.getUpdateForField(EditProfileData.FIELD_SURVEY_HANDEDNESS);

    if (handednessSF != null) { // We have local changes
      if (remoteProfile
          .getHandedness()
          .equals(handednessSF.getSnapshotValue())) { // Remote version untouched
        data.setHandedness(handednessSF.getNewValue());
      } else { // Priority to remote version
        localProfile.setHandedness(remoteProfile.getHandedness());
      }
    }

    // Birthday
    final IntegerSyncableField ageSF =
        (IntegerSyncableField) update.getUpdateForField(EditProfileData.FIELD_AGE);

    if (ageSF != null) { // We have local changes
      if (remoteProfile.getAge() == ageSF.getSnapshotValue()) { // Remote version untouched
        data.setAge(ageSF.getNewValue());
      } else { // Priority to remote version
        localProfile.setAge(remoteProfile.getAge());
      }
    }

    // Gender
    final StringSyncableField genderSF =
        (StringSyncableField) update.getUpdateForField(EditProfileData.FIELD_GENDER);

    if (genderSF != null) { // We have local changes
      if (remoteProfile
          .getGender()
          .equals(genderSF.getSnapshotValue())) { // Remote version untouched
        data.setGender(genderSF.getNewValue());
      } else { // Priority to remote version
        localProfile.setGender(remoteProfile.getGender());
      }
    }

    // Country
    final StringSyncableField countrySF =
        (StringSyncableField) update.getUpdateForField(EditProfileData.FIELD_COUNTRY);

    if (countrySF != null) { // We have local changes
      if (remoteProfile
          .getAddressCountry()
          .equals(countrySF.getSnapshotValue())) { // Remote version untouched
        data.setCountryCode(countrySF.getNewValue());
      } else { // Priority to remote version
        localProfile.setAddressCountry(remoteProfile.getAddressCountry());
      }
    }

    // Brushing time
    final IntegerSyncableField brushingTimeSF =
        (IntegerSyncableField) update.getUpdateForField(EditProfileData.FIELD_BRUSHING_GOAL_TIME);

    if (brushingTimeSF != null) { // We have local changes
      if (remoteProfile.getBrushingTime()
          == brushingTimeSF.getSnapshotValue()) { // Remote version untouched
        data.setBrushingTime(brushingTimeSF.getNewValue());
        FailEarly.failInConditionMet(
            brushingTimeSF.getNewValue() == EditProfileData.UNSET,
            "We may save UNSET brushing time here!",
            () -> {
              data.setBrushingTime(DEFAULT_BRUSHING_GOAL);
              return Unit.INSTANCE;
            });
      } else { // Priority to remote version
        localProfile.setBrushingTime(remoteProfile.getBrushingTime());
        FailEarly.failInConditionMet(
            remoteProfile.getBrushingTime() == EditProfileData.UNSET,
            "We may save UNSET brushing time here!",
            () -> {
              localProfile.setBrushingTime(DEFAULT_BRUSHING_GOAL);
              return Unit.INSTANCE;
            });
      }
    }

    final IntegerSyncableField brushingNumberSF =
        (IntegerSyncableField) update.getUpdateForField(EditProfileData.FIELD_BRUSHING_NUMBER);
    if (brushingNumberSF != null) { // We have local changes
      if (remoteProfile.getBrushingNumber()
          == brushingNumberSF.getSnapshotValue()) { // Remote version untouched
        data.setBrushingNumber(brushingNumberSF.getNewValue());
      } else { // Priority to remote version
        localProfile.setBrushingNumber(remoteProfile.getBrushingNumber());
      }
    }

    // Then we update profile
    localProfile.setPoints(remoteProfile.getPoints());

    profileDatastore().updateProfile(localProfile).blockingAwait();

    // If it remains doable local updates we update but we do nothing on failure (rejected)
    if (data.hasUpdate()) {
      try {
        kolibreeConnector.editProfile(data, localProfile).blockingGet();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @NonNull
  @VisibleForTesting
  EditProfileData emptyEditProfileData() {
    return new EditProfileData();
  }

  @SuppressLint("CheckResult")
  @VisibleForTesting
  void uploadAvatarIfUpdatedWhileOffline(OfflineUpdateDatastore offlineUpdateDatastore) {
    for (ProfileInternal p : currentAccount.getInternalProfiles()) {
      final OfflineUpdateInternal pictureUpdate =
          offlineUpdateDatastore.getOfflineUpdateForProfileId(
              p.getId(), OfflineUpdateInternal.TYPE_PICTURE);

      if (pictureUpdate != null) {
        final String picturePath = pictureUpdate.getPicturePath();
        try {
          //noinspection ResultOfMethodCallIgnored
          kolibreeConnector.changeProfilePictureSingle(p, picturePath).blockingGet();
        } catch (Exception e) {
          Timber.w(e, "Error changing profile picture");
        }
      }
    }
  }

  @VisibleForTesting
  void checkForNewOrDeletedProfiles(@NonNull AccountInternal remoteAccount) {
    List<Long> remoteProfileIds = new ArrayList<>();
    for (ProfileInternal remoteProfile : remoteAccount.getInternalProfiles()) {
      remoteProfileIds.add(remoteProfile.getId());
      final ProfileInternal localProfile =
          currentAccount.getProfileInternalWithId(remoteProfile.getId());

      if (localProfile == null) { // We do not have this local version
        onRemoteProfileNotInDatabase(remoteProfile);
      }
    }

    for (ProfileInternal localProfile : currentAccount.getInternalProfiles()) {
      if (!remoteProfileIds.contains(localProfile.getId())) {
        onProfileRemovedRemotely(localProfile);
      }
    }
  }

  @VisibleForTesting
  void onProfileRemovedRemotely(ProfileInternal localProfile) {
    try {
      resetActiveProfile();

      profileDatastore().deleteProfile(localProfile.getId()).blockingAwait();

      currentAccount.getInternalProfiles().remove(localProfile);
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  private void onRemoteProfileNotInDatabase(ProfileInternal remoteProfile) {
    final OfflineUpdateDatastore offlineUpdateDatastore = getOfflineUpdateDatastore();
    final OfflineUpdateInternal update =
        offlineUpdateDatastore.getOfflineUpdateForProfileId(
            remoteProfile.getId(), OfflineUpdateInternal.TYPE_DELETE_PROFILE);

    // User previously tried to delete it because we do have an update for profile deletion
    if (update != null) {
      if (!profileManager.deleteProfile(remoteProfile).blockingGet()) {
        // another error, we put the offline update back
        offlineUpdateDatastore.insertOrUpdate(update);
      } else {
        resetActiveProfile();
      }
    } else {
      onProfileAddedRemotely(remoteProfile);
    }
  }

  @VisibleForTesting
  void onProfileAddedRemotely(ProfileInternal remoteProfile) {
    currentAccount.getInternalProfiles().add(remoteProfile);
    profileDatastore().addProfile(remoteProfile);
  }

  @VisibleForTesting
  void resetActiveProfile() {
    currentAccount.setOwnerProfileAsCurrent();
    accountAdapter().updateCurrentProfileId(currentAccount);
  }

  @NonNull
  @VisibleForTesting
  AccountDatastore accountAdapter() {
    return accountDatastore;
  }

  @NonNull
  @VisibleForTesting
  ProfileDatastore profileDatastore() {
    return profileDatastore;
  }

  @NonNull
  @VisibleForTesting
  OfflineUpdateDatastore getOfflineUpdateDatastore() {
    return offlineUpdateDatastore;
  }

  @VisibleForTesting
  void completeJobWithoutReschedule() {
    jobFinished(jobParameters, false);
  }

  @VisibleForTesting
  void updateLocalProfile(ProfileInternal localProfile, ProfileInternal remoteProfile) {
    profileDatastore()
        .updateProfile(localProfile.copyProfileInternal(remoteProfile))
        .blockingAwait();
  }

  @VisibleForTesting
  void refreshAvatarCache() {
    AccountInternal localAccount = kolibreeConnector.currentAccount();
    if (localAccount != null) {
      for (ProfileInternal profileInternal : localAccount.getInternalProfiles()) {
        avatarCache.cache(
            profileInternal.getId(),
            profileInternal.getPictureUrl(),
            profileInternal.getPictureLastModifier());
      }
    }
  }

  public static List<Integer> jobIds() {
    return Arrays.asList(SYNC_IMMEDIATE, SYNC_WHEN_NETWORK);
  }
}
