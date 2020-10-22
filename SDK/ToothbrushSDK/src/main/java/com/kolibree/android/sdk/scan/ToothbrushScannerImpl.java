package com.kolibree.android.sdk.scan;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.core.notification.ListenerPool;
import com.kolibree.android.sdk.core.notification.UniqueListenerPool;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * Created by aurelien on 23/08/16.
 *
 * <p>Kolibree Common Toothbrush scanner type
 */
@Keep
abstract class ToothbrushScannerImpl implements ToothbrushScanner {

  /** Scan listeners */
  private final ListenerPool<ToothbrushScanCallback> listeners;

  private boolean scanning;

  ToothbrushScannerImpl() {
    listeners = new UniqueListenerPool<>("toothbrush scanner impl", true);
  }

  /**
   * Add a toothbrush scan callback implementation to get notified on scan events
   *
   * @param callback a non null ToothbrushScanCallback implementation
   */
  private void addToothbrushScanCallback(@NonNull final ToothbrushScanCallback callback) {
    listeners.add(callback);
  }

  /**
   * Remove a toothbrush scan callback
   *
   * @param callback a non null ToothbrushScanCallback implementation
   */
  private void removeToothbrushScanCallback(@NonNull ToothbrushScanCallback callback) {
    listeners.remove(callback);
  }

  /**
   * Look for Kolibree toothbrushes
   *
   * @param includeBondedDevices if set the scanner will also add bonded toothbrushes
   */
  @Override
  public final void startScan(
      @NotNull AnyToothbrushScanCallback callback, boolean includeBondedDevices) {
    if (scanning) {
      Timber.w("Scan already in progress");
      return;
    }

    scanning = true;

    if (includeBondedDevices) {
      for (ToothbrushScanResult result : getBondedToothbrushes()) {
        onToothbrushFound(result);
      }
    }

    addToothbrushScanCallback(callback);

    startScanInternal();
    Timber.i("%s is started", getClass().getSimpleName());
  }

  /** Stop scan */
  @Override
  public final void stopScan(@NotNull ToothbrushScanCallback callback) {
    removeToothbrushScanCallback(callback);

    if (scanning) {
      stopScanInternal();
      scanning = false;
    }
    Timber.i("%s is stopped", getClass().getSimpleName());
  }

  /**
   * Toothbrush scanner implementations should call this method when a new toothbrush is found
   *
   * @param result a non null Toothbrush result instance
   */
  final void onToothbrushFound(@NonNull final ToothbrushScanResult result) {
    listeners.notifyListeners(listener -> listener.onToothbrushFound(result));
  }

  /**
   * Get already bonded toothbrushes list
   *
   * @return non null {@link ToothbrushScanResult} list
   */
  @NonNull
  public abstract List<ToothbrushScanResult> getBondedToothbrushes();

  /** Toothbrush scanner implementations should call this method when an error occurs */
  protected final void onError() {
    listeners.notifyListeners(listener -> listener.onError(-1));
  }

  /**
   * Toothbrush scanner implementations have to implement this method to know when they should start
   * scanning
   */
  protected abstract void startScanInternal();

  /**
   * Toothbrush scanners implementations have to implement this method to know when they should stop
   * scanning
   */
  protected abstract void stopScanInternal();
}
