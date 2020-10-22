/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.annotation.SuppressLint
import android.app.job.JobParameters
import com.kolibree.android.app.dagger.BaseDaggerJobService
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.FAILURE
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.SCAN_NOT_NEEDED
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.SUCCESS
import dagger.android.AndroidInjection
import javax.inject.Inject
import timber.log.Timber

@SuppressLint("SpecifyJobSchedulerIdRange")
internal class ToothbrushScanJobService : BaseDaggerJobService() {

    @Inject
    lateinit var nightsWatchScanner: NightsWatchScanner

    @Inject
    lateinit var nightsWatchScheduler: NightsWatchScheduler

    override fun isDaggerReady(): Boolean = ::nightsWatchScanner.isInitialized

    override fun injectSelf() {
        AndroidInjection.inject(this)
    }

    override fun internalOnStartJob(params: JobParameters): Boolean {
        Timber.tag(tag()).i("internalOnStartJob")

        val startScanResult = nightsWatchScanner.startScan()

        maybeReschedule(startScanResult)

        return false
    }

    private fun maybeReschedule(startScanResult: StartScanResult) {
        when (startScanResult) {
            FAILURE -> nightsWatchScheduler.scheduleJob(this)
            SCAN_NOT_NEEDED -> {
                /*
                If scan is not needed, it means nights watch will never be needed until the user
                starts the app and pairs a toothbrush. So, no need to reschedule the task
                 */
            }
            SUCCESS -> {
                /*
                Under this scenario, we assume ToothbrushScannedBroadcastReceiver will eventually
                receive a scan result. Rescheduling will be done there
                 */
            }
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        nightsWatchScanner.stopScan()

        return false
    }
}
