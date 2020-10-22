package com.kolibree.android.sdk.core;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.parameters.Parameters;
import com.kolibree.android.sdk.core.driver.ParametersDriver;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.threeten.bp.OffsetDateTime;

/**
 * Created by aurelien on 16/08/17.
 *
 * <p>{@link Parameters} implementation
 *
 * <p>This class is Thread Safe
 */
@AnyThread
final class ParametersImpl implements Parameters, DataCache {

  /** Toothbrush parameters driver */
  private final ParametersDriver driver;

  /** Owner device ID cache */
  private final AtomicLong ownerDeviceCache = new AtomicLong(-1L);

  /** Auto shutdown timeout value cache */
  private final AtomicInteger autoShutdownTimeoutCache = new AtomicInteger(-1);

  /**
   * {@link Parameters} implementation constructor
   *
   * @param driver non null ParametersDriver
   */
  ParametersImpl(@NonNull ParametersDriver driver) {
    this.driver = driver;
  }

  @NonNull
  @Override
  public Completable setOwnerDevice(final long ownerDevice) {

    return Completable.create(
        emitter -> {
          try {
            driver.setOwnerDevice(ownerDevice);
            synchronized (ownerDeviceCache) {
              ownerDeviceCache.set(ownerDevice);
            }
            emitter.onComplete();
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Single<Long> getOwnerDevice() {
    return Single.create(
        emitter -> {
          try {
            long ownerDevice = driver.getOwnerDevice();
            long innerOwnerDevice;
            synchronized (ownerDeviceCache) {
              ownerDeviceCache.compareAndSet(-1L, ownerDevice);

              // avoid emitting inside synchronized block
              innerOwnerDevice = ownerDeviceCache.get();
            }

            emitter.onSuccess(innerOwnerDevice);
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Single<OffsetDateTime> getTime() {

    return Single.create(
        emitter -> {
          try {
            emitter.onSuccess(driver.getTime());
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Completable setAutoShutdownTimeout(final int autoShutdownTimeout) {

    return Completable.create(
        emitter -> {
          try {
            driver.setAutoReconnectTimeout(autoShutdownTimeout);

            synchronized (autoShutdownTimeoutCache) {
              autoShutdownTimeoutCache.set(autoShutdownTimeout);
            }
            emitter.onComplete();
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Single<Integer> getAutoShutdownTimeout() {

    return Single.create(
        emitter -> {
          try {
            int autoShutdownTimeout = driver.getAutoShutdownTimeout();
            int innerAutoShutdownTimeout;
            synchronized (autoShutdownTimeoutCache) {
              autoShutdownTimeoutCache.compareAndSet(-1, autoShutdownTimeout);

              // avoid emitting on alien methods
              innerAutoShutdownTimeout = autoShutdownTimeoutCache.get();
            }

            emitter.onSuccess(innerAutoShutdownTimeout);
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @Override
  public void clearCache() {
    synchronized (autoShutdownTimeoutCache) {
      synchronized (ownerDeviceCache) {
        autoShutdownTimeoutCache.set(-1);
        ownerDeviceCache.set(-1L);
      }
    }
  }
}
