package com.kolibree.heuristic;

import static com.kolibree.kml.MouthZone16.*;

import androidx.annotation.Keep;
import androidx.annotation.VisibleForTesting;
import com.kolibree.kml.MouthZone16;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Keep
public class HeuristicOfflineBrushingUtils {

  static final int THRESHOLD = 60;

  private final WeightMouthZones weightMouthZones;

  public HeuristicOfflineBrushingUtils() {
    weightMouthZones = new WeightMouthZones();
    initWeightMouthZones();
  }

  private void initWeightMouthZones() {
    weightMouthZones
        .forZone(LoMolLeOcc)
        .add(LoMolLeOcc, 1f)
        .add(LoMolRiOcc, 1f)
        .add(LoIncInt, 0.5f);

    weightMouthZones
        .forZone(LoMolLeExt)
        .add(LoMolLeExt, 1f)
        .add(LoMolRiInt, 0.5f)
        .add(LoIncExt, 1f);

    weightMouthZones
        .forZone(LoMolLeInt)
        .add(LoMolLeInt, 0.5f)
        .add(LoMolRiExt, 1f)
        .add(LoIncExt, 1f);

    weightMouthZones
        .forZone(LoMolRiOcc)
        .add(LoMolRiOcc, 1f)
        .add(LoMolLeOcc, 1f)
        .add(LoIncInt, 0.5f);

    weightMouthZones
        .forZone(LoMolRiExt)
        .add(LoMolRiExt, 1f)
        .add(LoMolLeInt, 0.5f)
        .add(LoIncExt, 1f);

    weightMouthZones
        .forZone(LoMolRiInt)
        .add(LoMolRiInt, 0.5f)
        .add(LoIncExt, 1f)
        .add(LoMolLeExt, 1f);

    weightMouthZones
        .forZone(LoIncExt)
        .add(LoIncExt, 1f)
        .add(LoMolRiExt, 0.5f)
        .add(LoMolLeExt, 0.5f);

    weightMouthZones
        .forZone(LoIncInt)
        .add(LoIncInt, 1f)
        .add(LoMolRiOcc, 0.5f)
        .add(LoMolLeOcc, 0.5f);

    weightMouthZones
        .forZone(UpMolLeOcc)
        .add(UpMolLeOcc, 1f)
        .add(UpMolRiOcc, 1f)
        .add(UpIncInt, 0.5f);

    weightMouthZones
        .forZone(UpMolLeExt)
        .add(UpMolLeExt, 1f)
        .add(UpMolRiInt, 0.5f)
        .add(UpIncExt, 1f);

    weightMouthZones
        .forZone(UpMolLeInt)
        .add(UpMolLeInt, 0.5f)
        .add(UpMolRiExt, 1f)
        .add(UpIncExt, 1f);

    weightMouthZones
        .forZone(UpMolRiOcc)
        .add(UpMolRiOcc, 1f)
        .add(UpMolLeOcc, 1f)
        .add(UpIncInt, 0.5f);

    weightMouthZones
        .forZone(UpMolRiExt)
        .add(UpMolRiExt, 1f)
        .add(UpMolLeInt, 0.5f)
        .add(UpIncExt, 1f);

    weightMouthZones
        .forZone(UpMolRiInt)
        .add(UpMolRiInt, 0.5f)
        .add(UpMolLeExt, 1f)
        .add(UpIncExt, 1f);

    weightMouthZones
        .forZone(UpIncExt)
        .add(UpIncExt, 1f)
        .add(UpMolRiExt, 0.5f)
        .add(UpMolLeExt, 0.5f);

    weightMouthZones
        .forZone(UpIncInt)
        .add(UpIncExt, 1f)
        .add(UpMolRiOcc, 0.5f)
        .add(UpMolLeOcc, 0.5f);
  }

  public Map<MouthZone16, Integer> adjustTime(Map<MouthZone16, Integer> zones) {
    return adjustTime(zones, THRESHOLD);
  }

  @VisibleForTesting
  Map<MouthZone16, Integer> adjustTime(Map<MouthZone16, Integer> zones, final int threshold) {
    List<TimeMouthZone> timeMouthZones = toTimeMouthZones(zones);

    initTimeAfter(timeMouthZones, threshold);

    sortDescending(timeMouthZones);

    Map<MouthZone16, TimeMouthZone> mapTimeMouthZones = mapTimeMouthZoneToZone(timeMouthZones);

    for (TimeMouthZone timeMouthZone : timeMouthZones) {
      MouthZone16 zone = timeMouthZone.zone;
      if (timeMouthZone.timeBefore > threshold) {

        float timeDiff = timeMouthZone.timeBefore - threshold;
        float weightSum = weightSum(zone);

        for (WeightMouthZone weightZone : confusionWeightZonesFor(zone)) {
          float percent = weightZone.weight / weightSum;
          int timeToRedistribution = (int) (timeDiff * percent);
          TimeMouthZone confusedZone = mapTimeMouthZones.get(weightZone.zone);
          confusedZone.incTimeAfter(timeToRedistribution);
        }
      }
    }

    return toAdjustedZone(mapTimeMouthZones);
  }

  private void initTimeAfter(List<TimeMouthZone> zones, final int threshold) {
    for (TimeMouthZone zone : zones) {
      if (zone.timeBefore > threshold) {
        zone.timeAfter = threshold;
      } else {
        zone.timeAfter = zone.timeBefore;
      }
    }
  }

  private Map<MouthZone16, Integer> toAdjustedZone(Map<MouthZone16, TimeMouthZone> mapZone) {
    Map<MouthZone16, Integer> adjustedZones = new HashMap<>();
    for (MouthZone16 zone : MouthZone16.values()) {
      adjustedZones.put(zone, 0);
    }
    for (Entry<MouthZone16, TimeMouthZone> entry : mapZone.entrySet()) {
      MouthZone16 zone = entry.getKey();
      int time = entry.getValue().timeAfter;
      adjustedZones.put(zone, time);
    }
    return adjustedZones;
  }

  private Map<MouthZone16, TimeMouthZone> mapTimeMouthZoneToZone(
      List<TimeMouthZone> timeMouthZones) {
    Map<MouthZone16, TimeMouthZone> mapTimeMouthZones = new HashMap<>();
    for (TimeMouthZone timeMouthZone : timeMouthZones) {
      mapTimeMouthZones.put(timeMouthZone.zone, timeMouthZone);
    }
    return mapTimeMouthZones;
  }

  @VisibleForTesting
  void sortDescending(List<TimeMouthZone> zones) {
    Collections.sort(zones, (first, second) -> -Float.compare(first.timeBefore, second.timeBefore));
  }

  private List<TimeMouthZone> toTimeMouthZones(Map<MouthZone16, Integer> zones) {
    for (MouthZone16 zone : MouthZone16.values()) {
      if (!zones.containsKey(zone)) {
        zones.put(zone, 0);
      }
    }
    List<TimeMouthZone> timeMouthZones = new ArrayList<>();
    for (Entry<MouthZone16, Integer> entry : zones.entrySet()) {
      timeMouthZones.add(new TimeMouthZone(entry.getKey(), entry.getValue()));
    }
    return timeMouthZones;
  }

  @VisibleForTesting
  float weightSum(MouthZone16 zone) {
    Map<MouthZone16, Float> map = weightMouthZones.weightMap.get(zone);
    float sum = 0;
    for (Float weight : map.values()) {
      sum += weight;
    }
    return sum;
  }

  @VisibleForTesting
  List<WeightMouthZone> confusionWeightZonesFor(MouthZone16 zone) {
    Map<MouthZone16, Float> map = weightMouthZones.weightMap.get(zone);
    List<WeightMouthZone> weightZones = new ArrayList<>();
    for (Entry<MouthZone16, Float> entry : map.entrySet()) {
      weightZones.add(new WeightMouthZone(entry.getKey(), entry.getValue()));
    }
    return weightZones;
  }

  @VisibleForTesting
  static class TimeMouthZone {

    private final MouthZone16 zone;
    private final int timeBefore;
    private int timeAfter;

    TimeMouthZone(MouthZone16 zone, int timeBefore) {
      this.zone = zone;
      this.timeBefore = timeBefore;
    }

    void incTimeAfter(int valueToInc) {
      timeAfter += valueToInc;
    }

    MouthZone16 zone() {
      return zone;
    }
  }

  @VisibleForTesting
  static class WeightMouthZone {

    private final MouthZone16 zone;
    private final float weight;

    WeightMouthZone(MouthZone16 zone, float weight) {
      this.zone = zone;
      this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof WeightMouthZone)) {
        return false;
      }
      WeightMouthZone that = (WeightMouthZone) o;
      return Float.compare(that.weight, weight) == 0 && zone == that.zone;
    }

    @Override
    public int hashCode() {
      return Objects.hash(zone, weight);
    }
  }

  static class WeightMouthZones {

    private Map<MouthZone16, Map<MouthZone16, Float>> weightMap;

    private WeightMouthZones() {
      weightMap = new HashMap<>();
    }

    WeightMouthZonesBuilder forZone(MouthZone16 zone) {
      Map<MouthZone16, Float> internalWeightMap = new HashMap<>();
      weightMap.put(zone, internalWeightMap);

      return new WeightMouthZonesBuilder(internalWeightMap);
    }

    static class WeightMouthZonesBuilder {

      private Map<MouthZone16, Float> map;

      WeightMouthZonesBuilder(Map<MouthZone16, Float> map) {
        this.map = map;
      }

      WeightMouthZonesBuilder add(MouthZone16 zone, float weight) {
        map.put(zone, weight);
        return this;
      }
    }
  }
}
