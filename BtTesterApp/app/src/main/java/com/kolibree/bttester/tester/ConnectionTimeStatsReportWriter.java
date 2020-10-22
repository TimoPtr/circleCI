package com.kolibree.bttester.tester;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/** Created by miguelaragues on 30/11/17. */
class ConnectionTimeStatsReportWriter {

  private final List<Long> connectionTimes;
  private final Long lastConnectionAttemptStartTimestamp;

  private long averageConnectionTime = -1,
      longestConnectionTime = -1,
      millisSinceLastConnection = -1,
      shortestConnectionTime = -1;

  static ConnectionTimeStatsReportWriter create(
      List<Long> connectionTimes, @Nullable Long lastConnectionAttemptStartTimestamp) {
    return new ConnectionTimeStatsReportWriter(
        connectionTimes, lastConnectionAttemptStartTimestamp);
  }

  private ConnectionTimeStatsReportWriter(
      List<Long> connectionTimes, @Nullable Long lastConnectionAttemptStartTimestamp) {
    this.connectionTimes = connectionTimes;

    this.lastConnectionAttemptStartTimestamp = lastConnectionAttemptStartTimestamp;

    calculateStats();
  }

  private void calculateStats() {
    if (lastConnectionAttemptStartTimestamp != null) {
      millisSinceLastConnection = System.currentTimeMillis() - lastConnectionAttemptStartTimestamp;
    }

    if (connectionTimes.isEmpty()) {
      return;
    }

    long connectionTimesSum = 0;
    shortestConnectionTime = Long.MAX_VALUE;
    for (int i = 0, size = connectionTimes.size(); i < size; i++) {
      long currentConnectionTime = connectionTimes.get(i);
      connectionTimesSum += currentConnectionTime;
      longestConnectionTime = Math.max(longestConnectionTime, currentConnectionTime);
      shortestConnectionTime = Math.min(shortestConnectionTime, currentConnectionTime);
    }

    averageConnectionTime = connectionTimesSum / connectionTimes.size();
  }

  @NonNull
  CharSequence writeReport(String prependToNewLine) {
    StringBuilder stringBuilder = new StringBuilder();
    if (!connectionTimes.isEmpty()) {
      stringBuilder.append(prependToNewLine);
      stringBuilder.append("Average establish time (millis): ");
      stringBuilder.append(averageConnectionTime);

      stringBuilder.append(prependToNewLine);
      stringBuilder.append("Longest establish time (millis): ");
      stringBuilder.append(longestConnectionTime);

      stringBuilder.append(prependToNewLine);
      stringBuilder.append("Shortest establish time (millis): ");
      stringBuilder.append(shortestConnectionTime);

      if (millisSinceLastConnection > averageConnectionTime) {
        stringBuilder.append(prependToNewLine);
        stringBuilder.append("Millis since last successful connection: ");
        stringBuilder.append(millisSinceLastConnection);
      }
    }

    return stringBuilder;
  }
}
