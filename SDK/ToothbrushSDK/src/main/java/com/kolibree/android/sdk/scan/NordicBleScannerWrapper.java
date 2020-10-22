package com.kolibree.android.sdk.scan;

import static com.kolibree.android.TimberTagKt.bluetoothTagFor;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils;
import com.kolibree.android.sdk.error.BluetoothNotEnabledException;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/** Created by miguelaragues on 24/11/17. */
public final class NordicBleScannerWrapper implements ToothbrushScanner {
  static final String TAG = bluetoothTagFor(NordicBleScannerWrapper.class);

  private final BluetoothWindowedScanner windowedScanner;

  @VisibleForTesting final ScanSettings scanSettings;

  private final IBluetoothUtils bluetoothUtils;

  private final ScanCallbackProvider scanCallbackProvider;

  private final HashMap<BluetoothDevice, AtomicBoolean> isScanningMap = new HashMap<>();
  private final BluetoothLeScannerCompat bleScanner;

  @VisibleForTesting volatile Disposable bluetoothStateDisposable = null;

  NordicBleScannerWrapper(IBluetoothUtils bluetoothUtils, BluetoothLeScannerCompat bleScanner) {
    this(
        bluetoothUtils,
        bleScanner,
        new BluetoothWindowedScanner(bleScanner, bluetoothUtils),
        new ScanCallbackProvider());
  }

  @VisibleForTesting
  NordicBleScannerWrapper(
      IBluetoothUtils bluetoothUtils,
      BluetoothLeScannerCompat bleScanner,
      BluetoothWindowedScanner windowedScanner,
      ScanCallbackProvider scanCallbackProvider) {
    this.bleScanner = bleScanner;
    this.windowedScanner = windowedScanner;
    this.bluetoothUtils = bluetoothUtils;
    this.scanCallbackProvider = scanCallbackProvider;

    // Settings cache
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      scanSettings = getMarshmallowScanSettings();
    } else {
      scanSettings = getLollipopScanSettings();
    }
  }

  /*
  Do not remove volatile keyword
   */
  private static volatile NordicBleScannerWrapper singleton;

  @NonNull
  static ToothbrushScanner singleton(IBluetoothUtils bluetoothUtils) {
    NordicBleScannerWrapper localField = singleton;
    if (localField == null) {
      synchronized (NordicBleScannerWrapper.class) {
        localField = singleton;
        if (localField == null) {
          singleton =
              localField =
                  new NordicBleScannerWrapper(
                      bluetoothUtils, BluetoothLeScannerCompat.getScanner());
        }
      }
    }

    return localField;
  }

  @Override
  public boolean startScan(
      @NotNull Context context,
      @NotNull List<String> macAddresses,
      @NotNull PendingIntent pendingIntent) {
    if (!bluetoothUtils.isBluetoothEnabled()) return false;

    if (!macAddresses.isEmpty()) {
      List<ScanFilter> filters = createScanFilterForMac(macAddresses);

      ScanSettings settings = pendingIntentScanSettings();

      Timber.d("Invoking startScan with %s", pendingIntent);
      bleScanner.startScan(filters, settings, context, pendingIntent);
    }

    return true;
  }

  @Override
  public void stopScan(@NotNull Context context, @NotNull PendingIntent pendingIntent) {
    if (bluetoothUtils.isBluetoothEnabled()) {
      Timber.d("Invoking stopScan with %s", pendingIntent);
      bleScanner.stopScan(context, pendingIntent);
    }
  }

  @Override
  public void scanFor(@NonNull SpecificToothbrushScanCallback scanCallback)
      throws BluetoothNotEnabledException {
    List<ScanFilter> filters = createScanFilterForDevice(scanCallback.bluetoothDevice());

    startScanWithFilters(filters, scanCallback);
  }

  @Override
  public void startScan(@NotNull AnyToothbrushScanCallback callback, boolean includeBondedDevices)
      throws BluetoothNotEnabledException {
    startScanWithFilters(Collections.emptyList(), callback);
  }

  /**
   * Checks if KLScanCallback still has listeners after callback is removed as listeners. If it
   * doesn't have listeners, invokes stopScan
   *
   * <p>If the original scan was for a specific device, it stop scans immediately. Otherwise, it'll
   * stop a scan with a small delay
   *
   * @param callback
   */
  @Override
  public void stopScan(@NotNull ToothbrushScanCallback callback) {
    KLScanCallback klScanCallback = scanCallbackProvider.get(callback);
    if (klScanCallback != null) {
      klScanCallback.removeListener(callback);

      if (shouldStopScanning(klScanCallback)) {
        if (shouldStopScanWithDelay(callback)) {
          windowedScanner.stopScanWithDelay(klScanCallback);
        } else {
          windowedScanner.stopScan(klScanCallback);
        }
      } else {
        Timber.tag(TAG)
            .w(
                "Not stopping scan. Has listeners %s. Is scanning %s",
                klScanCallback.hasListeners(), isScanning(klScanCallback.getBluetoothDevice()));
      }
    } else {
      Timber.tag(TAG).w("Not stopping scan. klScanCallback is null for %s", callback);
    }
  }

  @VisibleForTesting
  boolean shouldStopScanWithDelay(@NotNull ToothbrushScanCallback callback) {
    return bluetoothUtils.isBluetoothEnabled() && callback instanceof AnyToothbrushScanCallback;
  }

  @VisibleForTesting
  boolean shouldStopScanning(KLScanCallback klScanCallback) {
    if (!bluetoothUtils.isBluetoothEnabled()) return true;

    return !klScanCallback.hasListeners()
        && isScanning(klScanCallback.getBluetoothDevice()).compareAndSet(true, false);
  }

  @VisibleForTesting
  AtomicBoolean isScanning(@Nullable BluetoothDevice bluetoothDevice) {
    synchronized (isScanningMap) {
      if (!isScanningMap.containsKey(bluetoothDevice)) {
        isScanningMap.put(bluetoothDevice, new AtomicBoolean());
      }

      return isScanningMap.get(bluetoothDevice);
    }
  }

  @VisibleForTesting
  void startScanWithFilters(List<ScanFilter> filters, @NonNull ToothbrushScanCallback callback)
      throws BluetoothNotEnabledException {
    if (!bluetoothUtils.isBluetoothEnabled()) {
      throw new BluetoothNotEnabledException();
    }

    if (isScanning(callback.bluetoothDevice()).compareAndSet(false, true)) {
      windowedScanner.startScan(filters, scanSettings, scanCallbackProvider.getOrCreate(callback));
    } else {
      Timber.tag(TAG).w("Not starting scan, already scanning for %s", callback);
    }

    stopScanningOnBluetoothOff(callback);
  }

  @VisibleForTesting
  void stopScanningOnBluetoothOff(ToothbrushScanCallback callback) {
    /*
    This instance has state that needs to be reset whenever BT goes off. Thus, we want to receive
    bluetooth off events throughout its lifespan.

    Previously, we tried to be smart and stop listening under certain conditions, but we sometimes
    ended in a state were we ignored startScan requests
     */
    if (shouldListenToBluetoothState()) {
      synchronized (this) {
        if (shouldListenToBluetoothState()) {
          bluetoothStateDisposable =
              bluetoothUtils
                  .bluetoothStateObservable()
                  .subscribeOn(Schedulers.io())
                  .onTerminateDetach()
                  .distinctUntilChanged()
                  .filter(bluetoothEnabled -> !bluetoothEnabled)
                  .subscribe(ignore -> onBluetoothOff(callback), Timber::e);
        }
      }
    }
  }

  private boolean shouldListenToBluetoothState() {
    return bluetoothStateDisposable == null || bluetoothStateDisposable.isDisposed();
  }

  @VisibleForTesting
  void onBluetoothOff(ToothbrushScanCallback callback) {
    stopScan(callback);

    windowedScanner.onBluetoothOff();

    clearIsScanningCache();
  }

  private void clearIsScanningCache() {
    synchronized (isScanningMap) {
      isScanningMap.clear();
    }
  }

  @NonNull
  @VisibleForTesting
  List<ScanFilter> createScanFilterForDevice(@Nullable BluetoothDevice device) {
    if (device != null) {
      /*
      temporary hack to also return results for M1 devices on bootloader

      See https://jira.kolibree.com/browse/KLTB002-4507
       */
      final String dfuMac = DfuUtils.getDFUMac(device);

      return createScanFilterForMac(device.getAddress(), dfuMac);
    }

    return new ArrayList<>();
  }

  @VisibleForTesting
  List<ScanFilter> createScanFilterForMac(List<String> addresses) {
    List<ScanFilter> filters = new ArrayList<>();

    for (String mac : addresses) {
      filters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
    }

    return filters;
  }

  @VisibleForTesting
  List<ScanFilter> createScanFilterForMac(String... addresses) {
    List<ScanFilter> filters = new ArrayList<>();

    for (String mac : addresses) {
      filters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
    }

    return filters;
  }

  /**
   * Get scan settings for devices running Android L
   *
   * @return non null {@link ScanSettings}
   */
  @NonNull
  private ScanSettings getLollipopScanSettings() {
    return lollipopScanSettingsBuilder().build();
  }

  /**
   * Get scan settings for devices running Android >= M
   *
   * <p>Here we force default values since many bad BLE stack implementations do not
   *
   * @return non null {@link ScanSettings}
   */
  @TargetApi(Build.VERSION_CODES.M)
  @NonNull
  private ScanSettings getMarshmallowScanSettings() {
    return marshmallowScanSettingsBuilder().build();
  }

  @NonNull
  private ScanSettings.Builder lollipopScanSettingsBuilder() {
    return new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
  }

  @NonNull
  private ScanSettings.Builder marshmallowScanSettingsBuilder() {
    return lollipopScanSettingsBuilder().setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
  }

  @VisibleForTesting
  ScanSettings pendingIntentScanSettings() {
    return new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setReportDelay(PENDING_INTENT_SCAN_REPORT_DELAY)
        .build();
  }

  /** Number of seconds before a scan to be reported to our PendingIntent is sent */
  private static final long PENDING_INTENT_SCAN_REPORT_DELAY = 10000L;
}
