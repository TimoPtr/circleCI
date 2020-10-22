package com.kolibree.android.app.utils.dataRecorder;

import android.os.SystemClock;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.sdkws.data.model.BrushPass;
import com.kolibree.sdkws.data.model.BrushProcessData;
import com.kolibree.sdkws.data.model.BrushZonePasses;
import java.util.ArrayList;
import timber.log.Timber;

/** Created by mdaniel on 30/11/2015. */
// TODO refactor data recording
@Keep
public abstract class DataRecorder {

  private static final String TAG = "DataRecorder";

  private int targetBrushingTime;

  // Current objects
  @VisibleForTesting BrushPass currentPass;

  @VisibleForTesting BrushZonePasses currentZone;

  @VisibleForTesting final BrushProcessData processData;

  @VisibleForTesting long startTimestamp;

  @VisibleForTesting long startPassTimestamp;

  @VisibleForTesting long startPauseTimestamp;

  // Recorders states
  @VisibleForTesting boolean stopped;

  @VisibleForTesting boolean paused;

  @VisibleForTesting boolean wasInCorrectPosition = true;

  @VisibleForTesting long orientationGoodDurationMillis = 0;

  /**
   * *******************************************************************************************
   * DATARECORDER - Constructor and abstract methods
   * ********************************************************************************************
   */
  DataRecorder(int targetBrushingTime) {
    this.targetBrushingTime = targetBrushingTime;

    processData = new BrushProcessData();
  }

  public BrushProcessData getProcessData() {
    return processData;
  }

  abstract int expectedTimeForZone(int zoneId, long goal);

  @NonNull
  protected abstract String[] zoneNames();

  @VisibleForTesting
  String safeZoneName(int zoneId) {
    if (zoneId < 0 || zoneId >= getZoneCount()) {
      return zoneNames()[0];
    }

    return zoneNames()[zoneId];
  }

  /*
   * *******************************************************************************************
   * DATARECORDER - Events methods
   * ********************************************************************************************
   */

  /**
   * Start recording data.
   *
   * <p>At t0, it considers that the user is at the first zone
   */
  public void start() {
    if (stopped) {
      return;
    }

    // Fix : server does not accept missing zone in json anymore
    createEmptyBrushingPassForEachZone();

    setInitialState();

    changeToInitialZone();

    createPass();
  }

  @VisibleForTesting
  void changeToInitialZone() {
    int initialPrescribedZone = 0;
    changeZone(initialPrescribedZone);
  }

  @VisibleForTesting
  void setInitialState() {
    startTimestamp = elapsedRealTimeMillis();
    orientationGoodDurationMillis = 0;
  }

  @VisibleForTesting
  void createEmptyBrushingPassForEachZone() {
    for (int i = 0, size = getZoneCount() - 1; i <= size; i++) {
      processData.addZonePass(
          new BrushZonePasses(
              safeZoneName(i), new ArrayList<>(), expectedTimeForZone(i, targetBrushingTime)));
    }
  }

  public void resume() {
    incrementReferenceTimestampsAfterPause();

    paused = false;
  }

  private void incrementReferenceTimestampsAfterPause() {
    long pauseDuration = elapsedRealTimeMillis() - startPauseTimestamp;
    startTimestamp += pauseDuration;
    startPassTimestamp += pauseDuration;
  }

  public void stop() {
    if (stopped) {
      return;
    }
    if (paused) {
      resume();
    }
    stopped = true;
    closePass();
  }

  public void pause() {
    startPauseTimestamp = elapsedRealTimeMillis();
    paused = true;
  }

  public int getBrushTime() {
    return (int) ((elapsedRealTimeMillis() - startTimestamp) / 100);
  }

  public void toothbrushPositionDidChange(boolean correctPosition) {
    if (stopped) {
      return;
    }

    if (wasInCorrectPosition) {
      if (!correctPosition) {
        closePass();
      }
    } else { // Was in wrong position
      if (correctPosition) {
        createPass();
      }
    }
    wasInCorrectPosition = correctPosition;
  }

  /**
   * Notifies us that the prescribed zone has changed, as well as if the user is in the correct
   * position
   *
   * @param newPrescribedOneIndexedZoneId 1-indexed zoneId. Coach/Pirate zones start at index 1
   *     because index 0 is None
   * @param hasCorrectBrushingPosition is user currently in the correct position
   */
  public void prescribedZoneDidChange(
      int newPrescribedOneIndexedZoneId, boolean hasCorrectBrushingPosition) {
    if (wasInCorrectPosition) {
      closePass();
    }
    int zeroIndexZoneId = newPrescribedOneIndexedZoneId - 1;
    changeZone(zeroIndexZoneId);

    wasInCorrectPosition = hasCorrectBrushingPosition;
    if (hasCorrectBrushingPosition) {
      createPass();
    }
  }

  @VisibleForTesting
  void changeZone(int newPrescribedZoneId) {
    if (processData.containsBrushZone(safeZoneName(newPrescribedZoneId))) {
      // get existing zone already in processData
      currentZone = processData.getZonePass(safeZoneName(newPrescribedZoneId));
    } else {
      // create a new zone and add to processData
      createZone(newPrescribedZoneId);
    }
  }

  @VisibleForTesting
  void createZone(int newPrescribedZoneId) {
    BrushZonePasses newZone =
        new BrushZonePasses(
            safeZoneName(newPrescribedZoneId),
            new ArrayList<>(),
            expectedTimeForZone(newPrescribedZoneId, targetBrushingTime));

    processData.addZonePass(newZone);
    currentZone = newZone;
  }

  @VisibleForTesting
  void createPass() {
    BrushPass newPass = new BrushPass(0, 0);
    newPass.setPass_datetime(getBrushTime());
    currentPass = newPass;
    startPassTimestamp = elapsedRealTimeMillis();
  }

  @VisibleForTesting
  void closePass() {
    if (currentPass != null) {
      currentPass.setPass_effective_time(
          (int) ((elapsedRealTimeMillis() - startPassTimestamp) / 100));

      /*
       Fixing https://jira.kolibree.com/browse/KLTB002-1250
        The cause of the problem is still unknown, this will prevent the bug from occurring,
        but doesn't fix the source of the issue:
        when pausing the game in the first level, it records passes with a negative effective
        time value.
        Can be reproduced 1 / 20 times, adds 'grey' zones to the checkup
      */
      if (currentZone != null && currentPass.getPass_effective_time() > 0) {
        currentZone.addPass(currentPass);
      }
      currentPass = null;
      orientationGoodDurationMillis += elapsedRealTimeMillis() - startPassTimestamp;
    } else {
      Timber.e("Can't close Pass : current pass null");
    }
  }

  @VisibleForTesting
  long elapsedRealTimeMillis() {
    return SystemClock.elapsedRealtime();
  }

  public int getZoneCount() {
    return zoneNames().length;
  }

  public long getGoodOrientationTimeMillis() {
    return orientationGoodDurationMillis;
  }
}
