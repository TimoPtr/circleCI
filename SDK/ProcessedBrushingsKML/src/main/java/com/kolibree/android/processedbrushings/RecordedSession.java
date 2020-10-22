/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings;

import static com.kolibree.android.commons.BrushingConstantsKt.DEFAULT_BRUSHING_GOAL;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.commons.BrushingConstantsKt;
import com.kolibree.kml.MouthZone16;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;

/** Stored brushing data model used by legacy offline brushings protocol */
@Keep
public class RecordedSession {

  /** Record date */
  private final OffsetDateTime date;

  /** Record duration in milliseconds */
  private final long duration;

  /** Event list (zone changes) */
  private final Event[] events;

  /**
   * Brushing record from V2
   *
   * @param date brushing date
   * @param duration brushing duration in milliseconds
   * @param events list of events
   */
  public RecordedSession(@NonNull OffsetDateTime date, long duration, @NonNull Event[] events) {
    this.date = date;
    this.duration = duration;
    this.events = events;
  }

  /**
   * Get brushing date
   *
   * @return non null {@link ZonedDateTime}
   */
  public OffsetDateTime getDate() {
    return date;
  }

  /**
   * Get brushing duration
   *
   * @return long duration in milliseconds
   */
  public long getDuration() {
    return duration;
  }

  public Duration getDurationObject() {
    return Duration.ofSeconds(duration);
  }

  /**
   * Check record validity (brushing must have a duration &gt; 10 seconds)
   *
   * @return true if valid, false otherwise
   */
  public boolean isValid() {
    return duration >= TimeUnit.SECONDS.toMillis(BrushingConstantsKt.MIN_BRUSHING_DURATION_SECONDS);
  }

  /**
   * Get brushing zone events
   *
   * @return non null {@link Event} list on V2 toothbrush
   */
  @Nullable
  public Event[] getEvents() {
    return events;
  }

  @Nullable
  public String computeProcessedData() {
    return LegacyProcessedDataGenerator.computeProcessedData(this, DEFAULT_BRUSHING_GOAL);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordedSession that = (RecordedSession) o;
    return duration == that.duration
        && Objects.equals(date, that.date)
        && Arrays.equals(events, that.events);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(date, duration);
    result = 31 * result + Arrays.hashCode(events);
    return result;
  }

  @Override
  public String toString() {
    return "RecordedSession{"
        + "date="
        + date
        + ", duration="
        + duration
        + ", events="
        + Arrays.toString(events)
        + '}';
  }

  /** Ara and Connect M1 stored brushing zone pass */
  @Keep
  public static final class Event {

    /** Event datetime */
    public final int dateTime;

    /** Vibrator state */
    public final boolean vibrator;

    /** Brushed zone */
    public final MouthZone16 zone;

    public Event(int dateTime, boolean vibrator, @NonNull MouthZone16 zone) {
      this.dateTime = dateTime;
      this.vibrator = vibrator;
      this.zone = zone;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Event event = (Event) o;
      return dateTime == event.dateTime && vibrator == event.vibrator && zone == event.zone;
    }

    @Override
    public int hashCode() {

      return Objects.hash(dateTime, vibrator, zone);
    }

    @Override
    public String toString() {
      return "Event{" + "dateTime=" + dateTime + ", vibrator=" + vibrator + ", zone=" + zone + '}';
    }
  }
}
