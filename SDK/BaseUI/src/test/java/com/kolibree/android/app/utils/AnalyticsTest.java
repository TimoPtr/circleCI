package com.kolibree.android.app.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.kolibree.android.app.test.CommonBaseTest;
import com.kolibree.android.tracker.Analytics;
import com.kolibree.android.tracker.AnalyticsEvent;
import com.kolibree.android.tracker.EventTracker;
import org.junit.Test;

public class AnalyticsTest extends CommonBaseTest {

  @Test
  public void nonInitilized_eventTracker_sendEventDoesNotCrash() {
    Analytics.send(new AnalyticsEvent("bla"));
  }

  @Test
  public void initilized_eventTracker_containsExpectedTracker() {
    EventTracker tracker = mock(EventTracker.class);

    Analytics.init(tracker);
    assertEquals(tracker, Analytics.INSTANCE.getEventTracker());

    AnalyticsEvent event = new AnalyticsEvent("bla");
    Analytics.send(event);
    verify(tracker).sendEvent(event);
  }
}
