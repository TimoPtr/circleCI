/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble;

import static com.kolibree.android.sdk.core.driver.ble.KolibreeBleDriver.LOAD_SENSOR_CALIBRATION_MAX_RETRIES;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BOOTLOADER_VERSION;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_USER_ID;
import static com.kolibree.android.sdk.util.HexUtils.hexStringToByteArray;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyByte;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState;
import com.kolibree.android.sdk.core.binary.Bitmask;
import com.kolibree.android.sdk.core.binary.PayloadReader;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.KLTBDriverListener;
import com.kolibree.android.sdk.core.driver.VibratorMode;
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic;
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushingsExtractor;
import com.kolibree.android.sdk.error.CommandNotSupportedException;
import com.kolibree.android.sdk.error.ConnectionEstablishException;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.math.Matrix;
import com.kolibree.android.sdk.math.Vector;
import com.kolibree.android.sdk.version.DspVersion;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import kotlin.jvm.functions.Function0;
import no.nordicsemi.android.ble.ConnectRequestStub;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import timber.log.Timber;

/** Created by miguelaragues on 22/2/18. */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class KolibreeBleDriverTest extends BaseUnitTest {

  @Mock KLTBDriverListener driverListener;

  @Mock KLNordicBleManager bleManager;

  private Scheduler bluetoothScheduler;
  private Scheduler notifyListenerScheduler;

  @Mock CharacteristicNotificationStreamer notificationCaster;

  private StubKolibreeBleDriver bleDriver;

  @Before
  public void setup() throws Exception {
    super.setup();

    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);

    bluetoothScheduler = Schedulers.io();
    notifyListenerScheduler = Schedulers.io();

    bleDriver =
        spy(
            new StubKolibreeBleDriver(
                bleManager,
                driverListener,
                bluetoothScheduler,
                "mac",
                notificationCaster,
                notifyListenerScheduler));
  }

  /*
  processVersionsPayload
   */
  @Test
  public void processVersionsPayload_setsFirmwareVersion() {
    String loadVersionsResponse = "01 04 0B 00 04 00 01 00 04 00 AA 00 00 00 00 00 00";
    byte[] bytes = hexStringToByteArray(loadVersionsResponse);

    assertEquals(SoftwareVersion.NULL, bleDriver.getFirmwareVersion());

    bleDriver.processVersionsPayload(new PayloadReader(bytes));

    SoftwareVersion expectedSoftwareVersion = new SoftwareVersion(1, 4, 11);
    assertEquals(expectedSoftwareVersion, bleDriver.getFirmwareVersion());
  }

  @Test
  public void processVersionsPayload_setsHardwareVersion() {
    String loadVersionsResponse = "01 04 0B 00 04 00 01 00 04 00 AA 00 00 00 00 00 00";
    byte[] bytes = hexStringToByteArray(loadVersionsResponse);

    assertEquals(HardwareVersion.NULL, bleDriver.getHardwareVersion());

    bleDriver.processVersionsPayload(new PayloadReader(bytes));

    HardwareVersion expectedHardwareVersion = new HardwareVersion(4, 1);
    assertEquals(expectedHardwareVersion, bleDriver.getHardwareVersion());
  }

  @Test
  public void processVersionsPayload_byteElevenIs0x55_setsIsRunningBootloaderTrue() {
    String loadVersionsResponse = "01 04 0B 00 04 00 01 00 04 00 AA 55 00 00 00 00 00";
    byte[] bytes = hexStringToByteArray(loadVersionsResponse);

    assertFalse(bleDriver.isRunningBootloader());

    bleDriver.processVersionsPayload(new PayloadReader(bytes));

    SoftwareVersion expectedSoftwareVersion = new SoftwareVersion(1, 4, 11);
    assertTrue(bleDriver.isRunningBootloader());
    assertEquals(expectedSoftwareVersion, bleDriver.bootloaderVersion);
    assertEquals(SoftwareVersion.NULL, bleDriver.getFirmwareVersion());
  }

  @Test
  public void processVersionsPayload_byteElevenIs0x00_setsIsRunningBootloaderFalse() {
    String loadVersionsResponse = "01 04 0B 00 04 00 01 00 04 00 AA 00 00 00 00 00 00";
    byte[] bytes = hexStringToByteArray(loadVersionsResponse);

    assertFalse(bleDriver.isRunningBootloader());

    bleDriver.processVersionsPayload(new PayloadReader(bytes));

    SoftwareVersion expectedSoftwareVersion = new SoftwareVersion(1, 4, 11);
    assertFalse(bleDriver.isRunningBootloader());
    assertEquals(expectedSoftwareVersion, bleDriver.getFirmwareVersion());
    assertEquals(SoftwareVersion.NULL, bleDriver.bootloaderVersion);
  }

  /*
  SET USER ID
   */
  @Test
  public void setUserId_invokesDEVICE_PARAMETERS_USER_ID_withExpectedPayload() throws Exception {
    long expectedUserId = 98L;

    @NonNull
    byte[] expectedPayload =
        createByteBuffer(5).put(DEVICE_PARAMETERS_USER_ID).putInt((int) expectedUserId).array();

    bleDriver.setDeviceParameter(expectedPayload);
    verify(bleManager).setDeviceParameter(expectedPayload);
  }

  /*
  ENABLE VIBRATOR
   */
  @Test
  public void setVibratorMode_inBootloader_doesntInvokeDeviceParameters() throws FailureReason {
    bleDriver.setRunningBootloader(true);

    for (VibratorMode mode : VibratorMode.values()) {
      bleDriver.setVibratorMode(mode).test();
    }

    verify(bleManager, never()).setDeviceParameter(any());
  }

  @Test
  public void setVibratorMode_START_invokesDEVICE_PARAMETERS_VIBRATION_withPayload0x01()
      throws FailureReason {
    bleDriver.setVibratorMode(VibratorMode.START).test();

    @NonNull
    byte[] expectedPayload =
        new byte[] {GattCharacteristic.DEVICE_PARAMETERS_VIBRATION, (byte) 0x01};
    verify(bleManager).setDeviceParameter(expectedPayload);
  }

  @Test
  public void setVibratorMode_STOP_invokesDEVICE_PARAMETERS_VIBRATION_withPayload0x00()
      throws FailureReason {
    bleDriver.setVibratorMode(VibratorMode.STOP).test();

    @NonNull
    byte[] expectedPayload =
        new byte[] {GattCharacteristic.DEVICE_PARAMETERS_VIBRATION, (byte) 0x00};
    verify(bleManager).setDeviceParameter(expectedPayload);
  }

  @Test
  public void
      setVibratorMode_STOP_AND_HALT_RECORDING_invokesDEVICE_PARAMETERS_VIBRATION_withPayload0x02()
          throws FailureReason {
    bleDriver.setVibratorMode(VibratorMode.STOP_AND_HALT_RECORDING).test();

    @NonNull
    byte[] expectedPayload =
        new byte[] {GattCharacteristic.DEVICE_PARAMETERS_VIBRATION, (byte) 0x02};
    verify(bleManager).setDeviceParameter(expectedPayload);
  }

  /*
  ON FIRST CONNECTION ESTABLISHED
   */
  @Test
  public void disconect_invokesClearCache() {
    doNothing().when(bleDriver).clearCache();

    bleDriver.disconnect();

    verify(bleDriver).clearCache();
  }

  @Test
  public void disconnect_invokesBleDriverDisconnectWithoutReconnect() {
    doNothing().when(bleDriver).clearCache();

    bleDriver.disconnect();

    verify(bleManager).disconnectWithoutReconnect(any(), any());
  }

  @Test
  public void disconnect_removesCallbackAndInvokesOnDisconnectedIfLibraryReportedValid() {
    doNothing().when(bleDriver).clearCache();

    ArgumentCaptor<Function0> captor = ArgumentCaptor.forClass(Function0.class);

    bleDriver.disconnect();

    //noinspection unchecked
    verify(bleManager).disconnectWithoutReconnect(captor.capture(), any());

    verify(bleDriver, never()).onDeviceDisconnected();

    captor.getValue().invoke();

    verify(bleDriver).onDeviceDisconnected();

    verify(bleManager).setGattCallbacks(NoOpGattCallback.INSTANCE);
  }

  @Test
  public void disconnect_removesCallbackAndInvokesOnDisconnectedIfLibraryReportedInvalid() {
    doNothing().when(bleDriver).clearCache();

    ArgumentCaptor<Function0> captor = ArgumentCaptor.forClass(Function0.class);

    bleDriver.disconnect();

    //noinspection unchecked
    verify(bleManager).disconnectWithoutReconnect(any(), captor.capture());

    verify(bleDriver, never()).onDeviceDisconnected();

    captor.getValue().invoke();

    verify(bleDriver).onDeviceDisconnected();

    verify(bleManager).setGattCallbacks(NoOpGattCallback.INSTANCE);
  }

  /*
  LOAD GRU DATA INFO
   */

  @Test
  public void loadGruDataInfo_readsDataFromDEVICE_PARAMETERS_GRU_DATA_SET_INFO() throws Exception {
    PayloadWriter payloadWriter = new PayloadWriter(6);
    payloadWriter.writeByte((byte) 0);
    payloadWriter.writeByte((byte) 0x01); // true
    SoftwareVersion expectedGruVersion = new SoftwareVersion(1, 2, 3);
    payloadWriter.writeSoftwareVersion(expectedGruVersion);

    when(bleManager.getDeviceParameter(
            new byte[] {GattCharacteristic.DEVICE_PARAMETERS_GRU_DATA_SET_INFO}))
        .thenReturn(new PayloadReader(payloadWriter.getBytes()));

    bleDriver.maybeLoadGruDataInfo();

    assertTrue(bleDriver.hasValidGruData());
    assertEquals(expectedGruVersion, bleDriver.getGruDataVersion());
  }

  /*
  LOAD VERSIONS
   */

  @Test
  public void loadVersions_readsDataFromBleManagerGetDeviceVersion() throws FailureReason {
    PayloadReader payloadReader = mock(PayloadReader.class);
    SoftwareVersion expectedSwVersion = new SoftwareVersion(1, 2, 3);
    when(payloadReader.readSoftwareVersion()).thenReturn(expectedSwVersion);
    HardwareVersion expectedHwVersion = new HardwareVersion(4, 5);
    when(payloadReader.readHardwareVersion()).thenReturn(expectedHwVersion);

    when(bleManager.getDeviceVersions()).thenReturn(payloadReader);
    doNothing().when(bleDriver).validateVersionsPayload(payloadReader);
    doNothing().when(bleDriver).readBootloaderVersion();

    bleDriver.loadVersions();

    assertEquals(expectedSwVersion, bleDriver.getFirmwareVersion());
    assertEquals(expectedHwVersion, bleDriver.getHardwareVersion());
  }

  @Test
  public void loadVersions_invokesReadBootloaderVersion() throws FailureReason {
    PayloadReader payloadReader = mock(PayloadReader.class);
    SoftwareVersion expectedSwVersion = new SoftwareVersion(1, 2, 3);
    when(payloadReader.readSoftwareVersion()).thenReturn(expectedSwVersion);
    HardwareVersion expectedHwVersion = new HardwareVersion(4, 5);
    when(payloadReader.readHardwareVersion()).thenReturn(expectedHwVersion);

    when(bleManager.getDeviceVersions()).thenReturn(payloadReader);
    doNothing().when(bleDriver).validateVersionsPayload(payloadReader);
    doNothing().when(bleDriver).readBootloaderVersion();

    bleDriver.loadVersions();

    verify(bleDriver).readBootloaderVersion();
  }

  @Test
  public void loadVersions_invokesReadDspVersion() throws FailureReason {
    PayloadReader payloadReader = mock(PayloadReader.class);
    SoftwareVersion expectedSwVersion = new SoftwareVersion(1, 2, 3);
    when(payloadReader.readSoftwareVersion()).thenReturn(expectedSwVersion);
    HardwareVersion expectedHwVersion = new HardwareVersion(4, 5);
    when(payloadReader.readHardwareVersion()).thenReturn(expectedHwVersion);

    when(bleManager.getDeviceVersions()).thenReturn(payloadReader);
    doNothing().when(bleDriver).validateVersionsPayload(payloadReader);
    doNothing().when(bleDriver).readBootloaderVersion();
    doNothing().when(bleDriver).readDspVersion();

    bleDriver.loadVersions();

    verify(bleDriver).readDspVersion();
  }

  /*
  supportsReadingBootloader
   */
  @Test
  public void supportsReadingBootloader_returnsFalseIfIsRunningBootloaderIsTrue() {
    doReturn(true).when(bleDriver).isRunningBootloader();

    assertFalse(bleDriver.supportsReadingBootloader());
  }

  @Test
  public void supportsReadingBootloader_returnsTrueIfIsRunningBootloaderIsFalse() {
    doReturn(false).when(bleDriver).isRunningBootloader();

    assertFalse(bleDriver.supportsReadingBootloader());
  }

  /*
  readBootloaderVersion
   */
  @Test
  public void
      readBootloaderVersion_setsBootloaderVersionToNULL_whenSupportsReadingBootloaderReturnsFalse()
          throws FailureReason {
    SoftwareVersion testBootloaderVersion = new SoftwareVersion(1, 2, 3);
    bleDriver.bootloaderVersion = testBootloaderVersion;

    bleDriver.readBootloaderVersion();

    assertEquals(testBootloaderVersion, bleDriver.bootloaderVersion);
  }

  @Test
  public void
      readBootloaderVersion_readsBootloaderVersionFromToothbrush_whenSupportsReadingBootloaderReturnsTrue()
          throws FailureReason {
    doReturn(true).when(bleDriver).supportsReadingBootloader();

    /*
        Length : 4
    Payload : Bootloader version
    Byte 0 : Major version
    Byte 1 : Minor version
    Byte 2-3 : Revision

    4D-02-02-04-00
         */
    SoftwareVersion expectedSoftwareVersion = new SoftwareVersion(2, 2, 4);
    byte[] payload = new byte[] {DEVICE_PARAMETERS_BOOTLOADER_VERSION, 2, 2, 4, 0};
    PayloadReader response = new PayloadReader(payload);

    when(bleManager.getDeviceParameter(new byte[] {DEVICE_PARAMETERS_BOOTLOADER_VERSION}))
        .thenReturn(response);

    bleDriver.readBootloaderVersion();

    assertEquals(expectedSoftwareVersion, bleDriver.bootloaderVersion);
  }

  @Test
  public void readBootloaderVersion_setsBootloaderVersionNULL_whenPayloadIsAll0()
      throws FailureReason {
    doReturn(true).when(bleDriver).supportsReadingBootloader();

    /*
        Length : 4

    In case the bootloader versoon cannot be retrieved , all bytes are set to 0
         */
    byte[] payload = new byte[] {DEVICE_PARAMETERS_BOOTLOADER_VERSION, 0, 0, 0, 0};
    PayloadReader response = new PayloadReader(payload);

    when(bleManager.getDeviceParameter(new byte[] {DEVICE_PARAMETERS_BOOTLOADER_VERSION}))
        .thenReturn(response);

    bleDriver.readBootloaderVersion();

    assertEquals(SoftwareVersion.NULL, bleDriver.bootloaderVersion);
  }

  /*
  readDspVersions
   */
  @Test
  public void readDspVersion_doNothing_whenModelHasNoDsp() throws FailureReason {
    doReturn(ToothbrushModel.ARA).when(bleDriver).toothbrushModel();

    bleDriver.readDspVersion();

    assertEquals(DspVersion.NULL, bleDriver.dspVersion);
  }

  @Test
  public void readDspVersion_readsDspVersionFromToothbrush_whenModelHasDsp() throws FailureReason {
    doReturn(ToothbrushModel.PLAQLESS).when(bleDriver).toothbrushModel();

    DspVersion expectedDspVersion = new DspVersion(4, 1, 512);

    byte[] payload =
        new byte[] {
          DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
          0x01,
          0x04,
          0x00,
          0x01,
          0x00,
          0x00,
          0x02,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00
        };
    PayloadReader response = new PayloadReader(payload);

    when(bleManager.getDeviceParameter(new byte[] {DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS}))
        .thenReturn(response);

    bleDriver.readDspVersion();

    assertEquals(expectedDspVersion, bleDriver.dspVersion);
  }

  /*
  VALIDATE VERSIONS PAYLOAD
   */

  @Test
  public void validateVersionsPayload_readsCorrectBootloaderPayload() {
    byte[] appInBootloaderPayload = {
      0x01,
      0x12,
      0x00,
      0x00,
      0x02,
      0x00,
      0x05,
      0x00,
      0x05,
      0x00,
      (byte) 0xAA,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_11_APP_IN_BOOTLOADER,
      (byte) 0xFC,
      (byte) 0xFF,
      0x03,
      0x00,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_16_APP_IN_BOOTLOADER
    };
    PayloadReader payloadReader = new PayloadReader(appInBootloaderPayload);

    FailureReason expectedException = null;
    try {
      bleDriver.validateVersionsPayload(payloadReader);
    } catch (FailureReason e) {
      expectedException = e;
    }

    assertEquals(
        KolibreeBleDriver.DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME, payloadReader.length);
    assertNull(expectedException);
  }

  @Test
  public void validateVersionsPayload_readsCorrectNoBootloaderPayload() {
    byte[] appNotInBootloaderPayload = {
      0x01,
      0x12,
      0x00,
      0x00,
      0x02,
      0x00,
      0x05,
      0x00,
      0x05,
      0x00,
      (byte) 0xAA,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_11_APP_NOT_IN_BOOTLOADER,
      (byte) 0xFC,
      (byte) 0xFF,
      0x03,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_16_APP_NOT_IN_BOOTLOADER
    };
    PayloadReader payloadReader = new PayloadReader(appNotInBootloaderPayload);

    FailureReason expectedException = null;
    try {
      bleDriver.validateVersionsPayload(payloadReader);
    } catch (FailureReason e) {
      expectedException = e;
    }

    assertEquals(
        KolibreeBleDriver.DeviceVersionConstants.LENGTH_OF_MAIN_APP_FRAME, payloadReader.length);
    assertNull(expectedException);
  }

  @Test
  public void validateVersionsPayload_throwsExceptionOnInvalidBootloaderByte11Value() {
    byte[] invalidPayload = {
      0x01,
      0x12,
      0x00,
      0x00,
      0x02,
      0x00,
      0x05,
      0x00,
      0x05,
      0x00,
      (byte) 0xAA,
      (byte) 0xA1, // incorrect bootloader byte
      (byte) 0xFC,
      (byte) 0xFF,
      0x03,
      0x00,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_16_APP_NOT_IN_BOOTLOADER
    };
    PayloadReader payloadReader = new PayloadReader(invalidPayload);

    FailureReason expectedException = null;
    try {
      bleDriver.validateVersionsPayload(payloadReader);
    } catch (FailureReason e) {
      expectedException = e;
    }

    assertEquals(
        KolibreeBleDriver.DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME, payloadReader.length);
    assertNotNull(expectedException);
  }

  @Test
  public void validateVersionsPayload_throwsExceptionOnInvalidBootloaderByte16Value() {
    byte[] invalidPayload = {
      0x01,
      0x12,
      0x00,
      0x00,
      0x02,
      0x00,
      0x05,
      0x00,
      0x05,
      0x00,
      (byte) 0xAA,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_11_APP_IN_BOOTLOADER,
      (byte) 0xFC,
      (byte) 0xFF,
      0x03,
      0x00,
      0x02 // incorrect bootloader byte
    };
    PayloadReader payloadReader = new PayloadReader(invalidPayload);

    FailureReason expectedException = null;
    try {
      bleDriver.validateVersionsPayload(payloadReader);
    } catch (FailureReason e) {
      expectedException = e;
    }

    assertEquals(
        KolibreeBleDriver.DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME, payloadReader.length);
    assertNotNull(expectedException);
  }

  @Test
  public void validateVersionsPayload_throwsExceptionOnInvalidLength() {
    byte[] invalidPayload = {
      0x01,
      0x12,
      0x00,
      0x00,
      0x02,
      0x00,
      0x05,
      0x00,
      0x05,
      0x00,
      (byte) 0xAA,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_11_APP_IN_BOOTLOADER,
      (byte) 0xFC,
      (byte) 0xFF,
      0x03,
      0x00,
      KolibreeBleDriver.DeviceVersionConstants.BYTE_16_APP_IN_BOOTLOADER,
      0x02 // extra byte
    };
    PayloadReader payloadReader = new PayloadReader(invalidPayload);

    FailureReason expectedException = null;
    try {
      bleDriver.validateVersionsPayload(payloadReader);
    } catch (FailureReason e) {
      expectedException = e;
    }

    assertEquals(
        KolibreeBleDriver.DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME + 1,
        payloadReader.length);
    assertNotNull(expectedException);
  }

  /*
  ON FIRST CONNECTION ESTABLISHED
   */

  @Test
  public void
      onFirstConnectionEstablished_isFirstConnectionFalse_invokesLoadVersions_neverInvokesLoadGruDataOrLoadSensorCalibration()
          throws Exception {
    bleDriver.isFirstConnection = false;

    prepareOnFirstConnectionEstablished(false);

    bleDriver.onFirstConnectionEstablished();

    verify(bleDriver).onFirstConnectionEstablished();
    verify(bleDriver).loadVersions();

    verify(bleDriver, never()).maybeLoadGruDataInfo();
    verify(bleDriver, never()).loadSensorCalibration();
  }

  @Test
  public void
      onFirstConnectionEstablished_isFirstConnectionTrue_isInBootloaderTrue_neverInvokesLoadGruDataOrLoadSensorCalibration()
          throws Exception {
    bleDriver.isFirstConnection = true;

    prepareOnFirstConnectionEstablished(true);

    bleDriver.onFirstConnectionEstablished();

    verify(bleDriver).onFirstConnectionEstablished();
    verify(bleDriver).loadVersions();

    verify(bleDriver, never()).maybeLoadGruDataInfo();
    verify(bleDriver, never()).loadSensorCalibration();
  }

  @Test
  public void
      onFirstConnectionEstablished_isFirstConnectionTrue_isInBootloaderFalse_invokesLoadGruData()
          throws Exception {
    bleDriver.isFirstConnection = true;

    prepareOnFirstConnectionEstablished(false);

    bleDriver.onFirstConnectionEstablished();

    verify(bleDriver).maybeLoadGruDataInfo();
  }

  @Test
  public void
      onFirstConnectionEstablished_isFirstConnectionTrue_isInBootloaderFalse_invokesLoadSensorCalibration()
          throws Exception {
    bleDriver.isFirstConnection = true;

    prepareOnFirstConnectionEstablished(false);

    bleDriver.onFirstConnectionEstablished();

    verify(bleDriver).loadSensorCalibration();
  }

  @Test
  public void
      onFirstConnectionEstablished_isFirstConnectionTrue_isInBootloaderFalse_createsOfflineBrushingsExtractor()
          throws Exception {
    bleDriver.isFirstConnection = true;

    prepareOnFirstConnectionEstablished(false);

    assertNull(bleDriver.offlineBrushingsExtractor);

    bleDriver.onFirstConnectionEstablished();

    verify(bleDriver).injectOfflineBrushingExtractor();
  }

  /*
  LOAD SENSOR CALIBRATION
   */

  @Test
  public void loadSensorCalibration_fillsCalibrationDataWithExpectedLength() throws Exception {
    setupMagnetometerResponse();

    assertNull(bleDriver.calibrationData);

    doNothing().when(bleDriver).setMagnetometerCalibration(any(Matrix.class), any(Vector.class));

    bleDriver.loadSensorCalibration();

    assertEquals(bleDriver.calibrationDataSize(), bleDriver.calibrationData.length);
  }

  @Test
  public void loadSensorCalibration_invokesSetMagnetometerCalibration() throws Exception {
    setupMagnetometerResponse();

    doNothing().when(bleDriver).setMagnetometerCalibration(any(Matrix.class), any(Vector.class));

    bleDriver.loadSensorCalibration();

    verify(bleDriver).setMagnetometerCalibration(any(Matrix.class), any(Vector.class));
  }

  @Test(expected = FailureReason.class)
  public void
      loadSensorCalibration_alwaysFailsLoadingMagnetometerCalibration_ThrowsErrorAfter2Retries()
          throws Exception {
    doThrow(new FailureReason("Test forced error"))
        .when(bleDriver)
        .loadMagnetometerCalibration(anyByte());

    bleDriver.loadSensorCalibration();

    verify(bleDriver, times(LOAD_SENSOR_CALIBRATION_MAX_RETRIES + 1))
        .loadMagnetometerCalibration((byte) 0);
  }

  @Test
  public void
      loadSensorCalibration_loadingMagnetometerCalibrationSucceedsAfterSomeRetries_succeeds()
          throws Exception {
    setupMagnetometerOffsetResponse();

    int[] invokedCounter = new int[1];
    doAnswer(
            (Answer<PayloadReader>)
                invocation -> {
                  invokedCounter[0]++;

                  if (invokedCounter[0] < LOAD_SENSOR_CALIBRATION_MAX_RETRIES) {
                    throw new FailureReason("Test forced error");
                  }

                  return createDefaultPayloadReader();
                })
        .when(bleDriver)
        .loadMagnetometerCalibration((byte) 0);

    doReturn(createDefaultPayloadReader()).when(bleDriver).loadMagnetometerCalibration((byte) 1);
    doReturn(createDefaultPayloadReader()).when(bleDriver).loadMagnetometerCalibration((byte) 2);

    doNothing().when(bleDriver).setMagnetometerCalibration(any(Matrix.class), any(Vector.class));

    bleDriver.loadSensorCalibration();

    assertEquals(LOAD_SENSOR_CALIBRATION_MAX_RETRIES, invokedCounter[0]);
    verify(bleDriver).loadMagnetometerCalibration((byte) 1);
    verify(bleDriver).loadMagnetometerCalibration((byte) 2);

    assertEquals(bleDriver.calibrationDataSize(), bleDriver.calibrationData.length);
    verify(bleDriver).setMagnetometerCalibration(any(Matrix.class), any(Vector.class));
  }

  /*
  ON LINK LOSS OCCURRED
   */

  @Test
  public void onLinkLossOcurred_setsItsFirstconnectionToFalse() {
    bleDriver.isFirstConnection = true;

    bleDriver.onLinkLossOccurred(mock(BluetoothDevice.class));

    assertFalse(bleDriver.isFirstConnection);
  }

  @Test
  public void onLinkLossOcurred_clearsDisposables() {
    Disposable disposable = mock(Disposable.class);

    bleDriver.disposables.add(disposable);

    bleDriver.onLinkLossOccurred(mock(BluetoothDevice.class));

    verify(disposable).dispose();
  }

  /*
  forceEmitVibrationState
   */
  @Test
  public void forceEmitVibrationState_doesNothingIfDriverDoesNotSupportPolling()
      throws FailureReason {
    bleDriver.supportsBrushingEventsPolling = false;

    bleDriver.forceEmitVibrationState().test().assertComplete();

    verify(bleDriver, never()).getDeviceParameter(any());
  }

  @Test
  public void forceEmitVibrationState_doesNothing_ifDriverSupportsPollingButIsRunningBootloader()
      throws FailureReason {
    bleDriver.supportsBrushingEventsPolling = true;

    doReturn(true).when(bleDriver).isRunningBootloader();

    bleDriver.forceEmitVibrationState().test().assertComplete();

    verify(bleDriver, never()).getDeviceParameter(any());
  }

  @Test
  public void
      forceEmitVibrationState_sends0x210x01Command_ifDriverSupportsPollingAndIsNotRunningBootloader()
          throws FailureReason {
    bleDriver.supportsBrushingEventsPolling = true;

    doReturn(false).when(bleDriver).isRunningBootloader();

    doReturn(new PayloadReader(new byte[0])).when(bleDriver).getDeviceParameter(any());

    bleDriver.forceEmitVibrationState().test().assertComplete();

    verify(bleDriver)
        .getDeviceParameter(
            new byte[] {GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_EVENTS, 0x01});
  }

  /*
  notifyConnectionEstablishedCompletable
   */
  @Test
  public void
      notifyConnectionEstablishedCompletable_invokesOnConnectionEstablished_andThenForceEmitVibrationState()
          throws FailureReason {
    CompletableSubject forceEmitSubject = CompletableSubject.create();
    doReturn(forceEmitSubject).when(bleDriver).forceEmitVibrationState();

    boolean[] verified = new boolean[] {false};
    doAnswer(
            (Answer<Void>)
                invocation -> {
                  assertFalse(forceEmitSubject.hasObservers());

                  verified[0] = true;
                  return null;
                })
        .when(driverListener)
        .onConnectionEstablished();

    TestObserver<Void> observer =
        bleDriver.notifyConnectionEstablishedCompletable().test().assertNotComplete();

    assertTrue(forceEmitSubject.hasObservers());
    forceEmitSubject.onComplete();

    observer.assertComplete();

    assertTrue(verified[0]);
  }

  @Test
  public void notifyConnectionEstablishedCompletable_invokesDisconnectOnError()
      throws FailureReason {
    Throwable expectedError = new FailureReason("Test forced error");
    doThrow(expectedError).when(driverListener).onConnectionEstablished();

    CompletableSubject forceEmitSubject = CompletableSubject.create();
    doReturn(forceEmitSubject).when(bleDriver).forceEmitVibrationState();

    bleDriver.notifyConnectionEstablishedCompletable().test().assertError(expectedError);

    assertFalse(forceEmitSubject.hasObservers());

    verify(bleDriver).disconnect();
  }

  /*
  ON DEVICE READY
   */

  @Test
  public void
      onDeviceReady_invokesOnDeviceReadyCompletableFollowedByNotifyConnectionEstablishedCompletable() {
    CompletableSubject onDeviceReadySubject = CompletableSubject.create();
    doReturn(onDeviceReadySubject).when(bleDriver).onDeviceReadyCompletable();

    CompletableSubject connectionEstablishedSubject = CompletableSubject.create();
    doReturn(connectionEstablishedSubject).when(bleDriver).notifyConnectionEstablishedCompletable();

    bleDriver.onDeviceReady(mock(BluetoothDevice.class));

    assertTrue(onDeviceReadySubject.hasObservers());
    assertFalse(connectionEstablishedSubject.hasObservers());

    onDeviceReadySubject.onComplete();

    assertTrue(connectionEstablishedSubject.hasObservers());
  }

  @Test
  public void onDeviceReady_onDeviceReadyCompletableError_invokesOnDeviceReadyError() {
    Throwable expectedException = new Exception("Test forced error");
    doReturn(Completable.error(expectedException)).when(bleDriver).onDeviceReadyCompletable();

    doNothing()
        .when(bleDriver)
        .onDeviceReadyError(any(BluetoothDevice.class), any(Throwable.class));

    BluetoothDevice expectedDevice = mock(BluetoothDevice.class);
    bleDriver.onDeviceReady(expectedDevice);

    verify(bleDriver).onDeviceReadyError(expectedDevice, expectedException);
  }

  /*
  ON DEVICE READY ERROR
   */

  @Test
  @Ignore("Remove ignore when https://jira.kolibree.com/browse/KLTB002-6962 is done")
  public void onDeviceReadyError_connectEmitterIsNull_invokesReconnect() {
    assertNull(bleDriver.weakConnectEmitter.get());

    BluetoothDevice expectedDevice = mock(BluetoothDevice.class);
    bleDriver.onDeviceReadyError(expectedDevice, new Exception("Test forced error"));

    verify(bleDriver).internalReconnect(expectedDevice);
  }

  @Test
  public void onDeviceReadyError_connectEmitterIsNotNull_invokesEmitterTryOnError() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    bleDriver.weakConnectEmitter = new WeakReference<>(emitter);

    BluetoothDevice expectedDevice = mock(BluetoothDevice.class);
    bleDriver.onDeviceReadyError(expectedDevice, new Exception("Test forced error"));

    verify(emitter).tryOnError(any(Throwable.class));
  }

  @Test
  public void onDeviceReadyError_connectEmitterIsNotNull_nullifiesWeakEmitter() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    bleDriver.weakConnectEmitter = new WeakReference<>(emitter);

    BluetoothDevice expectedDevice = mock(BluetoothDevice.class);
    bleDriver.onDeviceReadyError(expectedDevice, new Exception("Test forced error"));

    assertNull(bleDriver.weakConnectEmitter.get());
  }

  /*
  RECONNECT
   */

  @Test
  public void reconnect_connectionInProgressTrue_returnsConnectCompletable() {
    Completable expectedCompletable = Completable.never();
    bleDriver.connectCompletable = expectedCompletable;

    doReturn(true).when(bleDriver).isConnectionInProgress();

    Completable reconnectCompletable = bleDriver.reconnect();

    assertEquals(expectedCompletable, reconnectCompletable);
  }

  @Test
  public void reconnect_connectionInProgressFalse_runsInternalReconnectWithGivenDevice() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    doReturn(false).when(bleDriver).isConnectionInProgress();

    Completable expectedCompletable = Completable.complete();
    doReturn(device).when(bleDriver).getBluetoothDevice(anyString());
    doReturn(expectedCompletable).when(bleDriver).internalReconnect(device);
    Completable reconnectCompletable = bleDriver.reconnect();

    assertEquals(expectedCompletable, reconnectCompletable);
  }

  /*
  IS CONNECTION IN PROGRESS
   */

  @Test
  public void isConnectionInProgress_emitterIsNull_returnsFalse() {
    assertNull(bleDriver.weakConnectEmitter.get());

    assertFalse(bleDriver.isConnectionInProgress());
  }

  @Test
  public void isConnectionInProgress_emitterNotNull_connectCompletableNull_returnsFalse() {
    bleDriver.weakConnectEmitter = new WeakReference<>(mock(CompletableEmitter.class));

    assertNull(bleDriver.connectCompletable);

    assertFalse(bleDriver.isConnectionInProgress());
  }

  @Test
  public void
      isConnectionInProgress_emitterNotNull_connectCompletableNotNullButDisposed_returnsTrue() {
    bleDriver.weakConnectEmitter = new WeakReference<>(mock(CompletableEmitter.class));

    bleDriver.connectCompletable = Completable.never();

    assertTrue(bleDriver.isConnectionInProgress());
  }

  /*
  INTERNAL RECONNECT
   */

  @Test
  public void internalReconnect_runsInnerConnectCompletableAfterDisconnectIsCompleted() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    CompletableSubject subject = spy(CompletableSubject.create());
    doReturn(subject).when(bleDriver).innerConnectCompletable(device);

    bleDriver.internalReconnect(device).test();

    ArgumentCaptor<Function0> captor = ArgumentCaptor.forClass(Function0.class);
    //noinspection unchecked
    verify(bleManager).disconnectWithoutReconnect(captor.capture(), any());

    assertFalse(subject.hasObservers());
    captor.getValue().invoke();
    verify(bleDriver).innerConnectCompletable(device);

    assertTrue(subject.hasObservers());
  }

  @Test
  public void internalReconnect_clearsCacheAfterDisconnectIsCompleted() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    CompletableSubject subject = spy(CompletableSubject.create());
    doReturn(subject).when(bleDriver).innerConnectCompletable(device);

    bleDriver.internalReconnect(device).test();

    ArgumentCaptor<Function0> captor = ArgumentCaptor.forClass(Function0.class);
    //noinspection unchecked
    verify(bleManager).disconnectWithoutReconnect(captor.capture(), any());

    assertFalse(subject.hasObservers());
    captor.getValue().invoke();
    verify(bleDriver).innerConnectCompletable(device);

    verify(bleDriver).clearCache();
  }

  /*
  CONNECT
   */

  @Test(expected = Throwable.class)
  public void connect_crashOnError() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    doReturn(device).when(bleDriver).getBluetoothDevice(anyString());

    doReturn(Completable.error(new Throwable("Test forced error")))
        .when(bleDriver)
        .connectCompletable(device);

    bleDriver.connect();
  }

  /*
  INNER CONNECT COMPLETABLE
   */

  @Test
  public void innerConnectCompletable_connectCompletableIsNull_createsNewInstance() {
    assertNull(bleDriver.connectCompletable);

    assertNotNull(bleDriver.innerConnectCompletable(mock(BluetoothDevice.class)));
  }

  @Test
  public void
      innerConnectCompletable_multipleInvocations_withoutUnsubscribing_returnSameInstance() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    assertEquals(
        bleDriver.innerConnectCompletable(device), bleDriver.innerConnectCompletable(device));
  }

  @Test
  public void innerConnectCompletable_storesEmitter() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    when(bleManager.connect(device)).thenReturn(createConnectRequest(device));

    assertNull(bleDriver.weakConnectEmitter.get());

    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    bleDriver.innerConnectCompletable(device).test();

    assertNotNull(bleDriver.weakConnectEmitter.get());
  }

  @Test
  public void
      innerConnectCompletable_multipleInvocations_emitterCompletes_returnDifferentInstance() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    Completable initialCompletable = bleDriver.innerConnectCompletable(device);

    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    TestObserver<Void> observer = initialCompletable.test();

    assertEquals(initialCompletable, bleDriver.innerConnectCompletable(device));

    ArgumentCaptor<CompletableEmitter> emitterCaptor =
        ArgumentCaptor.forClass(CompletableEmitter.class);
    verify(bleDriver).internalConnect(eq(device), emitterCaptor.capture());
    CompletableEmitter emitter = emitterCaptor.getValue();

    emitter.onComplete();

    assertNotEquals(initialCompletable, bleDriver.innerConnectCompletable(device));
  }

  @Test
  public void innerConnectCompletable_emitterCompletes_nullifiesWeakEmitter() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    bleDriver.innerConnectCompletable(device).test();

    assertNotNull(bleDriver.weakConnectEmitter.get());

    ArgumentCaptor<CompletableEmitter> emitterCaptor =
        ArgumentCaptor.forClass(CompletableEmitter.class);
    verify(bleDriver).internalConnect(eq(device), emitterCaptor.capture());
    CompletableEmitter emitter = emitterCaptor.getValue();

    emitter.onComplete();

    assertNull(bleDriver.weakConnectEmitter.get());
  }

  @Test
  public void innerConnectCompletable_emitterCompletes_nullifiesConnectCompletable() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    bleDriver.innerConnectCompletable(device).test();

    assertNotNull(bleDriver.connectCompletable);

    ArgumentCaptor<CompletableEmitter> emitterCaptor =
        ArgumentCaptor.forClass(CompletableEmitter.class);
    verify(bleDriver).internalConnect(eq(device), emitterCaptor.capture());
    CompletableEmitter emitter = emitterCaptor.getValue();

    emitter.onComplete();

    assertNull(bleDriver.connectCompletable);
  }

  @Test
  public void innerConnectCompletable_multipleInvocations_emitterError_returnDifferentInstance() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    Completable initialCompletable = bleDriver.innerConnectCompletable(device);

    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    TestObserver<Void> observer = initialCompletable.test();

    assertEquals(initialCompletable, bleDriver.innerConnectCompletable(device));

    ArgumentCaptor<CompletableEmitter> emitterCaptor =
        ArgumentCaptor.forClass(CompletableEmitter.class);
    verify(bleDriver).internalConnect(eq(device), emitterCaptor.capture());
    CompletableEmitter emitter = emitterCaptor.getValue();

    emitter.tryOnError(new Exception("Test forced error"));

    assertNotEquals(initialCompletable, bleDriver.innerConnectCompletable(device));
  }

  @Test
  public void innerConnectCompletable_setsGattCallback() {
    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    BluetoothDevice device = mock(BluetoothDevice.class);
    bleDriver.innerConnectCompletable(device).test();

    verify(bleManager).setGattCallbacks(bleDriver);
  }

  @Test
  public void innerConnectCompletable_invokesInternalConnect() {
    doNothing()
        .when(bleDriver)
        .internalConnect(any(BluetoothDevice.class), any(CompletableEmitter.class));

    BluetoothDevice device = mock(BluetoothDevice.class);
    bleDriver.innerConnectCompletable(device).test();

    verify(bleDriver).internalConnect(eq(device), any(CompletableEmitter.class));
  }

  /*
  INTERNAL CONNECT
   */

  @Test
  public void internalConnect_invokesConnect() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    when(bleManager.connect(device)).thenReturn(createConnectRequest(device));

    bleDriver.internalConnect(device, emitter);

    verify(bleManager).connect(device);
  }

  @Test
  public void internalConnect_setsAutoConnectTrue() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    assertTrue(connectRequest.isAutoConnect());
  }

  @Test
  public void internalConnect_canRetryTrue() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    assertTrue(connectRequest.isRetry());
  }

  @Test
  public void internalConnect_hasRetryDelay() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    assertTrue(connectRequest.retryDelay() > 0);
  }

  @Test
  public void internalConnect_done_emitterIsDisposedTrue_doesNothing() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    when(emitter.isDisposed()).thenReturn(true);

    connectRequest.succeed();

    verify(emitter, never()).onComplete();
  }

  @Test
  public void internalConnect_done_emitterIsDisposedFalse_invokesEmitterOnComplete() {
    Timber.d("In test");
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    when(emitter.isDisposed()).thenReturn(false);

    Timber.d("Pre succeed");
    connectRequest.succeed();

    verify(emitter).onComplete();
  }

  @Test
  public void internalConnect_fail_invokesTryOnError() {
    CompletableEmitter emitter = mock(CompletableEmitter.class);
    BluetoothDevice device = mock(BluetoothDevice.class);

    ConnectRequestStub connectRequest = createConnectRequest(device);
    when(bleManager.connect(device)).thenReturn(connectRequest);

    bleDriver.internalConnect(device, emitter);

    when(emitter.isDisposed()).thenReturn(false);

    connectRequest.fail();

    verify(emitter).tryOnError(any(ConnectionEstablishException.class));
  }

  @NonNull
  private ConnectRequestStub createConnectRequest(BluetoothDevice device) {
    ConnectRequestStub connectRequest = new ConnectRequestStub(device);

    connectRequest.setTestManager(bleManager);

    return connectRequest;
  }

  /*
  ON DEVICE READY COMPLETABLE
   */

  @Test
  public void onDeviceReadyCompletable_propagatesOnFirstConnectionEstablishedError()
      throws FailureReason {
    Throwable expectedError = new FailureReason("Test forced error");
    doAnswer(
            (Answer<Void>)
                invocation -> {
                  throw expectedError;
                })
        .when(bleDriver)
        .onFirstConnectionEstablished();

    doReturn(Schedulers.io()).when(bleDriver).onDeviceReadyScheduler();

    bleDriver.onDeviceReadyCompletable().test().assertError(expectedError);
  }

  @Test
  public void onDeviceReadyCompletable_neverInvokesNotifyConnectionEstablished()
      throws FailureReason {
    doNothing().when(bleDriver).onFirstConnectionEstablished();

    doReturn(Schedulers.io()).when(bleDriver).onDeviceReadyScheduler();

    bleDriver.onDeviceReadyCompletable().test().assertComplete();

    verify(bleDriver, never()).notifyConnectionEstablishedCompletable();
  }

  /*
  CONNECT COMPLETABLE
   */

  @Test
  public void
      connectCompletable_bleManagerHasDeviceEqualToParameter_bleManagerIsReady_isFirstConnection_returnsOnDeviceReadyCompletableFollowedByNotifyConnectionEstablishedCompletable() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    when(bleManager.getBluetoothDevice()).thenReturn(device);
    when(bleManager.isReady()).thenReturn(true);
    bleDriver.isFirstConnection = true;

    CompletableSubject onDeviceReadySubject = CompletableSubject.create();
    doReturn(onDeviceReadySubject).when(bleDriver).onDeviceReadyCompletable();

    CompletableSubject connectionEstablishedSubject = CompletableSubject.create();
    doReturn(connectionEstablishedSubject).when(bleDriver).notifyConnectionEstablishedCompletable();

    bleDriver.connectCompletable(device).test();

    assertTrue(onDeviceReadySubject.hasObservers());
    assertFalse(connectionEstablishedSubject.hasObservers());

    onDeviceReadySubject.onComplete();

    assertTrue(connectionEstablishedSubject.hasObservers());
  }

  @Test
  public void
      connectCompletable_bleManagerHasDeviceEqualToParameter_bleManagerIsReady_isFirstConnectionFalse_returnsNotifyConnectionEstablishedCompletable() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    when(bleManager.getBluetoothDevice()).thenReturn(device);
    when(bleManager.isReady()).thenReturn(true);
    bleDriver.isFirstConnection = false;

    CompletableSubject connectionEstablishedSubject = CompletableSubject.create();
    doReturn(connectionEstablishedSubject).when(bleDriver).notifyConnectionEstablishedCompletable();

    bleDriver.connectCompletable(device).test();

    assertTrue(connectionEstablishedSubject.hasObservers());
  }

  @Test
  public void
      connectCompletable_bleManagerHasDeviceEqualToParameter_bleManagerIsReadyFalse_returnsInnerConnectCompletable() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    when(bleManager.getBluetoothDevice()).thenReturn(device);
    when(bleManager.isReady()).thenReturn(false);

    Completable expectedCompletable = Completable.complete();
    doReturn(expectedCompletable).when(bleDriver).innerConnectCompletable(device);

    assertEquals(expectedCompletable, bleDriver.connectCompletable(device));
  }

  @Test
  public void
      connectCompletable_bleManagerHasDeviceDifferentToParameter_returnsInnerConnectCompletable() {
    when(bleManager.getBluetoothDevice()).thenReturn(mock(BluetoothDevice.class));

    BluetoothDevice parameterDevice = mock(BluetoothDevice.class);
    Completable expectedCompletable = Completable.complete();
    doReturn(expectedCompletable).when(bleDriver).innerConnectCompletable(parameterDevice);

    assertEquals(expectedCompletable, bleDriver.connectCompletable(parameterDevice));
  }

  @Test
  public void connectCompletable_bleManagerHasDeviceNull_returnsInnerConnectCompletable() {
    when(bleManager.getBluetoothDevice()).thenReturn(null);

    BluetoothDevice parameterDevice = mock(BluetoothDevice.class);
    Completable expectedCompletable = Completable.complete();
    doReturn(expectedCompletable).when(bleDriver).innerConnectCompletable(parameterDevice);

    assertEquals(expectedCompletable, bleDriver.connectCompletable(parameterDevice));
  }

  /*
  ON DEVICE CONNECTING
   */

  @Test
  public void onDeviceConnecting_invokesListenerOnConnectionEstablishing() {
    doNothing().when(bleDriver).runOnListenerScheduler(any(Runnable.class));

    bleDriver.onDeviceConnecting(mock(BluetoothDevice.class));

    ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(bleDriver).runOnListenerScheduler(runnableArgumentCaptor.capture());

    runnableArgumentCaptor.getValue().run();

    verify(driverListener).onConnectionEstablishing();
  }

  /*
  ON DEVICE CONNECTED
   */

  @Test
  public void onDeviceConnected_invokesListenerOnConnectionEstablishing() {
    doNothing().when(bleDriver).runOnListenerScheduler(any(Runnable.class));

    bleDriver.onDeviceConnected(mock(BluetoothDevice.class));

    ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(bleDriver).runOnListenerScheduler(runnableArgumentCaptor.capture());

    runnableArgumentCaptor.getValue().run();

    verify(driverListener).onConnectionEstablishing();
  }

  /*
  ON DEVICE DISCONNECTED
   */

  @Test
  public void onDeviceDisconnected_setsItsFirstconnectionToTrue() {
    bleDriver.isFirstConnection = false;

    bleDriver.onDeviceDisconnected(mock(BluetoothDevice.class));

    assertTrue(bleDriver.isFirstConnection);
  }

  @Test
  public void onDeviceDisconnected_clearsDisposables() {
    Disposable disposable = mock(Disposable.class);

    bleDriver.disposables.add(disposable);

    bleDriver.onDeviceDisconnected(mock(BluetoothDevice.class));

    verify(disposable).dispose();
  }

  @Test
  public void onDeviceDisconnected_invokesListenerOnDisconnected() {
    doNothing().when(bleDriver).runOnListenerScheduler(any(Runnable.class));

    bleDriver.onDeviceDisconnected(mock(BluetoothDevice.class));

    ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(bleDriver).runOnListenerScheduler(runnableArgumentCaptor.capture());

    runnableArgumentCaptor.getValue().run();

    verify(driverListener).onDisconnected();
  }

  /*
  GET NEXT RECORD
   */

  @Test
  public void getNextRecord_redirectsToOfflineBrushingsInteractor_subscribesOnBtScheduler() {
    OfflineBrushing expectedSession = mock(OfflineBrushing.class);
    OfflineBrushingsExtractor offlineBrushingsExtractor = mockOfflineBrushingExtractor();
    Single<OfflineBrushing> spySingle = spy(Single.just(expectedSession));
    when(offlineBrushingsExtractor.popRecord()).thenReturn(spySingle);

    assertEquals(expectedSession, bleDriver.getNextRecord());
    verify(spySingle).subscribeOn(bluetoothScheduler);
  }

  /*
  DELETE NEXT RECORD
   */

  @Test
  public void deleteNextRecord_redirectsToOfflineBrushingsInteractor_subscribesOnBtScheduler() {
    OfflineBrushingsExtractor offlineBrushingsExtractor = mockOfflineBrushingExtractor();
    Completable completable = spy(Completable.complete());
    when(offlineBrushingsExtractor.deleteRecord()).thenReturn(completable);

    bleDriver.deleteNextRecord().test();

    verify(offlineBrushingsExtractor).deleteRecord();
    verify(completable).subscribeOn(bluetoothScheduler);
  }

  /*
  GET REMAINING RECORD COUNT
   */

  @Test
  public void
      getRemainingRecordCount_redirectsToOfflineBrushingsInteractor_subscribesOnBtScheduler() {
    OfflineBrushingsExtractor offlineBrushingsExtractor = mockOfflineBrushingExtractor();
    Single<Integer> mockSingle = mock(Single.class);
    when(mockSingle.subscribeOn(bluetoothScheduler)).thenReturn(mockSingle);
    when(offlineBrushingsExtractor.recordCount()).thenReturn(mockSingle);

    assertEquals(mockSingle, bleDriver.getRemainingRecordCount());
    verify(mockSingle).subscribeOn(bluetoothScheduler);
  }

  /*
  ON SENSOR CONTROL

  We don't really test the payload here, I copy&pasted from the diver implementation

  It does serve tho as a test of what to expect from the payload, if we decide to change it one day

  We do test the enable/disable notifications.
   */

  @Test
  public void
      onSensorControl_svmOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayloadWithEnableSensorDetectionOn() {
    boolean svm = true;
    boolean rnn = false;
    boolean raw = false;
    boolean handedness = false;

    bleDriver.onSensorControl(svm, rnn, raw, handedness);

    @NonNull byte[] expectedPayload = sensorPayload(svm, rnn, raw, handedness);
    verify(bleManager).writeSensorStreamingControl(expectedPayload);
  }

  @Test
  public void
      onSensorControl_rnnOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
    boolean svm = false;
    boolean rnn = true;
    boolean raw = false;
    boolean handedness = false;

    bleDriver.onSensorControl(svm, rnn, raw, handedness);

    @NonNull byte[] expectedPayload = sensorPayload(svm, rnn, raw, handedness);
    verify(bleManager).writeSensorStreamingControl(expectedPayload);
  }

  @Test
  public void
      onSensorControl_osmSvmRnnOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
    boolean svm = true;
    boolean rnn = true;
    boolean raw = false;
    boolean handedness = false;

    bleDriver.onSensorControl(svm, rnn, raw, handedness);

    @NonNull byte[] expectedPayload = sensorPayload(svm, rnn, raw, handedness);
    verify(bleManager).writeSensorStreamingControl(expectedPayload);
  }

  @Test
  public void
      onSensorControl_osmSvmRnnFalse_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
    boolean svm = false;
    boolean rnn = false;
    boolean raw = false;
    boolean handedness = false;

    bleDriver.onSensorControl(svm, rnn, raw, handedness);

    @NonNull byte[] expectedPayload = sensorPayload(svm, rnn, raw, handedness);
    verify(bleManager).writeSensorStreamingControl(expectedPayload);
  }

  @Test
  public void
      onSensorControl_rawDataOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
    boolean svm = false;
    boolean rnn = false;
    boolean raw = true;
    boolean handedness = false;

    bleDriver.onSensorControl(svm, rnn, raw, handedness);

    @NonNull byte[] expectedPayload = sensorPayload(svm, rnn, raw, handedness);
    verify(bleManager).writeSensorStreamingControl(expectedPayload);
  }

  /*
  RUN ON LISTENER SCHEDULER COMPLETABLE
   */

  @Test
  public void runOnListenerSchedulerCompletable_handlesRunnableExceptionProperly()
      throws InterruptedException {
    Runnable mockedRunnable = mock(Runnable.class);
    doThrow(new RuntimeException("Mock exception")).when(mockedRunnable).run();

    TestObserver testSubscriber =
        bleDriver.runOnListenerSchedulerCompletable(mockedRunnable).test().await();

    testSubscriber.assertError(RuntimeException.class);
    verify(mockedRunnable, times(2)).run();
    verify(bleDriver).disconnect();
  }

  /*
  plaqlessRawDataNotifications
   */
  @Test
  public void plaqlessRawDataNotifications_returns_Flowable_CommandNotSupportedException() {
    bleDriver.plaqlessRawDataNotifications().test().assertError(CommandNotSupportedException.class);
  }

  /*
  plaqlessNotifications
   */
  @Test
  public void plaqlessNotifications_returns_Flowable_CommandNotSupportedException() {
    bleDriver.plaqlessNotifications().test().assertError(CommandNotSupportedException.class);
  }

  /*
  plaqlessRingLedState
   */
  @Test
  public void plaqlessRingLedState_returns_Flowable_CommandNotSupportedException() {
    bleDriver.plaqlessRingLedState().test().assertError(CommandNotSupportedException.class);
  }

  /*
  disableMultiUserMode
   */
  @Test
  public void disableMultiUserMode_dont_set_device_parameter() throws Exception {
    bleDriver.disableMultiUserMode();
    verify(bleManager, never()).setDeviceParameter(any());
  }

  /*
  queryRealName
   */
  @Test
  public void queryRealName_whenBleDriverGetDeviceParameterSucceed_returnsExpectedName()
      throws Exception {
    String testName = "testName";
    doReturn(new PayloadReader(ParameterSet.setToothbrushNameParameterPayload(testName)))
        .when(bleDriver)
        .getDeviceParameter(ParameterSet.getToothbrushNameParameterPayload());

    assertEquals(bleDriver.queryRealName(), testName);
  }

  @Test(expected = FailureReason.class)
  public void queryRealName_whenBleDriverFails_throwsFailureReason() throws Exception {
    FailureReason testFailure = new FailureReason("test");
    doThrow(testFailure).when(bleDriver).getDeviceParameter(any());
    bleDriver.queryRealName();
  }

  /*
  enableOverpressureDetector
   */

  @Test
  public void enableOverpressureDetector_emitsCommandNotSupportedException() {
    bleDriver
        .enableOverpressureDetector(true)
        .test()
        .assertNotComplete()
        .assertError(CommandNotSupportedException.class);
  }

  /*
  isOverpressureDetectorEnabled
   */

  @Test
  public void isOverpressureDetectorEnabled_emitsFalse() {
    bleDriver.isOverpressureDetectorEnabled().test().assertValue(false);
  }

  /*
  enablePickupDetector
   */

  @Test
  public void enablePickupDetector_emitsCommandNotSupportedException() {
    bleDriver
        .enablePickupDetector(true)
        .test()
        .assertNotComplete()
        .assertError(CommandNotSupportedException.class);
  }

  /*
  UTILS
   */

  private byte[] sensorPayload(
      boolean svmOn, boolean rnnOn, boolean rawDataOn, boolean handedness) {
    Bitmask streamingBitmask = new Bitmask();
    Bitmask detectionBitmask = new Bitmask();

    streamingBitmask.set(0, rawDataOn).set(1, false).set(2, svmOn || rnnOn);
    detectionBitmask.set(1, false).set(2, rnnOn || svmOn); // Ara uses RNN as SVM

    return new byte[] {
      streamingBitmask.get(),
      detectionBitmask.get(),
      50, // Default value
      (byte) (handedness ? 0x01 : 0x00)
    };
  }

  @NonNull
  private ByteBuffer createByteBuffer(int capacity) {
    return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
  }

  private PayloadReader createDefaultPayloadReader() {
    return createPayloadReader("42-00-9F-32-DD-3F-C8-F5-04-3D-BF-7E-26-3D");
  }

  private PayloadReader createPayloadReader(String s) {
    return new PayloadReader(hexStringToByteArray(s.replace("-", " ")));
  }

  /*
  Responses from real toothbrush
   */
  private void setupMagnetometerResponse() throws FailureReason {
    doReturn(createPayloadReader("42-00-9F-32-DD-3F-C8-F5-04-3D-BF-7E-26-3D"))
        .when(bleDriver)
        .loadMagnetometerCalibration((byte) 0);

    doReturn(createPayloadReader("42-01-36-CF-12-3D-FD-C0-02-40-39-69-64-BD"))
        .when(bleDriver)
        .loadMagnetometerCalibration((byte) 1);

    doReturn(createPayloadReader("42-02-13-0E-DC-3C-E8-01-23-BD-92-ED-75-3F"))
        .when(bleDriver)
        .loadMagnetometerCalibration((byte) 2);

    setupMagnetometerOffsetResponse();
  }

  private void setupMagnetometerOffsetResponse() throws FailureReason {
    doReturn(new Vector(-2009.2892f, 1127.3577f, -1495.306f))
        .when(bleDriver)
        .loadMagnetometerOffsets();
  }

  static class StubKolibreeBleDriver extends KolibreeBleDriver {
    static final int CALIBRATION_DATA_SIZE = 12;

    boolean supportsBrushingEventsPolling = true;

    StubKolibreeBleDriver(
        KLNordicBleManager bleManager,
        @NonNull KLTBDriverListener listener,
        @NonNull Scheduler bluetoothScheduler,
        @NonNull String mac,
        CharacteristicNotificationStreamer notificationCaster,
        @NonNull Scheduler notifyListenerScheduler) {
      super(
          bleManager,
          listener,
          bluetoothScheduler,
          mac,
          notificationCaster,
          notifyListenerScheduler);
    }

    @Override
    boolean supportsReadingBootloader() {
      return false;
    }

    @Override
    protected int calibrationDataSize() {
      return CALIBRATION_DATA_SIZE;
    }

    @Override
    public boolean supportsBrushingEventsPolling() {
      return supportsBrushingEventsPolling;
    }

    @Override
    ToothbrushModel toothbrushModel() {
      // random. Not true
      return ToothbrushModel.CONNECT_B1;
    }

    @NotNull
    @Override
    public Flowable<OverpressureState> overpressureStateFlowable() {
      return Flowable.never();
    }
  }

  private OfflineBrushingsExtractor mockOfflineBrushingExtractor() {
    OfflineBrushingsExtractor offlineBrushingsExtractor = mock(OfflineBrushingsExtractor.class);
    bleDriver.offlineBrushingsExtractor = offlineBrushingsExtractor;
    return offlineBrushingsExtractor;
  }

  private void prepareOnFirstConnectionEstablished(boolean isRunningBootloader) throws Exception {
    doNothing().when(bleDriver).loadVersions();
    doNothing().when(bleDriver).maybeLoadGruDataInfo();
    doNothing().when(bleDriver).loadSensorCalibration();
    doNothing().when(bleDriver).injectOfflineBrushingExtractor();
    bleDriver.runningBootloader.set(isRunningBootloader);
  }
}
