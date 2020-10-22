package com.kolibree.android.test.mocks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.processedbrushings.RecordedSession;
import com.kolibree.android.processedbrushings.RecordedSession.Event;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;

/** Created by miguelaragues on 17/11/17. */
public class RecordedSessionBuilder {

  private Event[] events;
  private OffsetDateTime recordedDate;
  private Long duration;

  private RecordedSessionBuilder(int events) {
    this.events = new Event[events];
  }

  private RecordedSessionBuilder() {}

  public static RecordedSessionBuilder createWithEvents(int numberOfEvents) {
    return new RecordedSessionBuilder(numberOfEvents);
  }

  public static RecordedSessionBuilder create() {
    return new RecordedSessionBuilder();
  }

  public RecordedSessionBuilder withDate(OffsetDateTime date) {
    recordedDate = date;

    return this;
  }

  public RecordedSessionBuilder withDateNow() {
    recordedDate = TrustedClock.getNowOffsetDateTime();

    return this;
  }

  public RecordedSessionBuilder withEvents(Event[] event) {
    this.events = event;

    return this;
  }

  public RecordedSessionBuilder withDuration(long durationInMillis) {
    this.duration = durationInMillis;

    return this;
  }

  public RecordedSession build() {
    final RecordedSession recordedSession = mock(RecordedSession.class);
    if (events != null) {
      when(recordedSession.getEvents()).thenReturn(events);
    }

    if (recordedDate == null) {
      recordedDate = TrustedClock.getNowOffsetDateTime();
    }
    when(recordedSession.getDate()).thenReturn(recordedDate);

    if (duration != null) {
      when(recordedSession.getDuration()).thenReturn(duration);
      when(recordedSession.getDurationObject()).thenReturn(Duration.ofSeconds(duration));
    }

    return recordedSession;
  }
}
