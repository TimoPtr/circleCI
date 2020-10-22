package com.kolibree.android.sdk.core;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.detectors.DetectorsManager;
import com.kolibree.android.sdk.connection.detectors.RNNDetector;
import com.kolibree.android.sdk.connection.detectors.RawDetector;
import com.kolibree.android.sdk.connection.detectors.SVMDetector;
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState;
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState;
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState;
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState;
import com.kolibree.android.sdk.connection.detectors.data.WeightedMouthZone;
import com.kolibree.android.sdk.connection.detectors.listener.RNNDetectorListener;
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener;
import com.kolibree.android.sdk.connection.detectors.listener.SVMDetectorListener;
import com.kolibree.android.sdk.core.driver.SensorDriver;
import com.kolibree.android.sdk.core.notification.ListenerPool;
import com.kolibree.android.sdk.core.notification.UniqueListenerPool;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState;
import com.kolibree.android.sdk.version.SoftwareVersion;
import com.kolibree.kml.MouthZone16;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.lang.ref.WeakReference;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>{@link DetectorsManager} implementation
 */
final class DetectorsManagerImpl
    implements DetectorsManager, DataCache, SVMDetector, RNNDetector, RawDetector {

  /*
  Multiple notifications to drivers were causing issues, so we want to delay them if
  a driver notification is already scheduled
   */
  @VisibleForTesting static final int NOTIFY_DELAY_MILLIS = 50;

  /** Sensor driver */
  private final SensorDriver driver;

  /** GRU data validity */
  private boolean validGruData;

  /** GRU data version */
  private SoftwareVersion gruDataVersion;

  // Listener pools for each detector type
  private final ListenerPool<SVMDetectorListener> svmListeners;
  private final ListenerPool<RNNDetectorListener> rnnListeners;
  private final ListenerPool<RawDetectorListener> rawListeners;

  // Cache
  private boolean svmOn;
  private boolean rnnOn;
  private boolean rawDataOn;
  private boolean rightHanded;

  private final Handler notifyHandler;
  @VisibleForTesting long lastNotificationTimestamp;

  /**
   * {@link DetectorsManager} implementation constructor
   *
   * @param toothbrushModel non null ToothbrushModel
   * @param driver non null SensorDriver
   */
  DetectorsManagerImpl(@NonNull ToothbrushModel toothbrushModel, @NonNull SensorDriver driver) {
    this(toothbrushModel, driver, new Handler(Looper.getMainLooper()));
  }

  @VisibleForTesting
  DetectorsManagerImpl(
      @NonNull ToothbrushModel toothbrushModel,
      @NonNull SensorDriver driver,
      Handler notifyHandler) {
    this.driver = driver;

    svmListeners = new UniqueListenerPool<>("svm", true);
    rawListeners = new UniqueListenerPool<>("raw", false);
    rnnListeners = new UniqueListenerPool<>("rnn", true);

    this.notifyHandler = notifyHandler;
  }

  @Override
  public void setRightHanded(boolean rightHanded) {
    synchronized (this) {
      this.rightHanded = rightHanded;
    }
    notifyDriver();
  }

  @NonNull
  @Override
  public SVMDetector probableMouthZones() {
    return this;
  }

  @Nullable
  @Override
  public synchronized RNNDetector mostProbableMouthZones() {
    // No RNN on Kolibree V1 or on newer FW
    return gruDataVersion != null ? this : null;
  }

  @NonNull
  @Override
  public RawDetector rawData() {
    return this;
  }

  @NonNull
  @Override
  public float[] getCalibrationData() {
    return driver.getSensorCalibration();
  }

  @Override
  public void enableRawDataNotifications() {
    driver.enableRawDataNotifications();
  }

  @Override
  public void disableRawDataNotifications() {
    driver.disableRawDataNotifications();
  }

  @Override
  public void enableDetectionNotifications() {
    driver.enableDetectionNotifications();
  }

  @Override
  public void disableDetectionNotifications() {
    driver.disableDetectionNotifications();
  }

  @Override
  @NonNull
  public Flowable<PlaqlessRawSensorState> plaqlessRawDataNotifications() {
    return driver.plaqlessRawDataNotifications();
  }

  @Override
  @NonNull
  public Flowable<PlaqlessSensorState> plaqlessNotifications() {
    return driver.plaqlessNotifications();
  }

  @NonNull
  @Override
  public Flowable<PlaqlessRingLedState> plaqlessRingLedState() {
    return driver.plaqlessRingLedState();
  }

  @Override
  public synchronized void clearCache() {
    svmOn = false;
    rnnOn = false;
    rawDataOn = false;
    rightHanded = false;
  }

  @Override
  public synchronized boolean hasValidGruData() {
    return validGruData;
  }

  @NonNull
  @Override
  public synchronized SoftwareVersion getGruDataVersion() {
    if (gruDataVersion == null) {
      return SoftwareVersion.NULL;
    }

    return gruDataVersion;
  }

  @Override
  public void register(@NonNull RawDetectorListener listener) {
    synchronized (this) {
      rawDataOn = rawListeners.add(listener) > 0;
    }
    notifyDriver();
  }

  @Override
  public void register(@NonNull SVMDetectorListener listener) {
    synchronized (this) {
      svmOn = svmListeners.add(listener) > 0;
    }
    notifyDriver();
  }

  @Override
  public void register(@NonNull RNNDetectorListener listener) {
    synchronized (this) {
      rnnOn = rnnListeners.add(listener) > 0;
    }
    notifyDriver();
  }

  @Override
  public void unregister(@NonNull RawDetectorListener listener) {
    synchronized (this) {
      rawDataOn = rawListeners.remove(listener) > 0;
    }
    notifyDriver();
  }

  @Override
  public void unregister(@NonNull SVMDetectorListener listener) {
    synchronized (this) {
      svmOn = svmListeners.remove(listener) > 0;
    }
    notifyDriver();
  }

  @Override
  public void unregister(@NonNull RNNDetectorListener listener) {
    synchronized (this) {
      rnnOn = rnnListeners.remove(listener) > 0;
    }
    notifyDriver();
  }

  @NonNull
  @Override
  public Flowable<OverpressureState> overpressureStateFlowable() {
    return driver.overpressureStateFlowable();
  }

  @NonNull
  @Override
  public Completable enableOverpressureDetector(boolean enable) {
    return driver.enableOverpressureDetector(enable);
  }

  @NotNull
  @Override
  public Single<Boolean> isOverpressureDetectorEnabled() {
    return driver.isOverpressureDetectorEnabled();
  }

  @NotNull
  @Override
  public Completable enablePickupDetector(boolean enable) {
    return driver.enablePickupDetector(enable);
  }

  void onSVMData(@NonNull final KLTBConnection source, @NonNull final List<MouthZone16> data) {
    svmListeners.notifyListeners(listener -> listener.onSVMData(source, data));
  }

  void onRNNData(
      @NonNull final KLTBConnection source, @NonNull final List<WeightedMouthZone> data) {
    if (gruDataVersion != null) { // Device is not a Kolibree V1
      rnnListeners.notifyListeners(listener -> listener.onRNNData(source, data));
    }
  }

  void onRawData(@NonNull final KLTBConnection source, @NonNull final RawSensorState data) {
    rawListeners.notifyListeners(listener -> listener.onRawData(source, data));
  }

  /**
   * Set GRU data information
   *
   * @param valid true if GRU data is valid, false otherwise
   * @param version non null SoftwareVersion
   */
  synchronized void setGruInfo(boolean valid, @NonNull SoftwareVersion version) {
    validGruData = valid;
    gruDataVersion = version;
  }

  /**
   * Notify driver that it should enable or disable detectors
   *
   * <p>There are issues in E1 where if we spam with commands too often, the FW crashes, so we try
   * to delay calls. It doesn't fix the issue but it may alleviate it
   */
  @VisibleForTesting
  void notifyDriver() {
    NotifyDriverRunnable notifyDriverRunnable;
    long delay;
    synchronized (this) {
      notifyDriverRunnable = new NotifyDriverRunnable(driver, svmOn, rnnOn, rawDataOn, rightHanded);

      delay = getNotifyDelay();
    }

    notifyHandler.postDelayed(notifyDriverRunnable, delay);
  }

  @VisibleForTesting
  synchronized long getNotifyDelay() {
    long delay;
    if (lastNotificationTimestamp != 0
        && (System.currentTimeMillis() - lastNotificationTimestamp) < NOTIFY_DELAY_MILLIS) {
      delay = NOTIFY_DELAY_MILLIS;
    } else {
      delay = 0;
    }

    lastNotificationTimestamp = System.currentTimeMillis();
    return delay;
  }

  /** Notifies the driver of the state of the detectors at the moment it's created */
  @VisibleForTesting
  static class NotifyDriverRunnable implements Runnable {
    private final WeakReference<SensorDriver> driver;

    private final boolean rightHanded;
    private final boolean svmOn;
    private final boolean rnnOn;
    private final boolean rawDataOn;

    NotifyDriverRunnable(
        SensorDriver driver, boolean svmOn, boolean rnnOn, boolean rawDataOn, boolean rightHanded) {
      this.driver = new WeakReference<>(driver);
      this.svmOn = svmOn;
      this.rnnOn = rnnOn;
      this.rawDataOn = rawDataOn;
      this.rightHanded = rightHanded;
    }

    /*
    Always runs on same thread
     */
    @Override
    public void run() {
      if (driver.get() != null) {
        try {
          driver.get().onSensorControl(svmOn, rnnOn, rawDataOn, rightHanded);
        } catch (FailureReason reason) {
          reason.printStackTrace();
        }
      } else {
        Timber.e("Tried to notify driver, but was null. %s", toString());
      }
    }

    @NonNull
    @Override
    public String toString() {
      return String.format(
          "NotifyDriverRunnable state:"
              + "\nsvmOn = %s"
              + "\nrnnOn = %s"
              + "\nrawDataOn = %s"
              + "\nrightHanded = %s",
          svmOn, rnnOn, rawDataOn, rightHanded);
    }
  }
}
