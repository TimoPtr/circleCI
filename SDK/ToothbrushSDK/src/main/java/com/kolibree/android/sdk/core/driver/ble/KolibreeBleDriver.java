package com.kolibree.android.sdk.core.driver.ble;

import static com.kolibree.android.TimberTagKt.bluetoothTagFor;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_AUTO_SHUTDOWN_TIMEOUT;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BOOTLOADER_VERSION;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_EVENTS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_CURRENT_TIME;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_DEFAULT_BRUSHING_DURATION;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_GRU_DATA_SET_INFO;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MAGNETOMETER_CALIBRATION;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_OWNER_DEVICE;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_SENSOR_SENSITIVITIES;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_SERIAL_NUMBER;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_VIBRATION_SIGNALS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_STATUS_NOTIFICATION;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSORS_DETECTIONS;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSOR_RAW_DATA;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.util.Collections.unmodifiableList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.KolibreeAndroidSdk;
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState;
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState;
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState;
import com.kolibree.android.sdk.connection.detectors.data.WeightedMouthZone;
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParser;
import com.kolibree.android.sdk.core.binary.Bitmask;
import com.kolibree.android.sdk.core.binary.PayloadReader;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.KLTBDriver;
import com.kolibree.android.sdk.core.driver.KLTBDriverListener;
import com.kolibree.android.sdk.core.driver.VibratorMode;
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic;
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils;
import com.kolibree.android.sdk.core.driver.ble.nordic.KLManagerCallbacks;
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushingsExtractor;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType;
import com.kolibree.android.sdk.core.driver.ble.raw.RawDataFactory;
import com.kolibree.android.sdk.error.CommandNotSupportedException;
import com.kolibree.android.sdk.error.ConnectionEstablishException;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.math.Axis;
import com.kolibree.android.sdk.math.Matrix;
import com.kolibree.android.sdk.math.Vector;
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState;
import com.kolibree.android.sdk.util.MouthZoneIndexMapper;
import com.kolibree.android.sdk.version.DspVersion;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import com.kolibree.kml.MouthZone16;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.OffsetDateTime;
import timber.log.Timber;

/**
 * Created by aurelien on 15/11/16.
 *
 * <p>Ara toothbrush driver
 *
 * <p>Contains all bluetooth related code
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
abstract class KolibreeBleDriver
    implements KLTBDriver, RawDataFactory.RawDataFactoryCallback, KLManagerCallbacks, BleDriver {

  @VisibleForTesting
  interface DeviceVersionConstants {
    int LENGTH_OF_BOOTLOADER_FRAME = 17;
    int LENGTH_OF_MAIN_APP_FRAME = 16;
    int BYTE_11_APP_IN_BOOTLOADER = 0x55;
    int BYTE_11_APP_NOT_IN_BOOTLOADER = 0x00;
    int BYTE_16_APP_IN_BOOTLOADER = 0x01;
    int BYTE_16_APP_NOT_IN_BOOTLOADER = 0x00;
    List<Integer> VALID_BYTE_11_VALUES =
        unmodifiableList(Arrays.asList(BYTE_11_APP_IN_BOOTLOADER, BYTE_11_APP_NOT_IN_BOOTLOADER));
    List<Integer> VALID_BYTE_16_VALUES =
        unmodifiableList(Arrays.asList(BYTE_16_APP_IN_BOOTLOADER, BYTE_16_APP_NOT_IN_BOOTLOADER));
  }

  private static final String TAG = bluetoothTagFor(KolibreeBleDriver.class);

  private static final int RECONNECT_ATTEMPTS = 3;
  private static final int RECONNECT_DELAY_MILLIS = 600;

  @VisibleForTesting static final int LOAD_SENSOR_CALIBRATION_MAX_RETRIES = 3;

  private final KLTBDriverListener listener;
  final KLNordicBleManager bleManager;

  @VisibleForTesting @Inject OfflineBrushingsExtractor offlineBrushingsExtractor;

  private RawDataFactory rawDataFactory;

  @NonNull private HardwareVersion hardwareVersion = HardwareVersion.NULL;

  @NonNull private SoftwareVersion firmwareVersion = SoftwareVersion.NULL;

  @NonNull private SoftwareVersion gruDataVersion = SoftwareVersion.NULL;

  @NonNull @VisibleForTesting SoftwareVersion bootloaderVersion = SoftwareVersion.NULL;

  @NonNull @VisibleForTesting DspVersion dspVersion = DspVersion.NULL;

  @VisibleForTesting final AtomicBoolean runningBootloader = new AtomicBoolean(false);

  private boolean hasValidGruData;

  @VisibleForTesting float[] calibrationData;

  private final String mac;

  @VisibleForTesting Completable connectCompletable;

  @VisibleForTesting
  WeakReference<CompletableEmitter> weakConnectEmitter = new WeakReference<>(null);

  /**
   * Scheduler on which to execute operations pointing to BleManager
   *
   * <p>BleManager has its own queuing mechanisms, this should be used when we return an Observable
   */
  private final Scheduler btScheduler;

  private final Scheduler notifyListenerScheduler;

  private final CharacteristicNotificationStreamer notificationCaster;

  protected final CharacteristicNotificationStreamer bleNotificationMulticaster() {
    return notificationCaster;
  }

  @VisibleForTesting boolean isFirstConnection = true;

  @VisibleForTesting final CompositeDisposable disposables = new CompositeDisposable();

  @VisibleForTesting(otherwise = PROTECTED)
  void clearCache() {
    hasValidGruData = false;
  }

  KolibreeBleDriver(Context context, @NonNull String mac, @NonNull KLTBDriverListener listener) {
    this(
        new KLNordicBleManager(context),
        listener,
        Schedulers.from(Executors.newSingleThreadExecutor()),
        mac,
        new CharacteristicNotificationStreamer(),
        Schedulers.from(Executors.newSingleThreadExecutor()));
  }

  @VisibleForTesting
  KolibreeBleDriver(
      KLNordicBleManager bleManager,
      @NonNull KLTBDriverListener listener,
      @NonNull Scheduler bluetoothScheduler,
      @NonNull String mac,
      CharacteristicNotificationStreamer characteristicNotificationStreamer,
      @NonNull Scheduler notifyListenerScheduler) {
    this.listener = listener;

    this.bleManager = bleManager;
    this.btScheduler = bluetoothScheduler;
    this.mac = mac;
    this.notifyListenerScheduler = notifyListenerScheduler;
    notificationCaster = characteristicNotificationStreamer;
    /*
    setGattCallbacks moved to connect because it's not safe to pass a self reference inside the
    constructor
     */
  }

  @MainThread
  @VisibleForTesting
  Completable connectCompletable(@NonNull BluetoothDevice bluetoothDevice) {
    Timber.tag(TAG).d("connectCompletable");
    if (bleManager.getBluetoothDevice() != null
        && bleManager.getBluetoothDevice().equals(bluetoothDevice)) {
      Timber.tag(TAG)
          .w(
              "BleManager's device is equal to requested connect. Is ready? %s",
              bleManager.isReady());
      if (bleManager.isReady()) {
        if (isFirstConnection) {
          Timber.tag(TAG).d("connectCompletable onDeviceReadyCompletable");
          return onDeviceReadyCompletable().andThen(notifyConnectionEstablishedCompletable());
        } else {
          Timber.tag(TAG).d("connectCompletable notifyConnectionEstablishedCompletable");
          return notifyConnectionEstablishedCompletable();
        }
      }
    }

    return innerConnectCompletable(bluetoothDevice);
  }

  @MainThread
  @VisibleForTesting
  Completable innerConnectCompletable(@NonNull BluetoothDevice bluetoothDevice) {
    Timber.tag(TAG).d("innerConnectCompletable is %s, driver is %s", connectCompletable, this);

    if (connectCompletable == null) {
      synchronized (this) {
        if (connectCompletable == null) {
          connectCompletable =
              Completable.create(
                      emitter -> {
                        bleManager.setGattCallbacks(this);

                        synchronized (this) {
                          weakConnectEmitter = new WeakReference<>(emitter);
                        }

                        internalConnect(bluetoothDevice, emitter);
                      })
                  .doOnSubscribe(ignore -> Timber.tag(TAG).d("ConnectCompletable onsubscribe"))
                  .doFinally(() -> Timber.tag(TAG).d("ConnectCompletable doFinally"))
                  .doOnComplete(() -> Timber.tag(TAG).d("ConnectCompletable doOnComplete"))
                  .doOnError(
                      throwable ->
                          Timber.tag(TAG).e("ConnectCompletable error %s", throwable.getMessage()))
                  .doOnTerminate(this::clearWeakConnectionEmitter)
                  .doOnTerminate(
                      () -> {
                        synchronized (this) {
                          connectCompletable = null;
                        }
                      })
                  .cache();
        }
      }
    }

    return connectCompletable;
  }

  @VisibleForTesting
  void internalConnect(@NonNull BluetoothDevice bluetoothDevice, CompletableEmitter emitter) {
    Timber.tag(TAG).d("Running connect");
    bleManager
        .connect(bluetoothDevice)
        .useAutoConnect(true)
        .retry(RECONNECT_ATTEMPTS, RECONNECT_DELAY_MILLIS)
        .done(
            ignore -> {
              Timber.tag(TAG).d("Connect succeeded");
              if (!emitter.isDisposed()) {
                emitter.onComplete();
              }
            })
        .fail(
            (device, status) -> {
              Timber.tag(TAG).d("Connect failed");
              emitter.tryOnError(
                  new ConnectionEstablishException(
                      format(
                          "Connect fail to device %s, status is %s", device.getAddress(), status)));
            })
        .invalid(
            () -> {
              Timber.tag(TAG).d("Connect invalid");
              emitter.tryOnError(
                  new ConnectionEstablishException(
                      format(
                          "Invalid state while connecting to %s", bluetoothDevice.getAddress())));
            })
        .enqueue();
  }

  @Override
  public final void connect() {
    connectCompletable(getBluetoothDevice(mac))
        .subscribeOn(AndroidSchedulers.mainThread())
        .blockingAwait();
  }

  @Override
  public final void connectDfuBootloader() {
    connectCompletable(getBluetoothDevice(DfuUtils.getDFUMac(mac)))
        .subscribeOn(AndroidSchedulers.mainThread())
        .blockingAwait();
  }

  @VisibleForTesting
  BluetoothDevice getBluetoothDevice(@NonNull String mac) {
    return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
  }

  /*
  Executed on btScheduler
   */
  @VisibleForTesting(otherwise = PROTECTED)
  void onFirstConnectionEstablished() throws FailureReason {
    try {
      Timber.tag(TAG).d("onFirstConnectionEstablished isFirst %s", isFirstConnection);
      loadVersions();

      if (isFirstConnection && !isRunningBootloader()) {
        maybeLoadGruDataInfo();

        loadSensorCalibration();

        injectOfflineBrushingExtractor();
      }
    } catch (Throwable failureReason) {
      throw new FailureReason(failureReason);
    }
  }

  @VisibleForTesting
  void injectOfflineBrushingExtractor() {
    DaggerExtractOfflineBrushingsComponent.factory()
        .create(
            KolibreeAndroidSdk.getSdkComponent().applicationContext(),
            bleManager,
            fileType(),
            toothbrushModel())
        .inject(this);
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  @NonNull
  FileType fileType() {
    return FileType.BRUSHING;
  }

  @Override
  public void onSensorState(@NonNull RawSensorState state) {
    runOnListenerScheduler(() -> listener.onSensorRawData(state));
  }

  /** Asynchronous call, will invoke KLTBDriverListener.onDisconnected on success */
  @Override
  public void disconnect() {
    clearCache();

    Timber.tag(TAG).d("KolibreeBleDriver disconnecting");
    bleManager.disconnectWithoutReconnect(this::onManualDisconnect, this::onManualDisconnect);
  }

  private Unit onManualDisconnect() {
    // we are no longer interested in receiving callbacks. Null is not accepted as a parameter
    bleManager.setGattCallbacks(NoOpGattCallback.INSTANCE);

    onDeviceDisconnected();

    return Unit.INSTANCE;
  }

  @Override
  public Completable reconnect() {
    synchronized (this) {
      if (isConnectionInProgress()) {
        Timber.tag(TAG)
            .w(
                "Reconnect invoked but Connection in progress, returning connectCompletable %s",
                connectCompletable);
        return connectCompletable;
      } else {
        Timber.tag(TAG)
            .w(
                "Reconnect invoked. Emitter is %s and completable %s",
                weakConnectEmitter.get(), connectCompletable);
      }
    }

    return internalReconnect(getBluetoothDevice(mac));
  }

  @VisibleForTesting
  boolean isConnectionInProgress() {
    return weakConnectEmitter.get() != null && connectCompletable != null;
  }

  @VisibleForTesting
  Completable internalReconnect(BluetoothDevice device) {
    CompletableSubject disconnectCompletable = CompletableSubject.create();

    Function0<Unit> commonCallback =
        () -> {
          Timber.tag(TAG).d("disconnectWithoutReconnect callback");
          clearCache();

          disconnectCompletable.onComplete();

          return Unit.INSTANCE;
        };

    bleManager.disconnectWithoutReconnect(commonCallback, commonCallback);

    return disconnectCompletable
        .doOnComplete(() -> Timber.tag(TAG).d("reconnect disconnectCompletable completed"))
        .andThen(
            Completable.defer(
                () ->
                    innerConnectCompletable(device)
                        .doOnComplete(
                            () ->
                                Timber.tag(TAG).d("reconnect innerConnectCompletable completed"))))
        .doOnComplete(() -> Timber.tag(TAG).d("reconnect completed"));
  }

  @Override
  public void setTime() throws FailureReason {
    try {
      OffsetDateTime datetime = TrustedClock.getNowOffsetDateTime();
      Timber.tag(TAG).d("setTime(date = %s)", datetime);
      setDeviceParameter(
          new PayloadWriter(7)
              .writeByte(DEVICE_PARAMETERS_CURRENT_TIME)
              .writeDate(datetime)
              .getBytes());
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @NonNull
  @Override
  public OffsetDateTime getTime() throws FailureReason {
    try {
      return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_CURRENT_TIME}).skip(1).readDate();
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public void setAutoReconnectTimeout(int timeout) {
    // TODO
  }

  @Override
  public void setDefaultBrushingDuration(int time) throws FailureReason {
    try {
      setDeviceParameter(
          new PayloadWriter(3)
              .writeByte(DEVICE_PARAMETERS_DEFAULT_BRUSHING_DURATION)
              .writeUnsignedInt16(time)
              .getBytes());
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public int getDefaultBrushingDuration() throws FailureReason {
    try {
      return (int)
          getDeviceParameter(new byte[] {DEVICE_PARAMETERS_DEFAULT_BRUSHING_DURATION})
              .skip(1)
              .readInt16();
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public void setOwnerDevice(long ownerDevice) throws FailureReason {
    try {
      setDeviceParameter(
          new PayloadWriter(5)
              .writeByte(DEVICE_PARAMETERS_OWNER_DEVICE)
              .writeInt32((int) ownerDevice)
              .getBytes());
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public long getOwnerDevice() throws FailureReason {
    try {
      return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_OWNER_DEVICE})
          .skip(1)
          .readUnsignedInt32();
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public void setAutoShutdownTimeout(int autoShutdownTimeout) throws FailureReason {
    if (autoShutdownTimeout < 30 || autoShutdownTimeout > 3600) {
      throw new FailureReason("Invalid auto shutdown timeout : " + autoShutdownTimeout);
    }

    try {
      setDeviceParameter(
          new PayloadWriter(3)
              .writeByte(DEVICE_PARAMETERS_AUTO_SHUTDOWN_TIMEOUT)
              .writeUnsignedInt16(autoShutdownTimeout)
              .getBytes());
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public int getAutoShutdownTimeout() throws FailureReason {
    try {
      return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_AUTO_SHUTDOWN_TIMEOUT})
          .skip(1)
          .readUnsignedInt16();
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public Completable monitorCurrentBrushing() {
    return Completable.create(
            emitter -> {
              try {
                setDeviceParameter(new byte[] {DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING});

                if (!emitter.isDisposed()) {
                  emitter.onComplete();
                }
              } catch (Exception e) {
                emitter.tryOnError(new FailureReason(e));
              }
            })
        .subscribeOn(btScheduler);
  }

  @Override
  public Completable startExtractFileSession() {
    return offlineBrushingsExtractor.startExtractFileSession();
  }

  @Override
  public Completable finishExtractFileSession() {
    return offlineBrushingsExtractor.finishExtractFileSession();
  }

  @Override
  public OfflineBrushing getNextRecord() {
    return offlineBrushingsExtractor.popRecord().subscribeOn(btScheduler).blockingGet();
  }

  @Override
  public Completable deleteNextRecord() {
    return offlineBrushingsExtractor.deleteRecord().subscribeOn(btScheduler);
  }

  @Override
  public Single<Integer> getRemainingRecordCount() {
    return offlineBrushingsExtractor.recordCount().subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public Completable setVibratorMode(@NonNull VibratorMode vibratorMode) {
    return Completable.create(
            emitter -> {
              try {
                setDeviceParameter(CommandSet.setVibrationPayload(vibratorMode));

                if (!emitter.isDisposed()) emitter.onComplete();
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public String getSerialNumber() throws FailureReason {
    try {
      final PayloadReader reader = getDeviceParameter(new byte[] {DEVICE_PARAMETERS_SERIAL_NUMBER});
      String serialNumber = reader.length > 1 ? reader.skip(1).readString(reader.length - 1) : "";
      Timber.tag(TAG).d("getSerialNumber() = %s", serialNumber);
      return serialNumber;
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public void setVibrationLevel(int percents) throws FailureReason {
    try {
      setDeviceParameter(
          new byte[] {
            DEVICE_PARAMETERS_VIBRATION_SIGNALS,
            0x00, // Brushing signal
            (byte) percents, // Intensity
            (byte) 0xFF, // Fixed pattern (see fw doc to add more)
            0x00,
            0x00,
            0x00,
            0x00 // Unused
          });
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @Override
  public boolean hasValidGruData() {
    return hasValidGruData;
  }

  @Override
  @NonNull
  public SoftwareVersion getGruDataVersion() {
    return gruDataVersion;
  }

  @Override
  @NonNull
  public SoftwareVersion getFirmwareVersion() {
    return firmwareVersion;
  }

  @Override
  @NonNull
  public SoftwareVersion getBootloaderVersion() {
    return bootloaderVersion;
  }

  @NotNull
  @Override
  public DspVersion getDspVersion() {
    return dspVersion;
  }

  @Override
  @NonNull
  public HardwareVersion getHardwareVersion() {
    return hardwareVersion;
  }

  /**
   * Load toothbrush's hardware, firmware and bootloader versions.
   *
   * <p>maragues - 6-oct-2017
   *
   * <p>Changed to protected because for now ConnectM1Driver can't read version if it's in
   * bootloader mode
   *
   * @throws FailureReason if the command could not be sent
   */
  @VisibleForTesting
  void loadVersions() throws FailureReason {
    Timber.tag(TAG).d("Load versions");
    final PayloadReader versions = bleManager.getDeviceVersions();

    validateVersionsPayload(versions);
    processVersionsPayload(versions);

    readBootloaderVersion();
    readDspVersion();

    Timber.tag(TAG)
        .d(
            "Versions: Firmware: %s; HW: %s; Bootloader: %s; DSP: %s; Is running BL: %s",
            firmwareVersion.toString(),
            hardwareVersion.toString(),
            bootloaderVersion.toString(),
            dspVersion.toString(),
            isRunningBootloader());
  }

  @VisibleForTesting
  void readDspVersion() throws FailureReason {
    if (toothbrushModel().getHasDsp()) {
      PayloadReader response =
          bleManager.getDeviceParameter(new byte[] {DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS});

      dspVersion =
          DspStatePayloadParser.create(firmwareVersion)
              .parseDspStatePayload(response)
              .getFirmwareVersion();
    }
  }

  @VisibleForTesting
  void readBootloaderVersion() throws FailureReason {
    if (supportsReadingBootloader()) {
      PayloadReader response =
          bleManager.getDeviceParameter(new byte[] {DEVICE_PARAMETERS_BOOTLOADER_VERSION});

      bootloaderVersion = response.skip(1).readSoftwareVersion();
    }
  }

  @CallSuper
  boolean supportsReadingBootloader() {
    return !isRunningBootloader();
  }

  @VisibleForTesting(otherwise = PROTECTED)
  @CallSuper
  void processVersionsPayload(PayloadReader versions) {
    firmwareVersion = versions.readSoftwareVersion();
    hardwareVersion = versions.readHardwareVersion();

    versions.skip(3);

    byte bootloaderByte = (byte) versions.readUnsignedInt8();
    setRunningBootloader(bootloaderByte == DeviceVersionConstants.BYTE_11_APP_IN_BOOTLOADER);

    if (isRunningBootloader()) {
      bootloaderVersion = firmwareVersion;
      firmwareVersion = SoftwareVersion.NULL;
    }
  }

  @VisibleForTesting
  void validateVersionsPayload(PayloadReader versions) throws FailureReason {
    Timber.tag(TAG).d("Validating versions response");

    versions.rewind();

    if (versions.length == DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME) {
      // response from bootloader
      versions.skip(11);
      int byte11 = versions.readUnsignedInt8();
      versions.skip(4);
      int byte16 = versions.readUnsignedInt8();

      Timber.tag(TAG).v("Versions Byte 11 = 0x%x", byte11);
      Timber.tag(TAG).v("Versions Byte 16 = 0x%x", byte16);

      if (!DeviceVersionConstants.VALID_BYTE_11_VALUES.contains(byte11)
          || !DeviceVersionConstants.VALID_BYTE_16_VALUES.contains(byte16)) {
        throw new FailureReason(
            format(
                "DeviceVersions payload (%s) has incorrect value for bootloader",
                versions.toString()));
      }
    } else if (versions.length == DeviceVersionConstants.LENGTH_OF_MAIN_APP_FRAME) {
      versions.skip(11);
      int byte11 = versions.readUnsignedInt8();
      Timber.tag(TAG).v("Versions Byte 11 = 0x%x", byte11);
      if (!DeviceVersionConstants.VALID_BYTE_11_VALUES.contains(byte11)) {
        throw new FailureReason(
            format(
                "DeviceVersions payload (%s) has incorrect value for bootloader",
                versions.toString()));
      }
    } else {
      throw new FailureReason(
          format(
              "DeviceVersions payload (%s) has incorrect length (neither %d nor %d)",
              versions.toString(),
              DeviceVersionConstants.LENGTH_OF_BOOTLOADER_FRAME,
              DeviceVersionConstants.LENGTH_OF_MAIN_APP_FRAME));
    }

    Timber.tag(TAG).d("Versions response is valid");

    versions.rewind();
  }

  @NonNull
  @Override
  public Completable reloadVersions() {
    return Completable.create(
            emitter -> {
              try {
                Timber.tag(TAG).i("Reloading versions");
                loadVersions();

                emitter.onComplete();
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(btScheduler);
  }

  /**
   * Load toothbrush's GRU data version and state.
   *
   * @throws Exception if the command could not be sent
   */
  void maybeLoadGruDataInfo() throws Exception {
    if (supportsGRUData()) {
      final PayloadReader gru =
          getDeviceParameter(new byte[] {DEVICE_PARAMETERS_GRU_DATA_SET_INFO});
      hasValidGruData = gru.skip(1).readBoolean();
      gruDataVersion = gru.readSoftwareVersion();
    }
  }

  @MainThread
  @Override
  public void onSensorControl(boolean svmOn, boolean rnnOn, boolean rawDataOn, boolean handedness) {
    final Bitmask streamingBitmask = new Bitmask();
    final Bitmask detectionBitmask = new Bitmask();

    streamingBitmask
        .set(0, rawDataOn)
        .set(1, false) // gravity always false
        .set(2, svmOn || rnnOn);

    detectionBitmask
        .set(1, false) // always disable OSM
        .set(2, rnnOn || svmOn); // Ara uses RNN as SVM

    // Send the control payload to the toothbrush
    bleManager.writeSensorStreamingControl(
        getSensorControlPayload(streamingBitmask, detectionBitmask, handedness));
  }

  protected byte[] getSensorControlPayload(
      Bitmask streamingBitmask, Bitmask detectionBitmask, boolean handedness) {
    return new byte[] {
      streamingBitmask.get(),
      detectionBitmask.get(),
      50, // Default value
      (byte) (handedness ? 0x01 : 0x00)
    };
  }

  @Override
  public void enableRawDataNotifications() {
    runActionOnBtScheduler(bleManager::enableRawDataNotifications);
  }

  @Override
  public void disableRawDataNotifications() {
    runActionOnBtScheduler(bleManager::disableRawDataNotifications);
  }

  @Override
  public void enableDetectionNotifications() {
    runActionOnBtScheduler(bleManager::enableDetectionNotifications);
  }

  @Override
  public void disableDetectionNotifications() {
    runActionOnBtScheduler(bleManager::disableDetectionNotifications);
  }

  protected void loadSensorCalibration() throws Exception {
    calibrationData = new float[calibrationDataSize()];

    // Load magnetometer rotation matrix
    final Matrix rotationMatrix = new Matrix();

    int errorCounter = 0;
    for (byte b = 0; b < 3; b++) {
      try {
        final PayloadReader reader = loadMagnetometerCalibration(b);

        for (int i = 0; i < 3; i++) {
          final float f = reader.readFloat();
          rotationMatrix.set(i, b, f);
          calibrationData[b * 3 + i] = f;
        }
      } catch (FailureReason failureReason) {
        Timber.tag(TAG).e(failureReason, "loadSensorCalibration");

        if (errorCounter >= LOAD_SENSOR_CALIBRATION_MAX_RETRIES) {
          throw failureReason;
        } else {
          b--;
        }

        errorCounter++;
      }
    }

    // Load magnetometer offset vector
    final Vector magnetometerOffsets = loadMagnetometerOffsets();

    calibrationData[9] = magnetometerOffsets.get(Axis.X);
    calibrationData[10] = magnetometerOffsets.get(Axis.Y);
    calibrationData[11] = magnetometerOffsets.get(Axis.Z);

    setMagnetometerCalibration(rotationMatrix, magnetometerOffsets);
  }

  @VisibleForTesting
  PayloadReader loadMagnetometerCalibration(byte payload) throws FailureReason {
    return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_MAGNETOMETER_CALIBRATION, payload})
        .skip(2);
  }

  @VisibleForTesting
  Vector loadMagnetometerOffsets() throws FailureReason {
    return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_MAGNETOMETER_CALIBRATION, 3})
        .skip(2)
        .readVector();
  }

  @VisibleForTesting
  void setMagnetometerCalibration(Matrix rotationMatrix, Vector magnetometerOffsets)
      throws FailureReason {
    // Raw data parser
    rawDataFactory = createRawDataFactory();

    // Load sensors sensitivities
    final PayloadReader sensitivities = loadSensorSensitivities();
    rawDataFactory.setSensitivities(
        sensitivities.skip(1).readFloat(), sensitivities.readFloat(), sensitivities.readFloat());

    rawDataFactory.setMagnetometerCalibration(rotationMatrix, magnetometerOffsets);
  }

  private PayloadReader loadSensorSensitivities() throws FailureReason {
    return getDeviceParameter(new byte[] {DEVICE_PARAMETERS_SENSOR_SENSITIVITIES});
  }

  @NonNull
  @VisibleForTesting
  private RawDataFactory createRawDataFactory() {
    return new RawDataFactory(this);
  }

  RawDataFactory rawDataFactory() {
    return rawDataFactory;
  }

  @NonNull
  @Override
  public float[] getSensorCalibration() {
    if (calibrationData == null) {
      return new float[calibrationDataSize()];
    }

    return calibrationData;
  }

  @Override
  public void onNotify(@NonNull UUID uuid, @Nullable byte[] value) {
    if (value == null) {
      return;
    }

    if (DEVICE_PARAMETERS.UUID.equals(uuid)) {
      processDeviceParameterNotification(value);
    } else if (SENSORS_DETECTIONS.UUID.equals(uuid)) {
      processSensorDetectionNotification(value);
    } else if (SENSOR_RAW_DATA.UUID.equals(uuid)) {
      processRawDataNotification(value);
    }

    notificationCaster.onNewData(new BleNotificationData(uuid, value));
  }

  @NotNull
  @Override
  public Flowable<byte[]> deviceParametersCharacteristicChangedStream() {
    return notificationCaster.characteristicStream(DEVICE_PARAMETERS);
  }

  @VisibleForTesting
  void processRawDataNotification(@NonNull byte[] value) {
    if (rawDataFactory != null) {
      rawDataFactory.onRawDataPacket(value);
    }
  }

  @VisibleForTesting
  void processSensorDetectionNotification(@NonNull byte[] value) {
    final PayloadReader sensors = new PayloadReader(value);

    if (sensors.readInt8() == 0x04) { // RNN (6 zones)
      final List<WeightedMouthZone> rnnData = new ArrayList<>(6);
      final List<MouthZone16> svmData = new ArrayList<>(6);

      for (int i = 0; i < 6; i++) {
        final MouthZone16 zone = MouthZoneIndexMapper.mapZoneIdToMouthZone16(sensors.readInt8());

        rnnData.add(new WeightedMouthZone(zone, sensors.readUnsignedInt16()));
        svmData.add(zone);
      }

      runOnListenerScheduler(
          () -> {
            listener.onRNNDetection(rnnData);

            // Notify SVM too, with zones only
            listener.onSVMDetection(svmData);
          });
    }
  }

  @VisibleForTesting
  void processDeviceParameterNotification(@NonNull byte[] value) {
    final PayloadReader payload = new PayloadReader(value);
    byte commandId = payload.readInt8();

    // Byte 0 is command ID
    if (commandId == DEVICE_PARAMETERS_BRUSHING_EVENTS) {
      boolean isSessionStarted = payload.readBoolean();
      boolean isVibrating = payload.readBoolean();
      runOnListenerScheduler(() -> listener.onBrushingSessionStateChanged(isSessionStarted));
      runOnListenerScheduler(() -> listener.onVibratorStateChanged(isVibrating));
    } else {
      onDeviceParameterNotification(commandId, payload);
    }
  }

  @CallSuper // Make sure we don't break a feature on a 2 levels inheritance
  protected void onDeviceParameterNotification(
      byte commandId, @NotNull PayloadReader payloadReader) {
    // no-op should be overridden by specific drivers to consume unparsed deviceParams
  }

  @Override
  public boolean isRunningBootloader() {
    Timber.tag(TAG).i("read isRunningBootloader: %s", runningBootloader.get());
    return runningBootloader.get();
  }

  @Override
  public void setRunningBootloader(boolean runningBootloader) {
    Timber.tag(TAG).i("setRunningBootloader to %s", runningBootloader);

    this.runningBootloader.set(runningBootloader);
  }

  @Override
  public void enableOtaUpdateStatusCharacteristicNotifications() {
    // blocking call, we want to schedule it on btScheduler
    runActionOnBtScheduler(bleManager::enableNotificationsForOtaUpdateStatus);
  }

  @NonNull
  @Override
  public Flowable<byte[]> otaUpdateStatusCharacteristicChangedFlowable() {
    return notificationCaster.characteristicStream(
        OTA_UPDATE_STATUS_NOTIFICATION,
        () -> {
          // blocking call, we want to schedule it on btScheduler
          runActionOnBtScheduler(bleManager::enableNotificationsForOtaUpdateStatus);

          return Unit.INSTANCE;
        });
  }

  @Override
  @NonNull
  public Flowable<PlaqlessRawSensorState> plaqlessRawDataNotifications() {
    return Flowable.error(
        new CommandNotSupportedException("PlaqlessRawData characteristic not supported"));
  }

  @NonNull
  @Override
  public Flowable<PlaqlessSensorState> plaqlessNotifications() {
    return Flowable.error(
        new CommandNotSupportedException("Plaqless characteristic not supported"));
  }

  @NotNull
  @Override
  public Flowable<PlaqlessRingLedState> plaqlessRingLedState() {
    return Flowable.error(new CommandNotSupportedException("PlaqlessRingLedState not supported"));
  }

  @NonNull
  @Override
  public Completable writeOtaUpdateStartCharacteristic(@NonNull byte[] payload) {
    return bleManager.writeOtaUpdateStartCharacteristic(payload).subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public Completable writeOtaChunkCharacteristicWithResponse(@NonNull byte[] payload) {
    return bleManager.writeOtaChunkCharacteristicWithResponse(payload).subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public Completable writeOtaChunkCharacteristic(@NonNull byte[] payload) {
    return bleManager.writeOtaChunkCharacteristic(payload).subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public Completable writeOtaUpdateValidateCharacteristic(@NonNull byte[] payload) {
    return bleManager.writeOtaUpdateValidateCharacteristic(payload).subscribeOn(btScheduler);
  }

  @Override
  public void cancelPendingOperations() {
    bleManager.cancelPendingOperations();
  }

  @NonNull
  @Override
  public Single<Integer> connectionInterval() {
    return Single.<Integer>create(
            emitter -> {
              try {
                emitter.onSuccess(bleManager.readConnectionInterval());
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(btScheduler);
  }

  @Override
  public void sendCommand(@NonNull byte[] commandPayload) throws FailureReason {
    // blocking call, we want to schedule it on btScheduler
    runActionOnBtScheduler(() -> bleManager.sendCommand(commandPayload));
  }

  @Override
  public boolean setDeviceParameter(@NonNull byte[] payload) throws FailureReason {
    // Device Parameter characteristic is not present in bootloader bode
    if (isRunningBootloader()) {
      return false;
    }

    // non-blocking call, no need to run it on btScheduler
    return bleManager.setDeviceParameter(payload);
  }

  @NonNull
  @Override
  public PayloadReader getDeviceParameter(@NonNull byte[] payload) throws FailureReason {
    // blocking call, we want to schedule it on btScheduler
    return runCallableOnBtScheduler(() -> bleManager.getDeviceParameter(payload));
  }

  @NonNull
  @Override
  public PayloadReader setAndGetDeviceParameter(@NonNull byte[] payload) throws FailureReason {
    // blocking call, we want to schedule it on btScheduler
    return runCallableOnBtScheduler(() -> bleManager.setAndGetDeviceParameter(payload));
  }

  @NonNull
  @Override
  public Single<PayloadReader> setAndGetDeviceParameterOnce(@NonNull byte[] payload) {
    return Single.<PayloadReader>create(
            emitter -> {
              try {
                emitter.onSuccess(bleManager.setAndGetDeviceParameter(payload));
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(btScheduler);
  }

  @NonNull
  @Override
  public Completable refreshDeviceCacheCompletable() {
    return bleManager.refreshDeviceCacheCompletable();
  }

  /*
  KLManagerCallbacks
   */

  @Override
  public void onDeviceConnecting(@NonNull BluetoothDevice device) {
    Timber.tag(TAG).i("%s started connecting to the TB", device.getAddress());
    runOnListenerScheduler(listener::onConnectionEstablishing);
  }

  @Override
  public void onDeviceConnected(@NonNull BluetoothDevice device) {
    // As the service discovery is happening just after that, we don't start the communication.
    Timber.tag(TAG).i("onDeviceConnected: %s has been connected", device.getAddress());
    runOnListenerScheduler(listener::onConnectionEstablishing);
  }

  @Override
  public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
    Timber.tag(TAG)
        .e(
            "onDeviceDisconnecting: the user initialized a disconnection for %s",
            device.getAddress());
  }

  @Override
  public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
    Timber.tag(TAG).e("onDeviceDisconnected: %s disconnected", device.getAddress());
    onDeviceDisconnected();
  }

  @VisibleForTesting
  void onDeviceDisconnected() {
    if (!isConnecting()) {
      // Reset the isFirstConnection flag to his default value
      isFirstConnection = true;

      disposables.clear();

      runOnListenerScheduler(listener::onDisconnected);
    } else {
      Timber.tag(TAG).w("onDeviceDisconnected while connecting");
    }
  }

  private boolean isConnecting() {
    return connectCompletable != null;
  }

  @Override
  public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
    if (!isConnecting()) {
      Timber.tag(TAG)
          .e("onLinkLossOccurred: Ble Manager lost connection with %s.", device.getAddress());
      // As the auto connect flag is true, we will try to reconnect automatically.
      isFirstConnection = false;

      disposables.clear();

      runOnListenerScheduler(listener::onDisconnected);
    } else {
      Timber.tag(TAG).w("onLinkLossOccurred while connecting");
    }
  }

  @Override
  public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
    Timber.tag(TAG)
        .d(
            "onServicesDiscovered: service discovery has finished for %s (optionalServicesFound = %b)",
            device.getAddress(), optionalServicesFound);
  }

  @Override
  public void onDeviceReady(@NonNull BluetoothDevice device) {
    Timber.tag(TAG)
        .d(
            "onDeviceReady: all initialization requests has been completed for %s",
            device.getAddress());

    DisposableUtils.addSafely(
        disposables,
        onDeviceReadyCompletable()
            .andThen(notifyConnectionEstablishedCompletable())
            .doOnError(throwable -> onDeviceReadyError(device, throwable))
            .subscribe(() -> {}, throwable -> Timber.tag(TAG).e(throwable, "onDeviceReady: ")));
  }

  @VisibleForTesting
  void onDeviceReadyError(BluetoothDevice device, Throwable throwable) {
    boolean emitError = false;
    CompletableEmitter connectEmitter;
    synchronized (this) {
      connectEmitter = weakConnectEmitter.get();
      if (connectEmitter != null && !connectEmitter.isDisposed()) {
        emitError = true;

        clearWeakConnectionEmitter();
      }
    }

    if (emitError) {
      connectEmitter.tryOnError(throwable);
    }
    // TODO we may try to attempt reconnection here but we need to handle RX stream properly
    // See https://jira.kolibree.com/browse/KLTB002-6962
  }

  private void clearWeakConnectionEmitter() {
    synchronized (this) {
      weakConnectEmitter = new WeakReference<>(null);
    }
  }

  @NonNull
  Completable onDeviceReadyCompletable() {
    return Completable.create(
            emitter -> {
              try {
                onFirstConnectionEstablished();

                emitter.onComplete();
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(onDeviceReadyScheduler())
        .doOnSubscribe(
            ignore ->
                Timber.tag(TAG)
                    .d("onDeviceReadyCompletable onFirstConnectionEstablished subscribe"))
        .doOnComplete(
            () ->
                Timber.tag(TAG)
                    .d("onDeviceReadyCompletable onFirstConnectionEstablished doOnComplete"));
  }

  @VisibleForTesting
  Scheduler onDeviceReadyScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }

  Completable notifyConnectionEstablishedCompletable() {
    return Completable.create(
            emitter -> {
              try {
                Timber.tag(TAG).d("Invoking onConnectionEstablished");
                listener.onConnectionEstablished();

                if (!emitter.isDisposed()) {
                  emitter.onComplete();
                }
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        // move to a separate thread to avoid deadlocks in onConnectionEstablished
        // listeners
        .subscribeOn(notifyListenerScheduler)
        .doOnSubscribe(
            ignore -> Timber.tag(TAG).d("notifyConnectionEstablishedCompletable subscribe"))
        .andThen(forceEmitVibrationState())
        .onErrorResumeNext(
            e -> {
              Timber.tag(TAG).w(e, "Exception, attempting disconnect");
              disconnect();
              return Completable.error(e);
            });
  }

  @VisibleForTesting
  Completable forceEmitVibrationState() {
    return Completable.create(
        emitter -> {
          if (!isRunningBootloader() && supportsBrushingEventsPolling()) {
            try {
              // force toothbrush to emit vibration state after reconnect
              getDeviceParameter(
                  new byte[] {GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_EVENTS, 0x01});
            } catch (Exception e) {
              Timber.e(e);

              emitter.tryOnError(e);
            }
          }

          emitter.onComplete();
        });
  }

  /**
   * Executes the Runnable on notifyListenerScheduler
   *
   * @param runnable
   */
  @VisibleForTesting
  void runOnListenerScheduler(@NonNull Runnable runnable) {
    DisposableUtils.addSafely(
        disposables,
        runOnListenerSchedulerCompletable(runnable)
            .subscribe(() -> {}, e -> Timber.tag(TAG).e(e, "runOnListenerScheduler")));
  }

  @VisibleForTesting
  Completable runOnListenerSchedulerCompletable(@NonNull Runnable runnable) {
    return Completable.create(
            emitter -> {
              try {
                runnable.run();

                if (!emitter.isDisposed()) {
                  emitter.onComplete();
                }
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .retry(1)
        .onErrorResumeNext(
            e -> {
              Timber.tag(TAG).w(e, "Exception, attempting disconnect");
              disconnect();
              return Completable.error(e);
            })
        .subscribeOn(notifyListenerScheduler);
  }

  @Override
  public void onBondingRequired(@NonNull BluetoothDevice device) {
    Timber.tag(TAG)
        .d(
            "onBondingRequired: a GATT_INSUFFICIENT_AUTHENTICATION occurred during the bonding of %s",
            device.getAddress());
  }

  @Override
  public void onBonded(@NonNull BluetoothDevice device) {
    Timber.tag(TAG).d("onBonded: %s has been successfully bonded", device.getAddress());
  }

  @Override
  public void onBondingFailed(@NonNull BluetoothDevice device) {
    Timber.tag(TAG).e("onBondingFailed: %s failed to bond", device.getAddress());
  }

  @Override
  public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
    Timber.tag(TAG)
        .e("onError: a BLE error has occurred message = %s, code = %s", message, errorCode);
  }

  @Override
  public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
    Timber.tag(TAG)
        .e(
            "onDeviceNotSupported: %s is not supported, required services have not been found",
            device.getAddress());
  }

  @SuppressLint("CheckResult")
  private void runActionOnBtScheduler(Action action) {
    btScheduler.scheduleDirect(
        () -> {
          try {
            action.run();
          } catch (Exception e) {
            Timber.e(e);
          }
        });
  }

  private <T> T runCallableOnBtScheduler(Callable<T> callable) {
    return Single.<T>create(
            emitter -> {
              try {
                emitter.onSuccess(callable.call());
              } catch (Exception e) {
                emitter.tryOnError(e);
              }
            })
        .subscribeOn(btScheduler)
        .blockingGet();
  }

  /*
  End of KLManagerCallbacks
   */

  // TODO maybe can be linked to KML since when KML is load this is not needed
  // In CE2, we are returning false if FW is >= 2.0.0
  @Override
  public boolean supportsGRUData() {
    return true;
  }

  @Override
  public void disableMultiUserMode() throws FailureReason {
    // no-op should only operate on ARA/E1
  }

  @Override
  public String queryRealName() throws FailureReason {
    PayloadReader response = getDeviceParameter(ParameterSet.getToothbrushNameParameterPayload());
    return response.skip(1).readString(response.length - 1);
  }

  @NotNull
  @Override
  public Completable enableOverpressureDetector(boolean enable) {
    return Completable.error(new CommandNotSupportedException());
  }

  @NotNull
  @Override
  public Completable enablePickupDetector(boolean enable) {
    return Completable.error(new CommandNotSupportedException());
  }

  @NotNull
  @Override
  public Single<Boolean> isOverpressureDetectorEnabled() {
    return Single.just(false);
  }

  abstract ToothbrushModel toothbrushModel();

  protected abstract int calibrationDataSize();
}
