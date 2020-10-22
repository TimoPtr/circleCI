/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.rules

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.EventTracker
import com.kolibree.android.tracker.VoidEventTracker
import com.nhaarman.mockitokotlin2.mock
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@VisibleForApp
class EventTrackerRule : TestRule {

    val eventTracker: EventTracker = mock()

    override fun apply(
        base: Statement,
        description: Description
    ): Statement = object : Statement() {
        override fun evaluate() {
            try {
                Analytics.init(eventTracker)
                base.evaluate()
            } finally {
                Analytics.init(VoidEventTracker())
            }
        }
    }
}
