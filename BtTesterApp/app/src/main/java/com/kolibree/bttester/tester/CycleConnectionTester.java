package com.kolibree.bttester.tester;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionStateListener;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import com.kolibree.bttester.ObjectWatcher;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

/** Created by miguelaragues on 24/11/17. */
public class CycleConnectionTester implements ConnectionStateListener, ConnectionTester {

  private final CompositeDisposable disposables = new CompositeDisposable();
  private final boolean reuseConnection;

  private TestReportWriter testReportWriter;

  private static final int CONNECTION_TIMEOUT_SECONDS = 20;
  private ToothbrushScanResult testedResult;
  private KLTBConnection connection;
  private WeakReference<KolibreeService> serviceWeakReference;
  private Disposable testDisposable;
  private Disposable connectionTimeoutDisposable;

  private final Scheduler testScheduler = Schedulers.newThread();
  private final Scheduler disconnectScheduler = Schedulers.newThread();

  /**
   * @param reuseConnection flag if we should destroy the connection on each cycle or if we should
   *     reuse it. True to reuse it, false to destroy it
   */
  private CycleConnectionTester(boolean reuseConnection) {
    this.reuseConnection = reuseConnection;
  }

  /**
   * Creates a CycleConnectionTester with the reuseConnection flag
   *
   * <p>If reuse flag is false, we destroy the connection after each successful attempt. This'd
   * replicate the scenario where the app is killed and restarted on each cycle
   *
   * <p>If reuse flag is true, we'll try to reuse the same connection for every cycle, which would
   * be closer to a continuous use of the application
   */
  public static CycleConnectionTester create(boolean reuseConnection) {
    return new CycleConnectionTester(reuseConnection);
  }

  public Observable<String> testFor(
      @NonNull KolibreeService service,
      ToothbrushScanResult result,
      int testDuration,
      TimeUnit testDurationUnit) {
    serviceWeakReference = new WeakReference<>(service);
    this.testedResult = result;

    testReportWriter = new TestReportWriter(result, testDuration, testDurationUnit);

    testDisposable =
        Observable.timer(testDuration, testDurationUnit)
            .onErrorReturn(
                throwable -> {
                  testReportWriter.onThrowable(throwable);
                  abortTest(TestEndReason.UNKNOWN);
                  return 1L;
                })
            .doOnTerminate(disposables::dispose)
            .doOnSubscribe(
                ignore -> {
                  testReportWriter.onStartTest(result);

                  Timber.d("testFor attemptConnection");
                  attemptConnection();
                })
            .subscribeOn(testScheduler)
            .onTerminateDetach()
            .subscribe(ignore -> onTestDurationComplete(), Throwable::printStackTrace);

    disposables.add(testDisposable);

    return testReportWriter.reportObservable();
  }

  private void onTestDurationComplete() {
    connection.state().unregister(CycleConnectionTester.this);

    disposables.dispose();

    abortConnectionTimeoutTimer();

    Timber.d(
        "Test duration complete "
            + connection
            + " with state "
            + (connection == null ? "null" : connection.state().getCurrent()));
    forgetConnection();

    testReportWriter.onTestCompleted(TestEndReason.PERIOD_COMPLETED);
  }

  private void abortTest(TestEndReason abortReason) {
    if (testDisposable != null && !testDisposable.isDisposed()) {
      testDisposable.dispose();
    }

    testReportWriter.onTestCompleted(abortReason);

    disposables.dispose();

    forgetConnection();
  }

  private void attemptConnection() {
    if (!testIsCompleted()) {
      Timber.d("attemptConnection");
      if (serviceWeakReference.get() == null) {
        abortTest(TestEndReason.SERVICE_CONNECTION_LOST);

        return;
      }

      connectionTimeoutDisposable =
          Observable.timer(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
              .filter(ignore -> connection == null)
              .doOnSubscribe(
                  ignore -> {
                    Timber.d("Invoking createConnection " + connection);
                    connection =
                        serviceWeakReference.get().createAndEstablishConnection(testedResult);
                    Timber.d(
                        "Invoked createConnection "
                            + connection
                            + " testedResult "
                            + testedResult.getMac()
                            + ", with state "
                            + connection.state().getCurrent());
                    connection.state().register(this);
                    Timber.d("After register connection state listener");

                    /*
                    If the connection is already active, we'll never be move to TERMINATED, so we'll never
                    be notified. Force the notification
                     */
                    if (connection.state().getCurrent() == KLTBConnectionState.ACTIVE) {
                      onConnectionStateChanged(connection, connection.state().getCurrent());
                    }
                  })
              .subscribe(ignore -> onConnectionTimedOut(), Throwable::printStackTrace);

      disposables.add(connectionTimeoutDisposable);
    }
  }

  @Override
  public void onConnectionStateChanged(
      @NonNull KLTBConnection connection, @NonNull KLTBConnectionState newState) {
    if (weAreListeningToIncomingConnection(connection)) {
      Timber.d(
          "onConnectionStateChanged to "
              + newState
              + ". Current Connection: "
              + CycleConnectionTester.this.connection);
      abortConnectionTimeoutTimer();

      switch (newState) {
        case NEW:
          testReportWriter.onConnectionAttempt();
          testReportWriter.emitReport();
        case TERMINATED:
          onConnectionTerminated(connection);
          break;
        case ACTIVE:
          onConnectionEstablished();
          Timber.d("After ACTIVE \n\n\n\nconnection");

          disconnect();
      }
    } else {
      Timber.d(
          "Ignoring state changed of "
              + connection
              + " with new state "
              + newState
              + ", current connection is "
              + CycleConnectionTester.this.connection);
      connection.state().unregister(CycleConnectionTester.this);

      if (newState == KLTBConnectionState.TERMINATING
          || newState == KLTBConnectionState.TERMINATED) {
        onConnectionTerminated(connection);
      }
    }
    Timber.d("exit onConnectionStateChanged to %s", newState);
  }

  private boolean weAreListeningToIncomingConnection(KLTBConnection incomingConnection) {
    return !disposables.isDisposed() && connection != null && connection.equals(incomingConnection);
  }

  private void onConnectionTimedOut() {
    Timber.w(
        "Connection " + connection + " timed out after " + CONNECTION_TIMEOUT_SECONDS + " seconds");
    abortTest(TestEndReason.CONNECTION_TIMED_OUT);
  }

  private void abortConnectionTimeoutTimer() {
    if (connectionTimeoutDisposable != null && !connectionTimeoutDisposable.isDisposed()) {
      connectionTimeoutDisposable.dispose();

      connectionTimeoutDisposable = null;
    }
  }

  private void onConnectionEstablished() {
    testReportWriter.onSuccessfulConnection(connection);
  }

  private void onConnectionTerminated(KLTBConnection terminatedConnection) {
    testReportWriter.onConnectionTerminated(terminatedConnection);
    testReportWriter.emitReport();

    Timber.d("onConnectionTerminated " + terminatedConnection + " vs instance field " + connection);
    if (!reuseConnection) {
      connection.state().unregister(this);

      ObjectWatcher.watch(connection, "Connection");

      this.connection = null;

      attemptConnection();
    }
  }

  private boolean testIsCompleted() {
    return disposables.isDisposed();
  }

  private void disconnect() {
    Timber.d("disconnect");
    if (serviceWeakReference.get() == null) {
      abortTest(TestEndReason.SERVICE_CONNECTION_LOST);

      return;
    }

    if (reuseConnection) {
      if (connection != null) {
        disposables.add(
            Completable.fromAction(() -> connection.disconnect())
                .subscribeOn(disconnectScheduler)
                .subscribe(() -> {}, Timber::e));
      }
    } else {
      forgetConnection();
    }
  }

  private void forgetConnection() {
    if (connection != null) {
      try {
        if (serviceWeakReference.get() != null) {
          Timber.d("Tester forget " + connection);
          serviceWeakReference.get().forget(connection);
        }
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    } else {
      Timber.w("Can't forget connection because it's null");
    }
  }
}
