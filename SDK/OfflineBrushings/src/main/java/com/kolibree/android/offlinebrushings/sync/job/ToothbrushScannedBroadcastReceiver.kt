/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.scan.IntentScanResultProcessor
import dagger.android.AndroidInjection
import javax.inject.Inject
import timber.log.Timber

/**
 * BroadcastReceiver to be invoked when Android receives a scan result for a Toothbrush
 *
 * open for testing :(
 */
internal open class ToothbrushScannedBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var jobScheduler: JobScheduler

    @Inject
    lateinit var nightsWatchScanner: NightsWatchScanner

    @Inject
    lateinit var nightsWatchScheduler: NightsWatchScheduler

    @Inject
    lateinit var intentScanResultProcessor: IntentScanResultProcessor

    override fun onReceive(context: Context, intent: Intent) {
        injectSelf(context)

        if (toothbrushResultsFound(intent)) {
            Timber.tag(tag()).d("Toothbrush results found!. Extras: ${intent.extras}")
            nightsWatchScanner.stopScan()

            scheduleOfflineBrushingsExtraction(context)

            nightsWatchScheduler.scheduleJob(context)
        } else {
            Timber.tag(tag()).d("NO toothbrush results found. Extras: ${intent.extras}")
        }
    }

    private fun toothbrushResultsFound(intent: Intent) =
        intentScanResultProcessor.process(intent).isNotEmpty()

    @VisibleForTesting
    open fun injectSelf(context: Context) {
        AndroidInjection.inject(this, context)
    }

    @VisibleForTesting
    fun scheduleOfflineBrushingsExtraction(context: Context) {
        val jobInfo = NightsWatchOfflineBrushingsChecker.scheduleImmediatelyJobInfo(context)
        if (jobScheduler.schedule(jobInfo) != JobScheduler.RESULT_SUCCESS) {
            Timber.tag(tag()).w("Failed to schedule NightsWatchOfflineBrushingsChecker")
        } else {
            Timber.tag(tag()).i("Scheduled NightsWatchOfflineBrushingsChecker for immediate run")
        }
    }
}
