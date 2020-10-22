package com.kolibree.android.processedbrushings;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kolibree.android.processedbrushings.models.ZoneData;
import com.kolibree.android.processedbrushings.models.ZonePass;
import com.kolibree.kml.MouthZone16;
import com.kolibree.sdkws.brushing.wrapper.IBrushing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

@SuppressWarnings("KotlinInternalInJava")
public class LegacyProcessedDataGeneratorTest {

  @Test
  public void testIncreaseBy15Percent() {
    assertEquals(6, LegacyProcessedDataGenerator.increaseBy15Percent(5));
    assertEquals(115, LegacyProcessedDataGenerator.increaseBy15Percent(100));
    assertEquals(230, LegacyProcessedDataGenerator.increaseBy15Percent(200));
  }

  @Test
  public void testTotalDuration() {
    ArrayList<ZonePass> passes = new ArrayList<>();
    passes.add(new ZonePass(0, 10));
    passes.add(new ZonePass(20, 100));
    passes.add(new ZonePass(300, 80));
    ZoneData data = ZoneData.createFromGoalBrushingTime(100, passes);
    assertEquals(10 + 100 + 80, LegacyProcessedDataGenerator.totalDuration(data));
  }

  @Test
  public void testMerge() {
    Map<MouthZone16, ZoneData> zones = new HashMap<>();
    ArrayList<ZonePass> passes = new ArrayList<>();
    ZoneData zoneData = ZoneData.createFromGoalBrushingTime(0, passes);
    passes.add(new ZonePass(0, 100));
    passes.add(new ZonePass(0, 110));
    passes.add(new ZonePass(0, 120));
    passes.add(new ZonePass(0, 150));
    zones.put(MouthZone16.LoIncExt, zoneData);

    Map<MouthZone16, Integer> merged = LegacyProcessedDataGenerator.merge(zones);
    assertEquals(100 + 110 + 120 + 150, (long) merged.get(MouthZone16.LoIncExt));
  }

  @Test
  public void testSort_withSingleEventPerZone() {
    int goalTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS;
    int brushingTime = 20000;
    String eventsAsRawJson =
        "["
            + "{\"dateTime\"=0, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiOcc\"}, "
            + "{\"dateTime\"=85, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiOcc\"}, "
            + "{\"dateTime\"=90, \"vibrator\"=\"true\", \"zone\"=\"UpIncExt\"}, "
            + "{\"dateTime\"=95, \"vibrator\"=\"true\", \"zone\"=\"LoIncExt\"}, "
            + "{\"dateTime\"=200, \"vibrator\"=\"false\", \"zone\"=\"LoMolLeOcc\"}, "
            + "{\"dateTime\"=400, \"vibrator\"=\"false\", \"zone\"=\"LoMolLeOcc\"}"
            + "]";

    Gson gson = new Gson();
    RecordedSession.Event[] encodedEventsArray =
        gson.fromJson(eventsAsRawJson, new TypeToken<RecordedSession.Event[]>() {}.getType());

    RecordedSession recordedSession =
        new RecordedSession(
            OffsetDateTime.parse("2018-11-12T16:08:03Z"), brushingTime, encodedEventsArray);

    Map<MouthZone16, ZoneData> sorted =
        LegacyProcessedDataGenerator.sort(recordedSession, goalTime);

    List<MouthZone16> nonEmptyZones =
        Arrays.asList(
            MouthZone16.LoMolRiOcc,
            MouthZone16.UpMolRiOcc,
            MouthZone16.UpIncExt,
            MouthZone16.LoIncExt);

    for (MouthZone16 zone16 : MouthZone16.values()) {
      assertEquals(75 /* = 120 * 10 / 16 */, sorted.get(zone16).getExpectedTenthOfSecondsPerZone());
      assertEquals(nonEmptyZones.contains(zone16), !sorted.get(zone16).getPasses().isEmpty());
    }

    final int expectedJoinedDuration = 200; // from 0 to first event without vibrator
    AtomicLong realJoinedDuration = new AtomicLong();
    sorted.forEach(
        (mouthZone16, zoneData) ->
            zoneData
                .getPasses()
                .forEach(
                    zonePass -> {
                      realJoinedDuration.addAndGet(zonePass.getDurationTenthSecond());
                    }));
    assertEquals(expectedJoinedDuration, realJoinedDuration.intValue());
  }

  @Test
  public void testSort_withMultipleEventsPerZone() {
    int goalTime = 120;
    int brushingTime = 20000;
    String eventsAsRawJson =
        "["
            + "{\"dateTime\"=0, \"vibrator\"=\"true\", \"zone\"=\"LoIncInt\"},"
            + "{\"dateTime\"=45, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiExt\"},"
            + "{\"dateTime\"=50, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiExt\"},"
            + "{\"dateTime\"=55, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeInt\"},"
            + "{\"dateTime\"=60, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiExt\"},"
            + "{\"dateTime\"=75, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeInt\"},"
            + "{\"dateTime\"=80, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiExt\"},"
            + "{\"dateTime\"=85, \"vibrator\"=\"true\", \"zone\"=\"LoMolLeOcc\"},"
            + "{\"dateTime\"=90, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeExt\"},"
            + "{\"dateTime\"=205, \"vibrator\"=\"true\", \"zone\"=\"UpIncExt\"},"
            + "{\"dateTime\"=225, \"vibrator\"=\"true\", \"zone\"=\"UpIncInt\"},"
            + "{\"dateTime\"=240, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeExt\"},"
            + "{\"dateTime\"=245, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiInt\"},"
            + "{\"dateTime\"=250, \"vibrator\"=\"true\", \"zone\"=\"UpIncInt\"},"
            + "{\"dateTime\"=255, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeExt\"},"
            + "{\"dateTime\"=265, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiInt\"},"
            + "{\"dateTime\"=280, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeExt\"},"
            + "{\"dateTime\"=285, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiOcc\"},"
            + "{\"dateTime\"=290, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeExt\"},"
            + "{\"dateTime\"=295, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiInt\"},"
            + "{\"dateTime\"=300, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeOcc\"},"
            + "{\"dateTime\"=305, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiInt\"},"
            + "{\"dateTime\"=315, \"vibrator\"=\"true\", \"zone\"=\"LoMolLeExt\"},"
            + "{\"dateTime\"=320, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiExt\"},"
            + "{\"dateTime\"=360, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiOcc\"},"
            + "{\"dateTime\"=365, \"vibrator\"=\"true\", \"zone\"=\"LoIncInt\"},"
            + "{\"dateTime\"=370, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiOcc\"},"
            + "{\"dateTime\"=375, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiExt\"},"
            + "{\"dateTime\"=385, \"vibrator\"=\"true\", \"zone\"=\"LoIncInt\"},"
            + "{\"dateTime\"=400, \"vibrator\"=\"true\", \"zone\"=\"LoMolLeInt\"},"
            + "{\"dateTime\"=405, \"vibrator\"=\"true\", \"zone\"=\"UpMolRiInt\"},"
            + "{\"dateTime\"=410, \"vibrator\"=\"true\", \"zone\"=\"LoMolLeOcc\"},"
            + "{\"dateTime\"=415, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiInt\"},"
            + "{\"dateTime\"=420, \"vibrator\"=\"true\", \"zone\"=\"LoMolRiOcc\"},"
            + "{\"dateTime\"=425, \"vibrator\"=\"true\", \"zone\"=\"LoIncInt\"},"
            + "{\"dateTime\"=430, \"vibrator\"=\"true\", \"zone\"=\"UpMolLeOcc\"},"
            + "{\"dateTime\"=440, \"vibrator\"=\"false\", \"zone\"=\"LoMolLeOcc\"}"
            + "]";

    Gson gson = new Gson();
    RecordedSession.Event[] encodedEventsArray =
        gson.fromJson(eventsAsRawJson, new TypeToken<RecordedSession.Event[]>() {}.getType());

    RecordedSession recordedSession =
        new RecordedSession(
            OffsetDateTime.parse("2018-11-12T16:08:03Z"), brushingTime, encodedEventsArray);

    Map<MouthZone16, ZoneData> sorted =
        LegacyProcessedDataGenerator.sort(recordedSession, goalTime);

    for (MouthZone16 zone16 : MouthZone16.values()) {
      assertEquals(75 /* = 120 * 10 / 16 */, sorted.get(zone16).getExpectedTenthOfSecondsPerZone());
    }

    Map<MouthZone16, Integer> passesPerZone =
        new HashMap<MouthZone16, Integer>() {
          {
            put(MouthZone16.LoMolLeOcc, 2);
            put(MouthZone16.LoMolLeExt, 1);
            put(MouthZone16.LoMolLeInt, 1);
            put(MouthZone16.LoMolRiOcc, 3);
            put(MouthZone16.LoMolRiExt, 3);
            put(MouthZone16.LoMolRiInt, 1);
            put(MouthZone16.LoIncExt, 0);
            put(MouthZone16.LoIncInt, 4);
            put(MouthZone16.UpMolLeOcc, 2);
            put(MouthZone16.UpMolLeExt, 5);
            put(MouthZone16.UpMolLeInt, 2);
            put(MouthZone16.UpMolRiOcc, 1);
            put(MouthZone16.UpMolRiExt, 3);
            put(MouthZone16.UpMolRiInt, 5);
            put(MouthZone16.UpIncExt, 1);
            put(MouthZone16.UpIncInt, 2);
          }
        };

    passesPerZone
        .entrySet()
        .forEach(
            entry ->
                assertEquals(
                    entry.getValue().intValue(), sorted.get(entry.getKey()).getPasses().size()));
  }

  @Test
  public void testIncrease() {
    Map<MouthZone16, Integer> times = new HashMap<>();
    times.put(MouthZone16.LoIncExt, 100);
    times.put(MouthZone16.LoIncInt, 200);
    times.put(MouthZone16.LoMolLeInt, 300);
    times.put(MouthZone16.LoMolRiInt, 400);

    Map<MouthZone16, Integer> increased = LegacyProcessedDataGenerator.increase(times);
    assertEquals(115, (long) increased.get(MouthZone16.LoIncExt));
    assertEquals(230, (long) increased.get(MouthZone16.LoIncInt));
    assertEquals(345, (long) increased.get(MouthZone16.LoMolLeInt));
    assertEquals(460, (long) increased.get(MouthZone16.LoMolRiInt));
  }

  @Test
  public void testFormat() {
    Map<MouthZone16, Integer> times = new HashMap<>();
    times.put(MouthZone16.LoMolLeOcc, 100);
    times.put(MouthZone16.LoMolRiExt, 200);
    times.put(MouthZone16.LoIncExt, 300);

    Map<MouthZone16, ZoneData> format = LegacyProcessedDataGenerator.format(times, 100);
    ZoneData data1 = format.get(MouthZone16.LoMolLeOcc);
    assertEquals(1, data1.getPasses().size());
    assertEquals(0, data1.getPasses().get(0).getStartTime());
    assertEquals(100, data1.getPasses().get(0).getDurationTenthSecond());

    ZoneData data2 = format.get(MouthZone16.LoMolRiExt);
    assertEquals(1, data2.getPasses().size());
    assertEquals(100, data2.getPasses().get(0).getStartTime());
    assertEquals(200, data2.getPasses().get(0).getDurationTenthSecond());

    ZoneData data3 = format.get(MouthZone16.LoIncExt);
    assertEquals(1, data3.getPasses().size());
    assertEquals(300, data3.getPasses().get(0).getStartTime());
    assertEquals(300, data3.getPasses().get(0).getDurationTenthSecond());
  }
}
