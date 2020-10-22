/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.kolibree.android.processedbrushings.models.ZoneData;
import com.kolibree.android.processedbrushings.models.ZonePass;
import com.kolibree.heuristic.HeuristicOfflineBrushingUtils;
import com.kolibree.kml.MouthZone16;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Legacy class that generates the Json to be sent to backend v1 brushings API
 *
 * <p>Originally called MetricsHelper, it was moved to this module and hidden from the rest of the
 * application
 */
final class LegacyProcessedDataGenerator {

  private LegacyProcessedDataGenerator() {}

  @Nullable
  static Map<MouthZone16, ZoneData> computeProcessedDataToMap(
      @NonNull RecordedSession record, int goalTime) {
    if (record.getEvents() == null) {
      return null;
    }

    Map<MouthZone16, ZoneData> sortedPasses = sort(record, goalTime);
    Map<MouthZone16, Integer> times = merge(sortedPasses);
    Map<MouthZone16, Integer> increased = increase(times);
    Map<MouthZone16, Integer> redistributed = redistributePasses(increased);

    return format(redistributed, goalTime);
  }

  @NonNull
  static HashMap<MouthZone16, ZoneData> computeProcessedDataToMap(
      @NonNull Map<MouthZone16, List<ZonePass>> data, int goalTime) {
    final HashMap<MouthZone16, ZoneData> sortedPasses = new HashMap<>();

    for (MouthZone16 z16 : MouthZone16.values()) {
      sortedPasses.put(z16, ZoneData.createFromGoalBrushingTime(goalTime, data.get(z16)));
    }

    return sortedPasses;
  }

  @Nullable
  static String computeProcessedData(@NonNull RecordedSession record, int goalTime) {
    Map<MouthZone16, ZoneData> formatted = computeProcessedDataToMap(record, goalTime);

    if (formatted == null) {
      return null;
    }

    return new Gson().toJson(formatted);
  }

  @VisibleForTesting
  static Map<MouthZone16, ZoneData> sort(@NonNull RecordedSession record, int goalTime) {
    Map<MouthZone16, ZoneData> sortedPasses = new HashMap<>();

    for (MouthZone16 z16 : MouthZone16.values()) {
      sortedPasses.put(z16, ZoneData.createFromExpectedTime(goalTime));
    }

    RecordedSession.Event last = null;

    for (RecordedSession.Event e : record.getEvents()) {
      if (last != null && last.vibrator) {
        ZonePass currentPass = new ZonePass(last.dateTime, e.dateTime - last.dateTime);
        sortedPasses.put(last.zone, sortedPasses.get(last.zone).addPass(currentPass));
      }

      last = e;
    }
    return sortedPasses;
  }

  private static Map<MouthZone16, Integer> redistributePasses(Map<MouthZone16, Integer> zones) {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    return utils.adjustTime(zones);
  }

  @VisibleForTesting
  static Map<MouthZone16, Integer> increase(Map<MouthZone16, Integer> zones) {
    Map<MouthZone16, Integer> increased = new HashMap<>();
    for (Entry<MouthZone16, Integer> entry : zones.entrySet()) {
      int duration = entry.getValue();
      MouthZone16 zone = entry.getKey();
      int increasedDuration = increaseBy15Percent(duration);
      increased.put(zone, increasedDuration);
    }
    return increased;
  }

  @VisibleForTesting
  static Map<MouthZone16, Integer> merge(Map<MouthZone16, ZoneData> zones) {
    Map<MouthZone16, Integer> times = new HashMap<>();
    for (Entry<MouthZone16, ZoneData> entry : zones.entrySet()) {
      int totalDuration = totalDuration(entry.getValue());
      times.put(entry.getKey(), totalDuration);
    }
    return times;
  }

  @VisibleForTesting
  static Map<MouthZone16, ZoneData> format(Map<MouthZone16, Integer> times, int goalTime) {
    int beginTime = 0;
    Map<MouthZone16, ZoneData> formatted = new HashMap<>();
    for (MouthZone16 zone : MouthZone16.values()) {
      ZoneData zoneData = ZoneData.createFromExpectedTime(goalTime);
      if (times.containsKey(zone)) {
        int duration = times.get(zone);
        zoneData = zoneData.addPass(new ZonePass(beginTime, duration));
        beginTime += duration;
      }
      formatted.put(zone, zoneData);
    }
    return formatted;
  }

  @VisibleForTesting
  static int increaseBy15Percent(int duration) {
    return (int) Math.ceil((115f * duration) / 100);
  }

  @VisibleForTesting
  static int totalDuration(ZoneData zoneData) {
    int totalDuration = 0;
    for (ZonePass pass : zoneData.getPasses()) {
      totalDuration += pass.getDurationTenthSecond();
    }
    return totalDuration;
  }

  @NonNull
  static String computeProcessedData(@NonNull Map<MouthZone16, List<ZonePass>> data, int goalTime) {
    return new Gson().toJson(computeProcessedDataToMap(data, goalTime));
  }

  static int getEffectiveBrushingTimeSeconds(@NonNull Map<MouthZone16, List<ZonePass>> data) {
    int sum = 0;

    for (List<ZonePass> list : data.values()) {
      for (ZonePass pass : list) {
        sum += pass.getDurationTenthSecond();
      }
    }

    return sum / 10; // Convert to seconds
  }
}
