package com.kolibree.android.sdk.core;

import static com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.kolibree.android.TimberTagKt;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.processedbrushings.LegacyProcessedBrushingFactory;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.brushing.Brushing;
import com.kolibree.android.sdk.connection.brushing.OfflineBrushingConsumer;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.driver.BrushingDriver;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing;
import com.kolibree.android.sdk.error.BadRecordException;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import timber.log.Timber;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>{@link Brushing} implementation
 */
final class BrushingImpl implements Brushing, DataCache {

  private static final String TAG = TimberTagKt.offlineBrushingsTagFor(BrushingImpl.class);

  private static final int NON_INIT_DURATION = 0;

  /** Events source */
  private final WeakReference<KLTBConnection> source;

  /** Brushing brushingDriver implementation */
  private final BrushingDriver brushingDriver;

  /**
   * Prevent two threads from being pulling records at the same time
   *
   * <p>pulling records is the only non thread safe public method
   */
  private final AtomicBoolean lock;

  /** Cache for default brushing duration value */
  private final AtomicInteger defaultDurationCache = new AtomicInteger(NON_INIT_DURATION);

  private final LegacyProcessedBrushingFactory processedBrushingFactory =
      new LegacyProcessedBrushingFactory(TrustedClock.INSTANCE.systemClock());

  /**
   * {@link Brushing} implementation constructor
   *
   * @param eventSource non null KLTBConnection event source
   * @param brushingDriver non null BrushingDriver
   */
  BrushingImpl(@NonNull KLTBConnection eventSource, @NonNull BrushingDriver brushingDriver) {
    this.source = new WeakReference<>(eventSource);
    this.brushingDriver = brushingDriver;
    this.lock = new AtomicBoolean(false);
  }

  @NonNull
  @Override
  public Completable monitorCurrent() {
    return brushingDriver.monitorCurrentBrushing();
  }

  @NonNull
  @Override
  public Completable setDefaultDuration(final int defaultDurationSeconds) {

    return Completable.create(
        emitter -> {
          try {
            brushingDriver.setDefaultBrushingDuration(defaultDurationSeconds);
            defaultDurationCache.set(defaultDurationSeconds);
            emitter.onComplete();
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Single<Integer> getDefaultDuration() {
    return Single.create(
        emitter -> {
          try {
            final int defaultDuration = brushingDriver.getDefaultBrushingDuration();

            defaultDurationCache.compareAndSet(NON_INIT_DURATION, defaultDuration);
            emitter.onSuccess(defaultDuration);
          } catch (Exception e) {
            Throwable throwable = e.getCause() != null ? e.getCause() : e;

            emitter.tryOnError(throwable);
          }
        });
  }

  @NonNull
  @Override
  public Single<Integer> getRecordCount() {
    return brushingDriver
        .getRemainingRecordCount()
        // session needs to be finished
        .onErrorResumeNext(
            throwable -> brushingDriver.finishExtractFileSession().andThen(Single.error(throwable)))
        .flatMap(count -> brushingDriver.finishExtractFileSession().toSingleDefault(count));
  }

  @Override
  public void pullRecords(@NonNull OfflineBrushingConsumer consumer) {

    Timber.tag(TAG).d("Pulling records started");

    final Handler ui = new Handler(Looper.getMainLooper());

    KLTBConnectionState state;

    state = source.get() != null ? source.get().state().getCurrent() : null;

    if (lock.get()) { // Check if no other thread is currently pulling records
      postError(ui, new FailureReason("Another thread is already pulling records"), consumer);
      return;
    } else if (state != null && state != ACTIVE) { // Toothbrush not connected
      postError(ui, new FailureReason("Connection is " + state.name()), consumer);
      return;
    } else {
      lock.set(true);
    }

    final WeakReference<OfflineBrushingConsumer> weakConsumer = new WeakReference<>(consumer);
    // Go async
    Timber.tag(TAG).d("Starting thread");
    new Thread(
            () -> {
              try {
                if (source.get() != null) {
                  Timber.tag(TAG).d("Starting sync");
                  consumer.onSyncStart(source.get());
                }

                int retrievedCount = 0;

                brushingDriver.startExtractFileSession().blockingAwait();

                // Pop records
                while (weakConsumer.get() != null
                    && brushingDriver.getRemainingRecordCount().blockingGet() > 0) {
                  try {
                    final OfflineBrushing offlineBrushing = brushingDriver.getNextRecord();

                    // V1 toothbrushes return null when no more records
                    if (offlineBrushing == null) {
                      Timber.tag(TAG).d("Retrieved record is null, breaking");
                      break;
                    }
                    Timber.tag(TAG).d("Proceeding with retrieved record");

                    if (offlineBrushing.isValid()) {
                      if (source.get() != null
                          && weakConsumer.get() != null
                          && weakConsumer
                              .get()
                              .onNewOfflineBrushing(
                                  source.get(),
                                  offlineBrushing,
                                  brushingDriver.getRemainingRecordCount().blockingGet())) {
                        retrievedCount++;
                        brushingDriver.deleteNextRecord().blockingAwait();
                      } else {
                        Timber.tag(TAG)
                            .w(
                                "onNewOfflineBrushing did not complete. Source is %s, weakConsumer is %s",
                                source.get(), weakConsumer.get());
                      }
                    } else {
                      Timber.tag(TAG)
                          .w(
                              "Ignoring record (duration %s ms is too short)",
                              offlineBrushing.getDuration());
                      brushingDriver.deleteNextRecord().blockingAwait();
                    }
                  } catch (BadRecordException e) { // Corrupted record
                    onBadRecordException(e);
                  } catch (RuntimeException e) {
                    if (e.getCause() != null && e.getCause() instanceof BadRecordException) {
                      onBadRecordException((BadRecordException) e.getCause());
                    } else {
                      throw e;
                    }
                  }
                }

                final int finalRetrievedCount = retrievedCount;

                ui.post(
                    () -> {
                      if (source.get() != null && weakConsumer.get() != null) {
                        weakConsumer.get().onSuccess(source.get(), finalRetrievedCount);
                      }
                    });
              } catch (final Exception e) { // In case of failure, notify on UI thread
                if (weakConsumer.get() != null) {
                  Timber.e(e);
                  postError(ui, new FailureReason(e.getMessage()), weakConsumer.get());
                }
              } finally {
                lock.set(false);
                ui.post(
                    () -> {
                      if (source.get() != null && weakConsumer.get() != null) {
                        weakConsumer.get().onSyncEnd(source.get());
                      }
                    });

                try {
                  brushingDriver.finishExtractFileSession().blockingAwait();
                } catch (Throwable throwable) {
                  Timber.e(throwable, "Error invoking finishExtractFileSession");
                }
              }
            })
        .start();
  }

  private void onBadRecordException(BadRecordException e) throws FailureReason {
    Timber.w(e, "Bad Record, deleting");
    brushingDriver.deleteNextRecord().blockingAwait();
  }

  @Override
  public void clearCache() {
    defaultDurationCache.set(NON_INIT_DURATION);
  }

  /**
   * Post an error to the main thread
   *
   * @param handler non null main thread Handler
   * @param reason non null FailureReason
   * @param l non null RecordedSessionConsumer
   */
  private void postError(
      @NonNull Handler handler,
      @NonNull final FailureReason reason,
      @NonNull final OfflineBrushingConsumer l) {
    Timber.tag(TAG).d(reason, "Error encountered!");
    handler.post(
        () -> {
          if (source.get() != null) {
            l.onFailure(source.get(), reason);
          }
        });
  }
}
