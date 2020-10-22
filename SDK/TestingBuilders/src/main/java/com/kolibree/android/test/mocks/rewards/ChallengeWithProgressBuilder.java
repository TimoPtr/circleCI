/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.test.mocks.rewards;

import androidx.annotation.Nullable;
import com.kolibree.android.rewards.models.*;
import com.kolibree.android.rewards.persistence.CategoryWithChallengeProgress;
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal;
import com.kolibree.android.rewards.persistence.ChallengeWithProgressInternal;
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeCompletionDetails;
import com.kolibree.android.rewards.synchronization.challenges.ChallengesCatalogApi;
import com.kolibree.android.test.mocks.ProfileBuilder;
import java.util.*;
import org.threeten.bp.ZonedDateTime;

@SuppressWarnings("KotlinInternalInJava")
public class ChallengeWithProgressBuilder {
  public static final String DEFAULT_LANGUAGE = "EN";

  private final String language;

  private final Map<String, CategoryEntity> categoriesMap = new HashMap<>();

  private final Set<TestChallenge> challengeSet = new HashSet<>();

  private ChallengeWithProgressBuilder(String language) {
    this.language = language;
  }

  public static ChallengeWithProgressBuilder create() {
    return create(DEFAULT_LANGUAGE);
  }

  public static ChallengeWithProgressBuilder create(String language) {
    return new ChallengeWithProgressBuilder(language);
  }

  public ChallengesCatalogApi buildChallengesCatalog() {
    return new ChallengesCatalogApi(new ArrayList<>(categoriesMap.values()), language);
  }

  public ChallengeProgressProfileCatalogInternal buildChallengeProgressCatalog() {
    ChallengeProgressProfileCatalogInternal catalog = new ChallengeProgressProfileCatalogInternal();

    for (TestChallenge testChallenge : challengeSet) {
      catalog.add(testChallenge.toChallengeWithProgressEntity());
    }

    return catalog;
  }

  public ChallengeWithProgressBuilder withChallenges(
      String categoryName, TestChallenge... challenges) {
    for (TestChallenge challenge : challenges) {
      saveChallenge(categoryName, challenge);
    }

    return this;
  }

  private void saveChallenge(String categoryName, TestChallenge challenge) {
    TestChallenge challengeToSave =
        challenge.withCategory(categoryName).withProfileId(challenge.profileId);

    getOrPutCategory(categoryName).getChallenges().add(challengeToSave.toChallengeEntity());

    challengeSet.add(challengeToSave);
  }

  private CategoryEntity getOrPutCategory(String category) {
    if (!categoriesMap.containsKey(category)) {
      categoriesMap.put(category, new CategoryEntity(category, new ArrayList<>()));
    }

    return categoriesMap.get(category);
  }

  @Nullable
  public CategoryEntity getCategoryEntity(String categoryName) {
    CategoryEntity category = categoriesMap.get(categoryName);
    if (category != null) {
      return category.copy(category.getName(), category.getChallenges());
    }

    return null;
  }

  @Nullable
  public CategoryWithProgress getCategoryWithProgress(String categoryName) {
    CategoryEntity category = categoriesMap.get(categoryName);
    if (category != null) {
      List<ChallengeWithProgress> categoryChallenges = new ArrayList<>();
      for (TestChallenge testChallenge : challengeSet) {
        if (testChallenge.category.equals(categoryName)) {
          categoryChallenges.add(testChallenge.toChallengeWithProgress());
        }
      }

      return new CategoryWithChallengeProgress(categoryName, categoryChallenges);
    }

    return null;
  }

  public static class TestChallenge {
    private static int CHALLENGE_ID = 1;
    public static final String DEFAULT_PICTURE_URL = "";
    public static final String DEFAULT_DESCRIPTION = "default description";

    private final String name, description, pictureUrl;
    private final long challengeId;

    private String category;
    private long profileId = ProfileBuilder.DEFAULT_ID;
    private int progress;
    private ZonedDateTime completionTime;
    private String action;
    private int smilesReward;
    private ChallengeCompletionDetails completionDetails;

    public TestChallenge(String name) {
      this(name, "");
    }

    public TestChallenge(String name, String pictureUrl) {
      this(name, pictureUrl, DEFAULT_DESCRIPTION);
    }

    public TestChallenge(String name, String pictureUrl, String description) {
      this(name, pictureUrl, description, CHALLENGE_ID++);
    }

    public TestChallenge(String name, String pictureUrl, String description, long challengeId) {
      this.name = name;
      this.description = description;
      this.pictureUrl = pictureUrl;
      this.challengeId = challengeId;
    }

    public TestChallenge withCategory(String categoryName) {
      category = categoryName;

      return this;
    }

    public TestChallenge withCompletionDetails(int completion, int rules) {
      completionDetails = new ChallengeCompletionDetails(completion, rules);

      return this;
    }

    public TestChallenge withProfileId(long profileId) {
      this.profileId = profileId;

      return this;
    }

    public TestChallenge withProgress(int progress) {
      this.progress = progress;

      return this;
    }

    public TestChallenge withCompletionTime(@Nullable ZonedDateTime completionTime) {
      this.completionTime = completionTime;

      return this;
    }

    public TestChallenge withAction(@Nullable String action) {
      this.action = action;

      return this;
    }

    public TestChallenge withSmilesReward(int smilesReward) {
      this.smilesReward = smilesReward;

      return this;
    }

    ChallengeWithProgress toChallengeWithProgress() {
      return new ChallengeWithProgressInternal(
          challengeId,
          name,
          description,
          pictureUrl,
          category,
          "",
          smilesReward,
          progress,
          completionTime,
          profileId,
          action,
          completionDetails);
    }

    ChallengeProgressEntity toChallengeWithProgressEntity() {
      return new ChallengeProgressEntity(
          challengeId, profileId, completionTime, completionDetails, progress);
    }

    ChallengeEntity toChallengeEntity() {
      return new ChallengeEntity(
          challengeId, name, "", description, pictureUrl, smilesReward, action, category);
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public String getPictureUrl() {
      return pictureUrl;
    }

    public long getChallengeId() {
      return challengeId;
    }

    public String getCategory() {
      return category;
    }

    public long getProfileId() {
      return profileId;
    }

    public int getProgress() {
      return progress;
    }

    public ZonedDateTime getCompletionTime() {
      return completionTime;
    }

    public String getAction() {
      return action;
    }
  }
}
