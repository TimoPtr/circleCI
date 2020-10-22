/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core;

import android.net.Uri;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.data.model.Brushing;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import com.kolibree.sdkws.data.model.EditProfileData;
import com.kolibree.sdkws.data.model.Practitioner;
import com.kolibree.sdkws.data.model.gopirate.GoPirateData;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData;
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.security.InvalidParameterException;
import java.util.List;
import timber.log.Timber;

/**
 * Intended for internal use
 *
 * <p>SDK clients should not use it
 */
@Keep
public class ProfileWrapper {

  private final ProfileInternal profile;
  private final InternalKolibreeConnector kolibreeConnector;
  private final BrushingsRepository brushingsRepository;
  private final ProfileRepository profileRepository;
  private final GoPirateDatastore goPirateDatastore;

  ProfileWrapper(
      long profileId,
      InternalKolibreeConnector kolibreeConnector,
      BrushingsRepository brushingsRepository,
      ProfileRepository profileRepository,
      GoPirateDatastore goPirateDatastore) {
    this.brushingsRepository = brushingsRepository;

    try {
      profile = profileRepository.getProfileLocally(profileId).blockingGet();
    } catch (Exception e) {
      throw new InvalidParameterException("No profile with id " + profileId + " does not exist");
    }

    this.profileRepository = profileRepository;
    this.goPirateDatastore = goPirateDatastore;

    this.kolibreeConnector = kolibreeConnector;
  }

  /**
   * Create a brushing
   *
   * @param data Brushing data
   */
  public void createBrushing(@NonNull CreateBrushingData data) {
    kolibreeConnector.addBrushing(data, profile);
  }

  /**
   * Create a brushing
   *
   * @param data Brushing data
   * @return [Single] with either created brushing or error if brushing creation failed
   */
  public Single<Brushing> createBrushingSingle(@NonNull CreateBrushingData data) {
    return kolibreeConnector.addBrushingSingle(data, profile);
  }

  /**
   * Create a brushing
   *
   * <p>Deprecated, use [createBrushingSingle]
   *
   * @param data Brushing data
   */
  @Deprecated
  public void createBrushingSync(@NonNull CreateBrushingData data) {
    kolibreeConnector.addBrushingSync(data, profile);
  }

  /**
   * Assign brushings to the profile wrapped in this object
   *
   * <p>Ideally this would return a Completable, but for now this project doesn't have rxjava
   *
   * @return true if the remote call succeeded, false otherwise
   */
  public boolean assignBrushings(List<Brushing> brushings) {
    return kolibreeConnector.assignBrushings(brushings, profile);
  }

  /**
   * Get profile brushing data
   *
   * @return Bushing data list
   */
  @NonNull
  public Single<List<Brushing>> getBrushing() {
    return kolibreeConnector.getBrushingList(profile.getId());
  }

  /**
   * Get the profile's last brushing session
   *
   * @return last {@link Brushing} session, or null if none
   */
  @Nullable
  public Brushing getLastBrushingSession() {
    try {
      return brushingsRepository.getLastBrushingSession(profile.getId());
    } catch (RuntimeException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get if profile is allowed to brush
   *
   * @return True if brushing is allowed, false otherwise
   */
  public boolean isAllowedToBrush() {
    return true;
  }

  /**
   * Set this profile as current for the current account
   *
   * @deprecated Use ProfileFacade.setActiveProfile
   */
  @Deprecated
  public void setCurrent() {
    kolibreeConnector.setActiveProfile(profile.getId());
  }

  /**
   * Updateprofile info
   *
   * @param data New profile info
   */
  public Single<Boolean> edit(final @NonNull EditProfileData data) {
    return kolibreeConnector.editProfile(data, profile);
  }

  /**
   * Enable Coach sound transition
   *
   * @param enable True to enable
   */
  public void enableTransitionSounds(boolean enable) {
    profile.setTransitionSounds(enable);
    profileRepository
        .updateProfileLocally(profile)
        .subscribeOn(Schedulers.io())
        .subscribe(
            __ ->
                Timber.d(
                    "%s coach transition sounds for profile id %s",
                    enable ? "Enabled" : "Disabled", profile.getId()),
            Throwable::printStackTrace);
  }

  /**
   * Set Coach music file
   *
   * @param uri File URI
   */
  public void setCoachMusicFileUri(Uri uri) {
    profile.setCoachMusic(uri != null ? uri.toString() : null);
    profileRepository
        .updateProfileLocally(profile)
        .subscribeOn(Schedulers.io())
        .subscribe(
            __ -> Timber.d("Updated coach music file for profile id %s", profile.getId()),
            Throwable::printStackTrace);
  }

  /** Mute Coach music */
  public void disableCoachMusic() {
    setCoachMusicFileUri(null);
  }

  /**
   * Get Go Pirate profile data
   *
   * @return Go Pirate data
   */
  public @Nullable Single<GoPirateData> getGoPirateData() {
    return goPirateDatastore.getData(profile.getId());
  }

  /**
   * Update Go Pirate data
   *
   * @param data New Go Pirate data
   */
  public void updateGoPirateData(final @NonNull UpdateGoPirateData data) {
    kolibreeConnector.updateGoPirateData(data, profile);
  }

  /**
   * Get associated practitioners
   *
   * @param l Callback
   */
  public void getPractitioners(final @NonNull KolibreeConnectorListener<Practitioner[]> l) {
    kolibreeConnector.getPractitioners(l, profile);
  }

  public void revokePractitioner(
      final @NonNull String token, @NonNull final KolibreeConnectorListener<Boolean> listener) {
    kolibreeConnector.revokePractitioner(token, listener);
  }
}
