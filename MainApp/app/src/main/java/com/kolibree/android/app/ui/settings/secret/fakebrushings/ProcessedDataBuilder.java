/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings;

import androidx.annotation.NonNull;
import com.kolibree.android.processedbrushings.models.ZonePass;
import com.kolibree.kml.MouthZone16;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.threeten.bp.Clock;

/** Created by aurelien on 31/03/17. */
final class ProcessedDataBuilder {

  private final HashMap<MouthZone16, List<ZonePass>> data;
  private boolean running;
  private long startTimeMillis;
  private int currentZoneStartTime;
  private boolean currentVibrator;
  private MouthZone16 currentZone;

  private Clock clock;

  /** Create an empty ProcessedDataBuilder. */
  public ProcessedDataBuilder(Clock clock) {
    this.data = new HashMap<>(16);
    this.clock = clock;

    for (MouthZone16 z : MouthZone16.values()) {
      data.put(z, new ArrayList<>());
    }
  }

  void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * The vibrator state has changed.
   *
   * @param on true if vibrator is on, false if vibrator is off
   */
  public void onVibratorStateChanged(boolean on) {
    if (running) {
      if (currentZone != null) {
        onEvent(currentVibrator, currentZone);
      }

      currentVibrator = on;
    }
  }

  /**
   * A new MouthZone has been detected.
   *
   * @param zone the new detected mouthzone
   */
  public void onMouthZoneDetection(@NonNull MouthZone16 zone) {
    if (running) {
      if (currentZone == null) {
        currentZone = zone;
        currentZoneStartTime = getTimeInTenthOfSecondsSinceStart();
      } else if (zone != currentZone) {
        onEvent(currentVibrator, currentZone);
        currentZone = zone;
        currentZoneStartTime = getTimeInTenthOfSecondsSinceStart();
      }
    }
  }

  private void onEvent(boolean wasVibrating, @NonNull MouthZone16 wasIn) {
    if (wasVibrating) {
      final int nowTenthSecond = getTimeInTenthOfSecondsSinceStart();

      synchronized (data) {
        data.get(wasIn)
            .add(new ZonePass(currentZoneStartTime, nowTenthSecond - currentZoneStartTime));
      }
    }
  }

  private int getTimeInTenthOfSecondsSinceStart() {
    return (int) ((nowInMillis() - startTimeMillis) / 100L);
  }

  /**
   * Start processed data builder.
   *
   * @param vibratorIsOn is the vibrator on or not
   */
  public void start(boolean vibratorIsOn) {
    synchronized (data) {
      for (List list : data.values()) {
        list.clear();
      }
    }

    currentVibrator = vibratorIsOn;
    startTimeMillis = nowInMillis();
    running = true;
  }

  private long nowInMillis() {
    return clock.millis();
  }

  /**
   * Stop the ProcessedDataBuilder.
   *
   * @return a map between the mouthzones and the ZonePasses
   */
  public @NonNull HashMap<MouthZone16, List<ZonePass>> stop() {
    running = false;
    currentVibrator = false;
    startTimeMillis = 0;

    synchronized (data) {
      return data;
    }
  }
}
