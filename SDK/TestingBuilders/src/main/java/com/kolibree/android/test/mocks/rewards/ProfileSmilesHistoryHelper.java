/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks.rewards;

import static com.kolibree.android.rewards.models.SmilesHistoryEventKt.EVENT_TYPE_SMILES_REDEEMED;

import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity;
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistoryApi;
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistoryApiWithProfileId;
import com.kolibree.android.rewards.synchronization.profilesmileshistory.SmilesHistoryEventApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.threeten.bp.ZonedDateTime;

@SuppressWarnings("KotlinInternalInJava")
public class ProfileSmilesHistoryHelper {
  public static ProfileSmilesHistoryHelper create() {
    return new ProfileSmilesHistoryHelper();
  }

  private ProfileSmilesHistoryHelper() {}

  private Set<SmilesHistoryEventEntity> smilesHistoryEventEntities = new HashSet<>();
  private long profileId;

  public ProfileSmilesHistoryHelper setProfileId(long profileId) {
    this.profileId = profileId;
    return this;
  }

  public ProfileSmilesHistoryHelper addHistoryEvent(SmilesHistoryEventEntity eventEntity) {
    smilesHistoryEventEntities.add(eventEntity);
    return this;
  }

  public ProfileSmilesHistoryApiWithProfileId getSmilesHistoryEventEntities() {
    List<SmilesHistoryEventApi> eventApis = new ArrayList<>();

    for (SmilesHistoryEventEntity smilesHistoryEventEntity : smilesHistoryEventEntities) {
      eventApis.add(
          new SmilesHistoryEventApi(
              smilesHistoryEventEntity.getMessage(),
              smilesHistoryEventEntity.getSmiles(),
              smilesHistoryEventEntity.getCreationTime(),
              smilesHistoryEventEntity.getEventType(),
              smilesHistoryEventEntity.getChallengeId(),
              smilesHistoryEventEntity.getBrushingId(),
              smilesHistoryEventEntity.getBrushingType(),
              smilesHistoryEventEntity.getTierLevel(),
              smilesHistoryEventEntity.getRewardsId(),
              smilesHistoryEventEntity.getRelatedProfileId()));
    }

    return new ProfileSmilesHistoryApiWithProfileId(
        profileId, new ProfileSmilesHistoryApi(eventApis));
  }

  public static class ProfileSmilesHistoryBuilder {
    public static final String DEFAULT_MESSAGE = "message";
    public static final int DEFAULT_SMILES = 10;
    public static final ZonedDateTime DEFAULT_CREATION_TIME = TrustedClock.getNowZonedDateTime();
    public static final String DEFAULT_EVENT_TYPE = EVENT_TYPE_SMILES_REDEEMED;
    public static final String DEFAULT_BRUSHING_TYPE = "Coach";

    private long id = 0;
    private String message = DEFAULT_MESSAGE;
    private int smiles = DEFAULT_SMILES;
    private ZonedDateTime creationTime = DEFAULT_CREATION_TIME;
    private Long profileId = null;
    private String eventType = DEFAULT_EVENT_TYPE;
    private Long challengeId = null;
    private Long brushingId = null;
    private String brushingType = DEFAULT_BRUSHING_TYPE;
    private Integer tierLevel = null;
    private Long rewardsId = null;
    private Long relatedProfileId = null;

    private ProfileSmilesHistoryBuilder() {}

    public static ProfileSmilesHistoryBuilder create() {
      return new ProfileSmilesHistoryBuilder();
    }

    public ProfileSmilesHistoryBuilder withId(long id) {
      this.id = id;
      return this;
    }

    public ProfileSmilesHistoryBuilder withMessage(String message) {
      this.message = message;
      return this;
    }

    public ProfileSmilesHistoryBuilder withSmiles(int smiles) {
      this.smiles = smiles;
      return this;
    }

    public ProfileSmilesHistoryBuilder withCreationTime(ZonedDateTime creationTime) {
      this.creationTime = creationTime;
      return this;
    }

    public ProfileSmilesHistoryBuilder withProfileId(Long profileId) {
      this.profileId = profileId;
      return this;
    }

    public ProfileSmilesHistoryBuilder withEventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public ProfileSmilesHistoryBuilder withChallengeId(Long challengeId) {
      this.challengeId = challengeId;
      return this;
    }

    public ProfileSmilesHistoryBuilder withBrushingId(Long brushingId) {
      this.brushingId = brushingId;
      return this;
    }

    public ProfileSmilesHistoryBuilder withBrushingType(String brushingType) {
      this.brushingType = brushingType;
      return this;
    }

    public ProfileSmilesHistoryBuilder withTierId(Integer tierLevel) {
      this.tierLevel = tierLevel;
      return this;
    }

    public ProfileSmilesHistoryBuilder withRewardsId(Long rewardsId) {
      this.rewardsId = rewardsId;
      return this;
    }

    public ProfileSmilesHistoryBuilder withRelatedProfileId(Long relatedProfileId) {
      this.relatedProfileId = relatedProfileId;
      return this;
    }

    public SmilesHistoryEventEntity buildSmilesHistoryEvent() {
      return new SmilesHistoryEventEntity(
          id,
          smiles,
          message,
          creationTime,
          profileId,
          eventType,
          challengeId,
          brushingId,
          brushingType,
          tierLevel,
          rewardsId,
          relatedProfileId);
    }
  }
}
