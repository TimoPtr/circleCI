/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import dagger.android.AndroidInjection
import javax.inject.Inject
import timber.log.Timber

/**
 * BroadcastReceiver to schedule NightsWatch mechanism after user rebooted the phone
 *
 * open for testing :-(
 */
internal open class NightsWatchBootBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var nightsWatchScheduler: NightsWatchScheduler

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(tag()).i("onReceive")
        injectSelf(context)

        nightsWatchScheduler.scheduleImmediateJob(context)
    }

    @VisibleForTesting
    open fun injectSelf(context: Context) {
        AndroidInjection.inject(this, context)
    }
}
