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
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * This rule registers SchedulerHooks for RxJava and RxAndroid to ensure that subscriptions always
 * subscribeOn and observeOn Schedulers.immediate(). Warning, this rule will resetProcedureStatus
 * RxAndroidPlugins and RxJavaPlugins after each test so if the application code uses RxJava plugins
 * this may affect the behaviour of the testing method.
 *
 *
 * See
 * https://medium.com/@fabioCollini/testing-asynchronous-rxjava-code-using-mockito-8ad831a16877#.ahj5h7jmg
 * See
 * https://github.com/fabioCollini/TestingRxJavaUsingMockito/blob/master/app/src/test/java/it/codingjam/testingrxjava/TestSchedulerRule.java
 */
@VisibleForApp
class EspressoImmediateRxSchedulersOverrideRule : TestRule {

    override fun apply(
        base: Statement,
        description: Description
    ): Statement = object : Statement() {
        override fun evaluate() {
            resetRxJavaPlugins { SCHEDULER_INSTANCE }
            failOnRxUncaughtError {
                try {
                    base.evaluate()
                } finally {
                    RxAndroidPlugins.reset()
                    RxJavaPlugins.reset()
                }
            }
        }
    }

    fun scheduler(): Scheduler = SCHEDULER_INSTANCE
}

private val SCHEDULER_INSTANCE = Schedulers.trampoline()
