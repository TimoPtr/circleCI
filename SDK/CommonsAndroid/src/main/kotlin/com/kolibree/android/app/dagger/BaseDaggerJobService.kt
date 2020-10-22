/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import androidx.annotation.Keep
import timber.log.Timber

/**
 * Helper class to inject JobServices and prevent Android 7 issues
 *
 * https://kolibree.atlassian.net/browse/KLTB002-9808
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
@Keep
abstract class BaseDaggerJobService : JobService() {

    override fun onCreate() {
        safeAndroidInjection()
        super.onCreate()
    }

    final override fun onStartJob(params: JobParameters): Boolean {
        if (isDaggerReady().not()) {
            safeAndroidInjection()

            if (isDaggerReady().not()) {
                Timber.e("Could not perform Android injection, canceling job...")
                return false
            }
        }

        return internalOnStartJob(params)
    }

    private fun safeAndroidInjection() =
        try {
            injectSelf()
        } catch (runtimeException: RuntimeException) {
            Timber.e(runtimeException)
        }

    abstract fun isDaggerReady(): Boolean

    abstract fun injectSelf()

    abstract fun internalOnStartJob(params: JobParameters): Boolean
}
