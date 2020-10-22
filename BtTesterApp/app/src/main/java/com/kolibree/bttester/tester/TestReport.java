package com.kolibree.bttester.tester;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import java.text.DecimalFormat;
import java.util.Set;
import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/** Created by miguelaragues on 24/11/17. */
public class TestReport {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private final ZonedDateTime testEndTime;
  private final ZonedDateTime testStartTime;
  private final TestEndReason endReason;
  private final int terminatedConnections;
  private final int successfulConnections;
  private final int attemptedConnections;
  private final Throwable throwable;
  private final Set<KLTBConnection> nonTerminatedConnectionsSet;
  private final ConnectionTimeStatsReportWriter timeStatsReportWriter;

  private TestReport(
      ZonedDateTime testStartTime,
      ZonedDateTime testEndTime,
      TestEndReason endReason,
      int successfulConnections,
      int terminatedConnections,
      int attemptedConnections,
      Throwable throwable,
      Set<KLTBConnection> nonTerminatedConnectionsSet,
      ConnectionTimeStatsReportWriter timeStatsReportWriter) {
    this.testEndTime = testEndTime;
    this.testStartTime = testStartTime;
    this.endReason = endReason;
    this.successfulConnections = successfulConnections;
    this.terminatedConnections = terminatedConnections;
    this.attemptedConnections = attemptedConnections;
    this.throwable = throwable;
    this.nonTerminatedConnectionsSet = nonTerminatedConnectionsSet;
    this.timeStatsReportWriter = timeStatsReportWriter;
  }

  static Builder builder(
      ZonedDateTime testStartTime, ZonedDateTime testEndTime, TestEndReason endReason) {
    return new Builder(testStartTime, testEndTime, endReason);
  }

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("\tTest started at ");
    stringBuilder.append(DATE_TIME_FORMATTER.format(testStartTime));
    stringBuilder.append(attemptedConnections);
    stringBuilder.append("\n\tAttempted connections: ");
    stringBuilder.append(attemptedConnections);
    stringBuilder.append("\n\tSuccessful connections: ");
    stringBuilder.append(successfulConnections);
    if (successfulConnections > 0) {
      stringBuilder.append("\n\tTime/Connections ratio: ");
      stringBuilder.append(
          DECIMAL_FORMAT.format(
              Duration.between(testStartTime, ZonedDateTime.now()).getSeconds()
                  * 1d
                  / successfulConnections));
    }

    if (timeStatsReportWriter != null) {
      stringBuilder.append(timeStatsReportWriter.writeReport("\n\t"));
    }

    if (testEndTime != null) {
      stringBuilder.append("\n\tSuccessful Never terminated connections: ");
      stringBuilder.append(nonTerminatedConnectionsSet.size());

      stringBuilder.append("\n\tTest finished at: ");
      stringBuilder.append(DATE_TIME_FORMATTER.format(testEndTime));

      if (endReason != null) {
        stringBuilder.append("\n\tWith code: ");
        stringBuilder.append(endReason);
      }

      if (throwable != null) {
        stringBuilder.append("\n\tWith throwable: ");
        stringBuilder.append(throwable.getMessage());
      }
    }

    return stringBuilder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestReport that = (TestReport) o;

    if (terminatedConnections != that.terminatedConnections) {
      return false;
    }
    if (successfulConnections != that.successfulConnections) {
      return false;
    }
    if (attemptedConnections != that.attemptedConnections) {
      return false;
    }
    if (testEndTime != null ? !testEndTime.equals(that.testEndTime) : that.testEndTime != null) {
      return false;
    }
    if (testStartTime != null
        ? !testStartTime.equals(that.testStartTime)
        : that.testStartTime != null) {
      return false;
    }
    if (endReason != that.endReason) {
      return false;
    }
    if (throwable != null ? !throwable.equals(that.throwable) : that.throwable != null) {
      return false;
    }
    if (nonTerminatedConnectionsSet != null
        ? !nonTerminatedConnectionsSet.equals(that.nonTerminatedConnectionsSet)
        : that.nonTerminatedConnectionsSet != null) {
      return false;
    }
    return timeStatsReportWriter != null
        ? timeStatsReportWriter.equals(that.timeStatsReportWriter)
        : that.timeStatsReportWriter == null;
  }

  @Override
  public int hashCode() {
    int result = testEndTime != null ? testEndTime.hashCode() : 0;
    result = 31 * result + (testStartTime != null ? testStartTime.hashCode() : 0);
    result = 31 * result + (endReason != null ? endReason.hashCode() : 0);
    result = 31 * result + terminatedConnections;
    result = 31 * result + successfulConnections;
    result = 31 * result + attemptedConnections;
    result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
    result =
        31 * result
            + (nonTerminatedConnectionsSet != null ? nonTerminatedConnectionsSet.hashCode() : 0);
    result = 31 * result + (timeStatsReportWriter != null ? timeStatsReportWriter.hashCode() : 0);
    return result;
  }

  static class Builder {

    private final ZonedDateTime testEndTime;
    private final ZonedDateTime testStartTime;
    private final TestEndReason endReason;
    private int successfulConnections = 0;
    private int terminatedConnections = 0;
    private int attemptedConnections = 0;
    private Throwable throwable;
    private Set<KLTBConnection> nonTerminatedConnectionsSet;
    private ConnectionTimeStatsReportWriter averageConnectionTime;

    private Builder(
        ZonedDateTime testStartTime, ZonedDateTime testEndTime, TestEndReason endReason) {
      this.testEndTime = testEndTime;
      this.testStartTime = testStartTime;
      this.endReason = endReason;
    }

    Builder withSuccessfulConnections(int successfulConnections) {
      this.successfulConnections = successfulConnections;

      return this;
    }

    Builder withTerminatedConnections(int terminatedConnections) {
      this.terminatedConnections = terminatedConnections;

      return this;
    }

    Builder withAttemptedConnections(int attemptedConnections) {
      this.attemptedConnections = attemptedConnections;

      return this;
    }

    Builder withTrowable(Throwable throwable) {
      this.throwable = throwable;

      return this;
    }

    Builder withNonTerminatedConnections(Set<KLTBConnection> nonTerminatedConnectionsSet) {
      this.nonTerminatedConnectionsSet = nonTerminatedConnectionsSet;

      return this;
    }

    Builder withTimeStatsReportWriter(ConnectionTimeStatsReportWriter averageConnectionTime) {
      this.averageConnectionTime = averageConnectionTime;

      return this;
    }

    @NonNull
    TestReport build() {
      return new TestReport(
          testStartTime,
          testEndTime,
          endReason,
          successfulConnections,
          terminatedConnections,
          attemptedConnections,
          throwable,
          nonTerminatedConnectionsSet,
          averageConnectionTime);
    }
  }
}
