package com.kolibree.heuristic;

import static com.kolibree.kml.MouthZone16.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.kolibree.heuristic.HeuristicOfflineBrushingUtils.TimeMouthZone;
import com.kolibree.heuristic.HeuristicOfflineBrushingUtils.WeightMouthZone;
import com.kolibree.kml.MouthZone16;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class HeuristicOfflineBrushingUtilsTest {

  private static final float DELTA = 0.00001f;

  /** Example can be found here: https://confluence.kolibree.com/display/PROD/Check-Up+Heuristic */
  @Test
  public void testAdjustTime() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    Map<MouthZone16, Integer> zones = new HashMap<>();
    zones.put(LoIncInt, 150);
    int threshold = 100;
    Map<MouthZone16, Integer> adjustedZones = utils.adjustTime(zones, threshold);
    // 50 from LoIncInt will be redistributed to:
    // * LoIncInt -> 25
    // * LoMolRiOcc -> 12
    // * LoMolLeOcc -> 12
    assertEquals(threshold + 25, adjustedZones.get(LoIncInt), DELTA);
    assertEquals(12, adjustedZones.get(LoMolLeOcc), DELTA);
    assertEquals(12, adjustedZones.get(LoMolRiOcc), DELTA);
  }

  @Test
  public void testAdjustTime_allInTheSameConfusionZones() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    Map<MouthZone16, Integer> zones = new HashMap<>();
    zones.put(LoMolLeOcc, 150);
    zones.put(LoMolRiOcc, 120);
    zones.put(LoIncInt, 80);

    int threshold = 100;
    Map<MouthZone16, Integer> adjustedZones = utils.adjustTime(zones, threshold);
    // 50 from LoMolLeOcc will be redistributed to:
    // * LoMolLeOcc -> 20
    // * LoMolRiOcc -> 20
    // * LoIncInt -> 10

    // 20 from LoMolRiOcc will be redistributed to:
    // * LoMolRiOcc -> 8
    // * LoMolLeOcc -> 8
    // * LoIncInt -> 4
    assertEquals(threshold + 20 + 8, adjustedZones.get(LoMolLeOcc), DELTA);
    assertEquals(threshold + 20 + 8, adjustedZones.get(LoMolRiOcc), DELTA);
    assertEquals(80 + 10 + 4, adjustedZones.get(LoIncInt), DELTA);
  }

  @Test
  public void testAdjustTime_differentConfusionZones() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    Map<MouthZone16, Integer> zones = new HashMap<>();
    zones.put(LoMolRiInt, 125);
    zones.put(LoIncExt, 140);
    zones.put(LoMolLeExt, 0);
    zones.put(LoMolRiExt, 70);
    int threshold = 100;
    Map<MouthZone16, Integer> adjustedZones = utils.adjustTime(zones, threshold);
    // 25 from LoMolRiInt will be distributed to:
    // * LoMolRiInt -> 5
    // * LoIncExt -> 10
    // * LoMolLeExt -> 10

    // 40 from LoIncExt will be distributed to:
    // LoIncExt -> 20
    // LoMolRiExt -> 10
    // LoMolLeExt -> 10
    assertEquals(threshold + 5, adjustedZones.get(LoMolRiInt), DELTA);
    assertEquals(threshold + 20 + 10, adjustedZones.get(LoIncExt), DELTA);
    assertEquals(10 + 10, adjustedZones.get(LoMolLeExt), DELTA);
    assertEquals(70 + 10, adjustedZones.get(LoMolRiExt), DELTA);
  }

  @Test
  public void testAdjustTime_belowThreshold() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    Map<MouthZone16, Integer> zones = new HashMap<>();
    zones.put(LoIncExt, 100);
    zones.put(LoIncInt, 110);
    zones.put(LoMolLeInt, 120);
    zones.put(UpMolLeExt, 130);
    zones.put(LoMolRiOcc, 140);
    zones.put(UpMolLeOcc, 150);
    Map<MouthZone16, Integer> adjustedZones = utils.adjustTime(zones, 200);

    assertEquals(100, adjustedZones.get(LoIncExt), DELTA);
    assertEquals(110, adjustedZones.get(LoIncInt), DELTA);
    assertEquals(120, adjustedZones.get(LoMolLeInt), DELTA);
    assertEquals(130, adjustedZones.get(UpMolLeExt), DELTA);
    assertEquals(140, adjustedZones.get(LoMolRiOcc), DELTA);
    assertEquals(150, adjustedZones.get(UpMolLeOcc), DELTA);
  }

  @Test
  public void testSortDescending() {
    List<TimeMouthZone> zones = new ArrayList<>();
    zones.add(new TimeMouthZone(LoIncExt, 150));
    zones.add(new TimeMouthZone(LoIncInt, 140));
    zones.add(new TimeMouthZone(LoMolLeInt, 300));
    zones.add(new TimeMouthZone(UpMolLeExt, 70));
    zones.add(new TimeMouthZone(LoMolRiOcc, 120));
    Collections.shuffle(zones);

    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    utils.sortDescending(zones);

    assertEquals(LoMolLeInt, zones.get(0).zone());
    assertEquals(LoIncExt, zones.get(1).zone());
    assertEquals(LoIncInt, zones.get(2).zone());
    assertEquals(LoMolRiOcc, zones.get(3).zone());
    assertEquals(UpMolLeExt, zones.get(4).zone());
  }

  /**
   * Re-distribution weights can be found here:
   * https://confluence.kolibree.com/pages/viewpage.action?spaceKey=PROD&title=Check-Up+Heuristic
   */
  @Test
  public void testWeightSum() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    assertEquals(2.5f, utils.weightSum(LoMolLeOcc));
    assertEquals(2.5f, utils.weightSum(LoMolLeExt));
    assertEquals(2.5f, utils.weightSum(LoMolLeInt));
    assertEquals(2.5f, utils.weightSum(LoMolRiOcc));
    assertEquals(2.5f, utils.weightSum(LoMolRiExt));
    assertEquals(2.5f, utils.weightSum(LoMolRiInt));
    assertEquals(2f, utils.weightSum(LoIncExt));
    assertEquals(2f, utils.weightSum(LoIncInt));
    assertEquals(2.5f, utils.weightSum(UpMolLeOcc));
    assertEquals(2.5f, utils.weightSum(UpMolLeExt));
    assertEquals(2.5f, utils.weightSum(UpMolLeInt));
    assertEquals(2.5f, utils.weightSum(UpMolRiOcc));
    assertEquals(2.5f, utils.weightSum(UpMolRiExt));
    assertEquals(2.5f, utils.weightSum(UpMolRiInt));
    assertEquals(2f, utils.weightSum(UpIncExt));
    assertEquals(2f, utils.weightSum(UpIncInt));
  }

  @Test
  public void zoneLoMolLeOcc_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolLeOcc);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolLeOcc, 1f);
    assertWeight(weights, LoMolRiOcc, 1f);
    assertWeight(weights, LoIncInt, 0.5f);
  }

  @Test
  public void zoneLoMolLeExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolLeExt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolLeExt, 1f);
    assertWeight(weights, LoMolRiInt, 0.5f);
    assertWeight(weights, LoIncExt, 1f);
  }

  @Test
  public void zoneLoMolLeInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolLeInt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolLeInt, 0.5f);
    assertWeight(weights, LoMolRiExt, 1f);
    assertWeight(weights, LoIncExt, 1f);
  }

  @Test
  public void zoneLoMolRiOcc_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolRiOcc);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolRiOcc, 1f);
    assertWeight(weights, LoMolLeOcc, 1f);
    assertWeight(weights, LoIncInt, 0.5f);
  }

  @Test
  public void zoneLoMolRiExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolRiExt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolRiExt, 1f);
    assertWeight(weights, LoMolLeInt, 0.5f);
    assertWeight(weights, LoIncExt, 1f);
  }

  @Test
  public void zoneLoMolRiInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoMolRiInt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoMolRiInt, 0.5f);
    assertWeight(weights, LoIncExt, 1f);
    assertWeight(weights, LoMolLeExt, 1f);
  }

  @Test
  public void zoneLoIncExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoIncExt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoIncExt, 1f);
    assertWeight(weights, LoMolRiExt, 0.5f);
    assertWeight(weights, LoMolLeExt, 0.5f);
  }

  @Test
  public void zoneLoIncInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(LoIncInt);
    assertEquals(3, weights.size());
    assertWeight(weights, LoIncInt, 1f);
    assertWeight(weights, LoMolRiOcc, 0.5f);
    assertWeight(weights, LoMolLeOcc, 0.5f);
  }

  @Test
  public void zoneUpMolLeOcc_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolLeOcc);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolLeOcc, 1f);
    assertWeight(weights, UpMolRiOcc, 1f);
    assertWeight(weights, UpIncInt, 0.5f);
  }

  @Test
  public void zoneUpMolLeExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolLeExt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolLeExt, 1f);
    assertWeight(weights, UpMolRiInt, 0.5f);
    assertWeight(weights, UpIncExt, 1f);
  }

  @Test
  public void zoneUpMolLeInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolLeInt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolLeInt, 0.5f);
    assertWeight(weights, UpMolRiExt, 1f);
    assertWeight(weights, UpIncExt, 1f);
  }

  @Test
  public void zoneUpMolRiOcc_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolRiOcc);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolRiOcc, 1f);
    assertWeight(weights, UpMolLeOcc, 1f);
    assertWeight(weights, UpIncInt, 0.5f);
  }

  @Test
  public void zoneUpMolRiExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolRiExt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolRiExt, 1f);
    assertWeight(weights, UpMolLeInt, 0.5f);
    assertWeight(weights, UpIncExt, 1f);
  }

  @Test
  public void zoneUpMolRiInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpMolRiInt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpMolRiInt, 0.5f);
    assertWeight(weights, UpMolLeExt, 1f);
    assertWeight(weights, UpIncExt, 1f);
  }

  @Test
  public void zoneUpIncExt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpIncExt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpIncExt, 1f);
    assertWeight(weights, UpMolRiExt, 0.5f);
    assertWeight(weights, UpMolLeExt, 0.5f);
  }

  @Test
  public void zoneUpIncInt_confusionWeightZonesFor() {
    HeuristicOfflineBrushingUtils utils = new HeuristicOfflineBrushingUtils();
    List<WeightMouthZone> weights = utils.confusionWeightZonesFor(UpIncInt);
    assertEquals(3, weights.size());
    assertWeight(weights, UpIncExt, 1f);
    assertWeight(weights, UpMolRiOcc, 0.5f);
    assertWeight(weights, UpMolLeOcc, 0.5f);
  }

  private void assertWeight(List<WeightMouthZone> weights, MouthZone16 zone, float expectedWeight) {
    WeightMouthZone expectedWeightMouthZone = new WeightMouthZone(zone, expectedWeight);
    assertTrue(weights.contains(expectedWeightMouthZone));
  }
}
