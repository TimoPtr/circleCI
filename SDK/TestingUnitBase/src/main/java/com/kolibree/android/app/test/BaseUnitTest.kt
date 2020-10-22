package com.kolibree.android.app.test

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.test.rules.EventTrackerRule
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.android.tracker.EventTracker
import org.junit.Rule

/** Created by miguelaragues on 28/5/17.  */
@VisibleForApp
abstract class BaseUnitTest : CommonBaseTest() {

    /*
      If you want to copy paste this rule into a Kotlin test class, make sure that the scope of the
      @Rule annotation targets the property getter, otherwise it won't work

      @get:Rule
      val overrideSchedulersRule = UnitTestImmediateRxSchedulersOverrideRule()
   */
    @get:Rule
    val overrideSchedulersRule = UnitTestImmediateRxSchedulersOverrideRule()

    @get:Rule
    val eventTrackerRule = EventTrackerRule()

    protected val eventTracker: EventTracker = eventTrackerRule.eventTracker
}
