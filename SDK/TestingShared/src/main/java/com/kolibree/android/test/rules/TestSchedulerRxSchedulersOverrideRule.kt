package com.kolibree.android.test.rules

import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler.ExecutorWorker
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** Created by miguelaragues on 19/2/17.  */
@VisibleForApp
class TestSchedulerRxSchedulersOverrideRule : TestRule {
    val testScheduler = TestScheduler()

    override fun apply(
        base: Statement,
        d: Description
    ): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { testScheduler }
                RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
                RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }
                RxAndroidPlugins.setMainThreadSchedulerHandler { IMMEDIATE_SCHEDULER }
                failOnRxUncaughtError {
                    try {
                        base.evaluate()
                    } finally {
                        RxJavaPlugins.reset()
                        RxAndroidPlugins.reset()
                    }
                }
            }
        }
    }

    @VisibleForApp
    companion object {
        private val IMMEDIATE_SCHEDULER: Scheduler = object : Scheduler() {
            override fun scheduleDirect(
                run: Runnable,
                delay: Long,
                unit: TimeUnit
            ): Disposable {
                // Changing delay to 0 prevents StackOverflowErrors when scheduling with a delay.
                return super.scheduleDirect(run, 0, unit)
            }

            override fun createWorker(): Worker = ExecutorWorker({ obj: Runnable -> obj.run() }, true)
        }
    }
}
