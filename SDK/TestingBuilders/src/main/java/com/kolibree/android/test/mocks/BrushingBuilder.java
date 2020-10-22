/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks;

import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.GameApiConstants;
import com.kolibree.sdkws.data.model.Brushing;
import org.threeten.bp.OffsetDateTime;

/** Created by miguelaragues on 31/1/18. */
public class BrushingBuilder {

  public static final int DEFAULT_DURATION = 90;
  public static final int DEFAULT_GOAL_DURATION = 120;
  public static final long DEFAULT_PROFILE_ID = 42L;

  public static final String DEFAULT_PROCESSED_DATA =
      "{\n"
          + "\t\t\t\"LoIncExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 34,\n"
          + "\t\t\t\t\t\"effective_time\": 3\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolLeOcc\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 40,\n"
          + "\t\t\t\t\t\"effective_time\": 2\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolRiOcc\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 84,\n"
          + "\t\t\t\t\t\"effective_time\": 6\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpIncInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 90,\n"
          + "\t\t\t\t\t\"effective_time\": 6\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolLeInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": []\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolLeExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 9,\n"
          + "\t\t\t\t\t\"effective_time\": 6\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoIncInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 96,\n"
          + "\t\t\t\t\t\"effective_time\": 8\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpIncExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": []\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolRiInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 71,\n"
          + "\t\t\t\t\t\"effective_time\": 1\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolRiInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": []\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolRiExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 15,\n"
          + "\t\t\t\t\t\"effective_time\": 6\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolRiOcc\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 65,\n"
          + "\t\t\t\t\t\"effective_time\": 1\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolLeOcc\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 59,\n"
          + "\t\t\t\t\t\"effective_time\": 3\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolRiExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 21,\n"
          + "\t\t\t\t\t\"effective_time\": 6\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"UpMolLeExt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": [{\n"
          + "\t\t\t\t\t\"pass_datetime\": 0,\n"
          + "\t\t\t\t\t\"effective_time\": 1\n"
          + "\t\t\t\t}]\n"
          + "\t\t\t},\n"
          + "\t\t\t\"LoMolLeInt\": {\n"
          + "\t\t\t\t\"expected_time\": 6,\n"
          + "\t\t\t\t\"passes\": []\n"
          + "\t\t\t}\n"
          + "\t\t}";

  private long duration = DEFAULT_DURATION;
  private int goalDuration = DEFAULT_GOAL_DURATION, coins, points;
  private OffsetDateTime dateTime = TrustedClock.getNowOffsetDateTime();
  private String processedData = "";
  private Long kolibreeId;
  private String game = GameApiConstants.GAME_COACH;
  private String mac = KLTBConnectionBuilder.DEFAULT_MAC;
  private Long profileId = DEFAULT_PROFILE_ID;

  private BrushingBuilder() {}

  public static BrushingBuilder create() {
    return new BrushingBuilder();
  }

  public BrushingBuilder withDuration(long duration) {
    this.duration = duration;

    return this;
  }

  public BrushingBuilder withProfileId(long profileId) {
    this.profileId = profileId;

    return this;
  }

  public BrushingBuilder withDateTime(OffsetDateTime dateTime) {
    this.dateTime = dateTime;

    return this;
  }

  public BrushingBuilder withProcessedData(String processedData) {
    this.processedData = processedData;

    return this;
  }

  public BrushingBuilder withGoalDuration(int durationGoal) {
    this.goalDuration = durationGoal;

    return this;
  }

  public BrushingBuilder withGame(String game) {
    this.game = game;

    return this;
  }

  public BrushingBuilder withMac(String mac) {
    this.mac = mac;

    return this;
  }

  public BrushingBuilder withKolibreeId(Long id) {
    this.kolibreeId = id;

    return this;
  }

  public Brushing build() {
    return new Brushing(
        duration,
        goalDuration,
        dateTime,
        coins,
        points,
        processedData,
        profileId,
        kolibreeId,
        game,
        mac);
  }
}
