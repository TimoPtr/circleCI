/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.rules

import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import junit.framework.TestCase.fail
import timber.log.Timber

internal fun resetRxJavaPlugins(schedulerFunction: (Scheduler) -> Scheduler) {
    RxJavaPlugins.reset()
    RxJavaPlugins.setSingleSchedulerHandler(schedulerFunction)
    RxJavaPlugins.setIoSchedulerHandler(schedulerFunction)
    RxJavaPlugins.setNewThreadSchedulerHandler(schedulerFunction)
    RxJavaPlugins.setComputationSchedulerHandler(schedulerFunction)
}

/**
 * If you call RxJavaPlugins.reset or setErrorHandler inside the block it will
 * most probably miss uncaught exception
 */
internal fun failOnRxUncaughtError(block: () -> Unit) {
    val errors = arrayListOf<Throwable>()

    RxJavaPlugins.setErrorHandler {
        errors.add(it)
    }

    block()

    RxJavaPlugins.setErrorHandler(null)

    if (errors.isNotEmpty()) {
        Timber.e("Error handler caught ${errors.size}")
        errors.forEachIndexed { index, error -> Timber.e(error, "error $index") }
        fail("Error uncaught in Rx")
    }
}
