/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils.dataRecorder;

import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.EXPECTED_TIME_ZONE1;
import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.EXPECTED_TIME_ZONE2;
import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.EXPECTED_TIME_ZONE3;
import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.ZONE_1;
import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.ZONE_2;
import static com.kolibree.android.app.utils.dataRecorder.DataRecorderTest.FakeDataRecorder.ZONE_3;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.data.model.BrushPass;
import com.kolibree.sdkws.data.model.BrushProcessData;
import com.kolibree.sdkws.data.model.BrushZonePasses;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/** Created by miguelaragues on 1/3/18. */
public class DataRecorderTest extends BaseUnitTest {

  private DataRecorder dataRecorder = spy(new FakeDataRecorder(120));

  /*
  START
   */
  @Test
  public void start_stopped_neverInvokesCreateBrushingPassForEachZones() {
    dataRecorder.stopped = true;

    dataRecorder.start();

    verify(dataRecorder, never()).createEmptyBrushingPassForEachZone();
  }

  @Test
  public void start_stoppedFalse_invokesCreateBrushingPassForEachZones() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).createEmptyBrushingPassForEachZone();
    doNothing().when(dataRecorder).setInitialState();
    doNothing().when(dataRecorder).createPass();
    doNothing().when(dataRecorder).changeZone(anyInt());

    dataRecorder.start();

    verify(dataRecorder).createEmptyBrushingPassForEachZone();
  }

  @Test
  public void start_stoppedFalse_invokesSetInitialState() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).createEmptyBrushingPassForEachZone();
    doNothing().when(dataRecorder).setInitialState();
    doNothing().when(dataRecorder).createPass();
    doNothing().when(dataRecorder).changeToInitialZone();

    dataRecorder.start();

    verify(dataRecorder).setInitialState();
  }

  @Test
  public void start_stoppedFalse_invokesCreatePass() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).createEmptyBrushingPassForEachZone();
    doNothing().when(dataRecorder).setInitialState();
    doNothing().when(dataRecorder).createPass();
    doNothing().when(dataRecorder).changeToInitialZone();

    dataRecorder.start();

    verify(dataRecorder).createPass();
  }

  @Test
  public void start_stoppedFalse_invokesChangeToInitialZone() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).createEmptyBrushingPassForEachZone();
    doNothing().when(dataRecorder).setInitialState();
    doNothing().when(dataRecorder).createPass();
    doNothing().when(dataRecorder).changeToInitialZone();

    dataRecorder.start();

    verify(dataRecorder).changeToInitialZone();
  }

  /*
  CREATE BRUSHING PASS FOR EACH ZONE
   */
  @Test
  public void createBrushingPassForEachZone_createsABrushPassForEachZone() {
    assertTrue(dataRecorder.getProcessData().getZonepasses().isEmpty());

    dataRecorder.createEmptyBrushingPassForEachZone();

    assertTrue(dataRecorder.getProcessData().containsBrushZone(ZONE_3));
    assertTrue(dataRecorder.getProcessData().containsBrushZone(ZONE_2));
    assertTrue(dataRecorder.getProcessData().containsBrushZone(ZONE_1));
  }

  @Test
  public void createBrushingPassForEachZone_storesExpectedTimeForEachZone() {
    dataRecorder.createEmptyBrushingPassForEachZone();

    assertEquals(
        EXPECTED_TIME_ZONE1, dataRecorder.getProcessData().getZonePass(ZONE_1).getExpectedTime());
    assertEquals(
        EXPECTED_TIME_ZONE2, dataRecorder.getProcessData().getZonePass(ZONE_2).getExpectedTime());
    assertEquals(
        EXPECTED_TIME_ZONE3, dataRecorder.getProcessData().getZonePass(ZONE_3).getExpectedTime());
  }

  @Test
  public void createBrushingPassForEachZone_initsBrushZonePassWithEmptyPasses() {
    dataRecorder.createEmptyBrushingPassForEachZone();

    assertTrue(dataRecorder.getProcessData().getZonePass(ZONE_1).getPasses().isEmpty());
    assertTrue(dataRecorder.getProcessData().getZonePass(ZONE_2).getPasses().isEmpty());
    assertTrue(dataRecorder.getProcessData().getZonePass(ZONE_3).getPasses().isEmpty());
  }

  /*
  CHANGE TO INITIAL ZONE
   */
  @Test
  public void changeToInitialZone_invokesChangeZoneWithValue0() {
    doNothing().when(dataRecorder).changeZone(0);

    dataRecorder.changeToInitialZone();

    verify(dataRecorder).changeZone(0);
  }

  /*
  SET INITIAL STATE
   */
  @Test
  public void setInitialState_storesElapsedRealTimeInStartTimestamp() {
    long expectedStartTimestamp = 98L;
    doReturn(expectedStartTimestamp).when(dataRecorder).elapsedRealTimeMillis();

    assertEquals(0, dataRecorder.startTimestamp);

    dataRecorder.setInitialState();

    assertEquals(expectedStartTimestamp, dataRecorder.startTimestamp);
  }

  @Test
  public void setInitialState_stores0InOrientationGoodDurationMillis() {
    dataRecorder.orientationGoodDurationMillis = 432;
    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.setInitialState();

    assertEquals(0, dataRecorder.orientationGoodDurationMillis);
  }

  /*
  CREATE PASS
   */
  @Test
  public void
      createPass_storesNewBrushPassInCurrentPass_withDateTimeSinceStartTimestampAndEffectiveTime0() {
    int ellapsedSeconds = 1;
    int startEllapsedSeconds = 5;
    long startTimestampMillis = TimeUnit.SECONDS.toMillis(startEllapsedSeconds);
    long elapsedRealTime =
        startTimestampMillis + TimeUnit.SECONDS.toMillis(ellapsedSeconds); // 1 second after start
    doReturn(elapsedRealTime).when(dataRecorder).elapsedRealTimeMillis();
    assertNull(dataRecorder.currentPass);

    dataRecorder.createPass();

    int expectedTenthOfSeconds = (startEllapsedSeconds + ellapsedSeconds) * 10;

    assertEquals(expectedTenthOfSeconds, dataRecorder.currentPass.getPass_datetime());
  }

  @Test
  public void createPass_assignsElapsedTimeToStartPassTimestamp() {
    long elapsedRealTime = 30;
    doReturn(elapsedRealTime).when(dataRecorder).elapsedRealTimeMillis();

    assertEquals(0, dataRecorder.startPassTimestamp);

    dataRecorder.createPass();

    assertEquals(elapsedRealTime, dataRecorder.startPassTimestamp);
  }

  /*
  RESUME
   */
  @Test
  public void resume_calculatesPauseDurationAndAddsItToStartTimeStamp() {
    long pauseTimestamp = 1000;
    dataRecorder.startPauseTimestamp = pauseTimestamp;

    long elapsedRealTime = 2000;
    doReturn(elapsedRealTime).when(dataRecorder).elapsedRealTimeMillis();

    long startTimestamp = 98435;
    dataRecorder.startTimestamp = startTimestamp;

    dataRecorder.resume();

    long expectedStartTimestamp = startTimestamp + (elapsedRealTime - pauseTimestamp);

    assertEquals(expectedStartTimestamp, dataRecorder.startTimestamp);
  }

  @Test
  public void resume_calculatesPauseDurationAndAddsItToStartPassTimeStamp() {
    long pauseTimestamp = 1000;
    dataRecorder.startPauseTimestamp = pauseTimestamp;

    long elapsedRealTime = 2000;
    doReturn(elapsedRealTime).when(dataRecorder).elapsedRealTimeMillis();

    long startPassTimestamp = 54656;
    dataRecorder.startPassTimestamp = startPassTimestamp;

    dataRecorder.resume();

    long expectedStartPassTimestamp = startPassTimestamp + (elapsedRealTime - pauseTimestamp);

    assertEquals(expectedStartPassTimestamp, dataRecorder.startPassTimestamp);
  }

  @Test
  public void resume_setsPausedToFalse() {
    dataRecorder.paused = true;

    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.resume();

    assertFalse(dataRecorder.paused);
  }

  /*
  STOP
   */
  @Test
  public void stop_stoppedTrue_returnsImmediately() {
    dataRecorder.stopped = true;

    dataRecorder.stop();

    verify(dataRecorder, never()).closePass();
  }

  @Test
  public void stop_stoppedFalse_pausedTrue_invokesResume() {
    dataRecorder.stopped = false;
    dataRecorder.paused = true;

    doNothing().when(dataRecorder).resume();
    doNothing().when(dataRecorder).closePass();

    dataRecorder.stop();

    verify(dataRecorder).resume();
  }

  @Test
  public void stop_stoppedFalse_pausedFalse_neverInvokesResume() {
    dataRecorder.stopped = false;
    dataRecorder.paused = false;

    doNothing().when(dataRecorder).closePass();

    dataRecorder.stop();

    verify(dataRecorder, never()).resume();
  }

  @Test
  public void stop_stoppedFalse_invokesClosePass() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).closePass();

    dataRecorder.stop();

    verify(dataRecorder).closePass();
  }

  @Test
  public void stop_stoppedFalse_flagsAsStoppedTrue() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).closePass();

    dataRecorder.stop();

    assertTrue(dataRecorder.stopped);
  }

  /*
  PAUSE
   */
  @Test
  public void pause_storesCurrentEllapsedTimeInStartPauseTimestamp() {
    long elapsedTime = 2000;
    doReturn(elapsedTime).when(dataRecorder).elapsedRealTimeMillis();

    assertEquals(0, dataRecorder.startPauseTimestamp);

    dataRecorder.pause();

    assertEquals(elapsedTime, dataRecorder.startPauseTimestamp);
  }

  @Test
  public void pause_flagsAsPaused() {
    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    assertFalse(dataRecorder.paused);

    dataRecorder.pause();

    assertTrue(dataRecorder.paused);
  }

  /*
  CLOSE PASS
   */
  @Test
  public void closePass_currentPassNull_doesNothing() {
    dataRecorder.closePass();
  }

  @Test
  public void closePass_withCurrentPass_storesEffectivePassTime() {
    BrushPass brushPass = mock(BrushPass.class);
    dataRecorder.currentPass = brushPass;

    dataRecorder.startPassTimestamp = 1000;

    long elapsedTime = 2000;
    doReturn(elapsedTime).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.closePass();

    int expectedPassEffectiveTime = (int) ((elapsedTime - dataRecorder.startPassTimestamp) / 100);
    verify(brushPass).setPass_effective_time(expectedPassEffectiveTime);
  }

  @Test
  public void closePass_withCurrentPass_nullifiesCurrentPass() {
    dataRecorder.currentPass = mock(BrushPass.class);

    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.closePass();

    assertNull(dataRecorder.currentPass);
  }

  @Test
  public void closePass_withCurrentPass_incrementsOrientationGoodDurationMillis() {
    dataRecorder.currentPass = mock(BrushPass.class);

    dataRecorder.startPassTimestamp = 1000;

    long elapsedTime = 2000;
    doReturn(elapsedTime).when(dataRecorder).elapsedRealTimeMillis();

    long initialOrientationDuration = 9001;
    dataRecorder.orientationGoodDurationMillis = initialOrientationDuration;

    dataRecorder.closePass();

    long expectedOrientationGoodDurationMillis =
        initialOrientationDuration + elapsedTime - dataRecorder.startPassTimestamp;

    assertEquals(expectedOrientationGoodDurationMillis, dataRecorder.orientationGoodDurationMillis);
  }

  @Test
  public void closePass_withCurrentPass_currentZoneNull_doesntCrash() {
    dataRecorder.currentPass = mock(BrushPass.class);

    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.closePass();
  }

  @Test
  public void closePass_withCurrentPass_withCurrentZone_withEffectiveTimeZero_doesntCrash() {
    BrushPass brushPass = mock(BrushPass.class);
    dataRecorder.currentPass = brushPass;
    BrushZonePasses brushZonePass = mock(BrushZonePasses.class);
    dataRecorder.currentZone = brushZonePass;

    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.closePass();

    verify(brushZonePass, never()).addPass(brushPass);
  }

  @Test
  public void
      closePass_withCurrentPass_withCurrentZone_withEffectiveTimeOverZero_addsCurrentPassToCurrentZone() {
    BrushPass brushPass = mock(BrushPass.class);
    dataRecorder.currentPass = brushPass;
    BrushZonePasses brushZonePass = mock(BrushZonePasses.class);
    dataRecorder.currentZone = brushZonePass;

    when(brushPass.getPass_effective_time()).thenReturn(10);

    doReturn(0L).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.closePass();

    verify(brushZonePass).addPass(brushPass);
  }

  /*
  CREATE ZONE
   */
  @Test
  public void createZone_createsNewZoneForIndexAndStoresItInCurrentZone() {
    assertNull(dataRecorder.currentZone);

    dataRecorder.createZone(1);

    assertNotNull(dataRecorder.currentZone);
  }

  @Test
  public void createZone_createsNewZoneForIndexAndAddsItToProcessData() {
    assertTrue(dataRecorder.getProcessData().getZonepasses().isEmpty());

    dataRecorder.createZone(1);

    assertTrue(dataRecorder.getProcessData().getZonepasses().contains(dataRecorder.currentZone));
  }

  @Test
  public void createZone_createsNewZoneWithExpectedValues() {
    dataRecorder.createZone(1);

    assertEquals(ZONE_2, dataRecorder.currentZone.getZoneName());
    assertEquals(EXPECTED_TIME_ZONE2, dataRecorder.currentZone.getExpectedTime());
    assertTrue(dataRecorder.currentZone.getPasses().isEmpty());
  }

  /*
  CHANGE ZONE
   */
  @Test
  public void changeZone_processedDataContainsNewZone_storesExistingZoneInCurrentZone() {
    int newPrescribedZoneId = 2;

    BrushProcessData brushProcessedData = mock(BrushProcessData.class);
    setProcessedData(brushProcessedData);

    BrushZonePasses expectedZonePass = mock(BrushZonePasses.class);
    when(brushProcessedData.getZonePass(ZONE_3)).thenReturn(expectedZonePass);

    when(brushProcessedData.containsBrushZone(ZONE_3)).thenReturn(true);

    assertNull(dataRecorder.currentZone);

    dataRecorder.changeZone(newPrescribedZoneId);

    assertEquals(expectedZonePass, dataRecorder.currentZone);
  }

  @Test
  public void changeZone_processedDataDoesNotContainNewZone_invokesCreateZone() {
    int newPrescribedZoneId = 2;

    BrushProcessData brushProcessedData = mock(BrushProcessData.class);
    setProcessedData(brushProcessedData);

    when(brushProcessedData.containsBrushZone(ZONE_3)).thenReturn(false);

    doNothing().when(dataRecorder).createZone(anyInt());

    dataRecorder.changeZone(newPrescribedZoneId);

    verify(dataRecorder).createZone(newPrescribedZoneId);
  }

  /*
  PRESCRIBED ZONE DID CHANGE
   */
  @Test
  public void prescribedZoneDidChange_wasInCorrectPositionTrue_invokesClosePass() {
    int newPrescribedZoneId = 2;
    boolean ignore = false;

    dataRecorder.wasInCorrectPosition = true;

    doNothing().when(dataRecorder).closePass();

    doNothing().when(dataRecorder).changeZone(anyInt());

    dataRecorder.prescribedZoneDidChange(newPrescribedZoneId, ignore);

    verify(dataRecorder).closePass();
  }

  @Test
  public void prescribedZoneDidChange_wasInCorrectPositionFalse_neverInvokesClosePass() {
    int newPrescribedZoneId = 2;
    boolean ignore = false;

    dataRecorder.wasInCorrectPosition = false;

    doNothing().when(dataRecorder).changeZone(anyInt());

    dataRecorder.prescribedZoneDidChange(newPrescribedZoneId, ignore);

    verify(dataRecorder, never()).closePass();
  }

  @Test
  public void prescribedZoneDidChange_storesWasInCorrectPosition() {
    int newPrescribedZoneId = 2;

    doNothing().when(dataRecorder).changeZone(anyInt());

    assertTrue(dataRecorder.wasInCorrectPosition);

    boolean hasCorrectPosition = false;
    dataRecorder.prescribedZoneDidChange(newPrescribedZoneId, hasCorrectPosition);

    assertFalse(dataRecorder.wasInCorrectPosition);
  }

  @Test
  public void prescribedZoneDidChange_hasCorrectPositionTrue_invokesCreatePass() {
    int newPrescribedZoneId = 2;

    doNothing().when(dataRecorder).changeZone(anyInt());
    doNothing().when(dataRecorder).createPass();

    boolean hasCorrectPosition = true;
    dataRecorder.prescribedZoneDidChange(newPrescribedZoneId, hasCorrectPosition);

    verify(dataRecorder).createPass();
  }

  @Test
  public void prescribedZoneDidChange_hasCorrectPositionFalse_neverInvokesCreatePass() {
    int newPrescribedZoneId = 2;

    doNothing().when(dataRecorder).changeZone(anyInt());

    boolean hasCorrectPosition = false;
    dataRecorder.prescribedZoneDidChange(newPrescribedZoneId, hasCorrectPosition);

    verify(dataRecorder, never()).createPass();
  }

  @Test
  public void prescribedZoneDidChange_receives1IndexedZone_uses0IndexedZone() {
    int incomingZoneId = 1;
    int expectedInternalZoneIndex = incomingZoneId - 1;

    doNothing().when(dataRecorder).changeZone(anyInt());

    dataRecorder.prescribedZoneDidChange(incomingZoneId, false);

    verify(dataRecorder).changeZone(expectedInternalZoneIndex);
  }

  /*
  TOOTHBRUSH POSITION DID CHANGE
   */
  @Test
  public void toothbrushPositionDidChange_stopped_doesNothing() {
    dataRecorder.stopped = true;

    boolean valueThatWouldBeStored = !dataRecorder.wasInCorrectPosition;
    dataRecorder.toothbrushPositionDidChange(valueThatWouldBeStored);

    verify(dataRecorder, never()).closePass();
    verify(dataRecorder, never()).createPass();

    assertNotEquals(valueThatWouldBeStored, dataRecorder.wasInCorrectPosition);
  }

  @Test
  public void toothbrushPositionDidChange_stoppedFalse_storesValue() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).closePass();

    boolean valueToBeStored = !dataRecorder.wasInCorrectPosition;
    dataRecorder.toothbrushPositionDidChange(valueToBeStored);

    assertEquals(valueToBeStored, dataRecorder.wasInCorrectPosition);
  }

  @Test
  public void
      toothbrushPositionDidChange_stoppedFalse_wasInCorrectPositionTrue_incomingValueFalse_invokesClosePass() {
    dataRecorder.stopped = false;

    doNothing().when(dataRecorder).closePass();

    dataRecorder.wasInCorrectPosition = true;

    boolean incomingValue = false;
    dataRecorder.toothbrushPositionDidChange(incomingValue);

    verify(dataRecorder).closePass();
  }

  @Test
  public void
      toothbrushPositionDidChange_stoppedFalse_wasInCorrectPositionTrue_incomingValueTrue_neverInvokesClosePass() {
    dataRecorder.stopped = false;

    dataRecorder.wasInCorrectPosition = true;

    boolean incomingValue = true;
    dataRecorder.toothbrushPositionDidChange(incomingValue);

    verify(dataRecorder, never()).closePass();
  }

  @Test
  public void
      toothbrushPositionDidChange_stoppedFalse_wasInCorrectPositionFalse_incomingValueFalse_neverInvokesCreatePass() {
    dataRecorder.stopped = false;

    dataRecorder.wasInCorrectPosition = false;

    boolean incomingValue = false;
    dataRecorder.toothbrushPositionDidChange(incomingValue);

    verify(dataRecorder, never()).createPass();
  }

  @Test
  public void
      toothbrushPositionDidChange_stoppedFalse_wasInCorrectPositionFalse_incomingValueTrue_invokesCreatePass() {
    dataRecorder.stopped = false;

    dataRecorder.wasInCorrectPosition = false;

    doNothing().when(dataRecorder).createPass();

    boolean incomingValue = true;
    dataRecorder.toothbrushPositionDidChange(incomingValue);

    verify(dataRecorder).createPass();
  }

  /*
  GET BRUSH TIME
   */

  @Test
  public void getBrushTime_returnsTenthOfSecondsBetweenEllapsedTimeAndStartTimestamp() {
    long ellapsedTime = 2000;
    doReturn(ellapsedTime).when(dataRecorder).elapsedRealTimeMillis();

    dataRecorder.startTimestamp = 500;

    int expectedBrushingTime = (int) (ellapsedTime - dataRecorder.startTimestamp) / 100;

    assertEquals(expectedBrushingTime, dataRecorder.getBrushTime());
  }

  static class FakeDataRecorder extends DataRecorder {

    static final String ZONE_1 = "ZONE 1";
    static final String ZONE_2 = "ZONE 2";
    static final String ZONE_3 = "ZONE 3";
    private static final String[] ZONES = new String[] {ZONE_1, ZONE_2, ZONE_3};

    static final int EXPECTED_TIME_ZONE1 = 1;
    static final int EXPECTED_TIME_ZONE2 = 2;
    static final int EXPECTED_TIME_ZONE3 = 3;

    /**
     * *******************************************************************************************
     * DATARECORDER - Constructor and abstract methods
     * ********************************************************************************************
     */
    FakeDataRecorder(int targetBrushingTime) {
      super(targetBrushingTime);
    }

    @Override
    int expectedTimeForZone(int zoneId, long goal) {
      switch (zoneId) {
        case 0:
          return EXPECTED_TIME_ZONE1;
        case 1:
          return EXPECTED_TIME_ZONE2;
        case 2:
          return EXPECTED_TIME_ZONE3;
        default:
          return 0;
      }
    }

    @NonNull
    @Override
    protected String[] zoneNames() {
      return ZONES;
    }
  }

  private void setProcessedData(BrushProcessData brushProcessData) {
    try {
      Field f = DataRecorder.class.getDeclaredField("processData");

      f.setAccessible(true);
      f.set(dataRecorder, brushProcessData);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
