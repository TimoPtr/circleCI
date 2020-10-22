package com.kolibree.android.app.tracker;

import android.app.Activity;
import com.kolibree.android.tracker.AnalyticsEvent;
import com.kolibree.android.tracker.EventTracker;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/** Created by Kornel on 3/16/2018. */
public class TestEventTracker implements EventTracker {

  private List<AnalyticsEvent> events = new ArrayList<>();
  private AnalyticsEvent lastEvent;

  @Inject
  public TestEventTracker() {}

  @Override
  public void sendEvent(@NotNull AnalyticsEvent event) {
    events.add(event);
    lastEvent = event;
  }

  public boolean containsEvent(AnalyticsEvent eventName) {
    return events.contains(eventName);
  }

  public List<AnalyticsEvent> getEvents() {
    return events;
  }

  public AnalyticsEvent getLastEvent() {
    return lastEvent;
  }

  @Override
  public void setCurrentScreen(@NotNull Activity activity, @NotNull String screenName) {
    // no-op
  }
}
