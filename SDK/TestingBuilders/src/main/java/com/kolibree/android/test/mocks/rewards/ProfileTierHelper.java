/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.test.mocks.rewards;

import android.annotation.SuppressLint;
import com.kolibree.android.rewards.models.ProfileTierEntity;
import com.kolibree.android.rewards.models.TierEntity;
import com.kolibree.android.rewards.synchronization.tiers.TierApi;
import com.kolibree.android.rewards.synchronization.tiers.TiersCatalogApi;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.LocalDate;

@SuppressWarnings("KotlinInternalInJava")
public class ProfileTierHelper {
  private ProfileTierHelper() {}

  public static ProfileTierHelper create() {
    return new ProfileTierHelper();
  }

  private Set<ProfileTierEntity> profileTierEntities = new HashSet<>();
  private Set<TierEntity> tierEntities = new HashSet<>();

  public void addProfileTier(TierEntity tier, long profileId) {
    profileTierEntities.add(new ProfileTierEntity(profileId, tier.getLevel()));
  }

  public void addTier(TierEntity tierEntity) {
    tierEntities.add(tierEntity);
  }

  public Collection<ProfileTierEntity> profileTierEntities() {
    return profileTierEntities;
  }

  public TiersCatalogApi tierEntities() {
    @SuppressLint("UseSparseArrays")
    Map<Integer, TierApi> tierApiMap = new HashMap<>();

    for (TierEntity tier : tierEntities) {
      TierApi alreadyPresentTier = tierApiMap.get(tier.getLevel());
      if (alreadyPresentTier != null) {
        throw new IllegalArgumentException(
            "A Tier with level "
                + tier.getLevel()
                + " is already present ("
                + alreadyPresentTier
                + ")");
      }

      tierApiMap.put(
          tier.getLevel(),
          new TierApi(
              tier.getSmilesPerBrushing(),
              tier.getChallengesNeeded(),
              tier.getPictureUrl(),
              tier.getRank(),
              tier.getCreationDate(),
              tier.getMessage()));
    }

    return new TiersCatalogApi(tierApiMap);
  }

  public static class ProfileTierBuilder {
    public static final String DEFAULT_RANK = "Wood";
    public static final String DEFAULT_MSG = "Wood level, you Rock!";
    public static final int DEFAULT_LEVEL = 7;
    public static final int DEFAULT_SMILES_PER_BRUSHING = 8;
    public static final int DEFAULT_CHALLENGES_NEEDED = 10;

    private String rank = DEFAULT_RANK;
    private String message = DEFAULT_MSG;
    private String pictureUrl = ChallengeWithProgressBuilder.TestChallenge.DEFAULT_PICTURE_URL;
    private int level = DEFAULT_LEVEL;
    private int smilesPerBrushing = DEFAULT_SMILES_PER_BRUSHING;
    private int challengesNeeded = DEFAULT_CHALLENGES_NEEDED;
    //    private LocalDate creationDate = TrustedClock.getNowLocalDate();
    private LocalDate creationDate = LocalDate.now();

    private ProfileTierBuilder() {}

    public static ProfileTierBuilder create() {
      return new ProfileTierBuilder();
    }

    @NotNull
    public ProfileTierBuilder withPictureUrl(String pictureUrl) {
      this.pictureUrl = pictureUrl;

      return this;
    }

    @NotNull
    public ProfileTierBuilder withSmilesPerBrushing(int smilesPerBrushing) {
      this.smilesPerBrushing = smilesPerBrushing;

      return this;
    }

    @NotNull
    public ProfileTierBuilder withLevel(int level) {
      this.level = level;

      return this;
    }

    @NotNull
    public ProfileTierBuilder withRank(@NotNull String rank) {
      this.rank = rank;

      return this;
    }

    public TierEntity buildTier() {
      return new TierEntity(
          level, smilesPerBrushing, challengesNeeded, pictureUrl, rank, creationDate, message);
    }
  }
}
