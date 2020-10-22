/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.app.test

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.test.rules.TestSchedulerRxSchedulersOverrideRule
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Rule

/** Created by miguelaragues on 20/2/18.  */
@VisibleForApp
open class BaseAsyncUnitTest : CommonBaseTest() {

    @get:Rule
    val asyncScheduler =
        TestSchedulerRxSchedulersOverrideRule()

    protected fun testScheduler(): TestScheduler = asyncScheduler.testScheduler

    protected fun advanceTimeBy(delayTime: Long, unit: TimeUnit?) {
        testScheduler().advanceTimeBy(delayTime, unit)
    }
}
