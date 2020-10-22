package com.kolibree.bttester.tester;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.threeten.bp.ZonedDateTime;

/** Created by miguelaragues on 24/11/17. */
class TestReportWriter {

  private final AtomicInteger attemptedConnections = new AtomicInteger();
  private final AtomicInteger succesfulConnections = new AtomicInteger();
  private final AtomicInteger terminatedConnections = new AtomicInteger();

  private ZonedDateTime testStartDateTime;
  private ZonedDateTime testEndDateTime;

  private ToothbrushScanResult result;

  private TestEndReason exitReason;

  PublishSubject<String> reportSubject = PublishSubject.create();

  final StringBuffer stringBuffer = new StringBuffer();
  private Throwable throwable;

  final Set<KLTBConnection> establishedConnectionsSet = new HashSet<>();

  final List<Long> successfulConnectionTimes = new ArrayList<>();

  TestReportWriter(ToothbrushScanResult result, int duration, TimeUnit unit) {
    stringBuffer.append("Starting connection test on device ");
    stringBuffer.append(result.getName());
    stringBuffer.append("(");
    stringBuffer.append(result.getMac());
    stringBuffer.append(") ");
    stringBuffer.append(" - ");
    stringBuffer.append(result.getModel());
    stringBuffer.append("\n");
    stringBuffer.append("Test will last ");
    stringBuffer.append(duration);
    stringBuffer.append(" ");
    stringBuffer.append(unit);
    stringBuffer.append("\n");
  }

  void onStartTest(ToothbrushScanResult result) {
    testStartDateTime = ZonedDateTime.now();
    this.result = result;
  }

  void onTestCompleted(TestEndReason reason) {
    testEndDateTime = ZonedDateTime.now();

    if (exitReason == null) {
      this.exitReason = reason;
    }

    emitReport();

    reportSubject.onComplete();
  }

  Long connectionAttemptStartTimestamp;

  void onConnectionAttempt() {
    connectionAttemptStartTimestamp = System.currentTimeMillis();

    attemptedConnections.incrementAndGet();
  }

  void onSuccessfulConnection(KLTBConnection connection) {
    long connectionSuccessTimestamp = System.currentTimeMillis();
    if (connectionAttemptStartTimestamp != null) {
      successfulConnectionTimes.add(connectionSuccessTimestamp - connectionAttemptStartTimestamp);

      connectionAttemptStartTimestamp = null;
    }

    succesfulConnections.incrementAndGet();

    establishedConnectionsSet.add(connection);
  }

  void onConnectionTerminated(KLTBConnection connection) {
    connectionAttemptStartTimestamp = null;

    terminatedConnections.incrementAndGet();

    establishedConnectionsSet.remove(connection);
  }

  void emitReport() {
    reportSubject.onNext(reportSnapshot().toString());
  }

  @NonNull
  TestReport reportSnapshot() {
    return TestReport.builder(testStartDateTime, testEndDateTime, exitReason)
        .withSuccessfulConnections(succesfulConnections.get())
        .withTerminatedConnections(terminatedConnections.get())
        .withAttemptedConnections(attemptedConnections.get())
        .withNonTerminatedConnections(establishedConnectionsSet)
        .withTrowable(throwable)
        .withTimeStatsReportWriter(
            ConnectionTimeStatsReportWriter.create(
                successfulConnectionTimes, connectionAttemptStartTimestamp))
        .build();
  }

  Observable<String> reportObservable() {
    return reportSubject.startWith(stringBuffer.toString()).hide();
  }

  public void onThrowable(Throwable throwable) {
    this.throwable = throwable;
  }
}
