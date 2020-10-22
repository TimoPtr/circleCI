/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kolibree.android.failearly.FailEarly
import javax.inject.Inject

typealias Factory = @JvmSuppressWildcards WorkerFactory

/**
 * Worker Factory allowing dependency injection and multi-binding inside our WorkManager
 * It has been inspired by [this article](https://proandroiddev.com/dagger-2-setup-with-workmanager-a-complete-step-by-step-guild-bb9f474bde37)
 * Learnt the hard way : @[JvmSuppressWildcards] must stay, otherwise, it fails.
 */
@Suppress("MaxLineLength")
class KolibreeWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, Factory>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return getFactoryProvider(workerClassName)?.createWorker(
            appContext, workerClassName, workerParameters
        )
    }

    private fun getFactoryProvider(workerClassName: String): WorkerFactory? {
        return try {
            val clazz = Class.forName(workerClassName)
            val foundEntry = workerFactories.entries.find { clazz.isAssignableFrom(it.key) }

            if (foundEntry == null) {
                FailEarly.fail("Class not found in $workerFactories ($workerClassName)")
            }

            foundEntry?.value
        } catch (e: ClassNotFoundException) {
            FailEarly.fail("Unknown worker class : $workerClassName", e)

            null
        }
    }
}
