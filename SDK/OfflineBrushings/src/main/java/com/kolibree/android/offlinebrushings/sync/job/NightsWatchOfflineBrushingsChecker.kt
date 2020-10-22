/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.content.ComponentName
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.BaseDaggerJobService
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.JobServiceIdConstants.EXTRACT_OFFLINE
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.sdk.util.IBluetoothUtils
import dagger.android.AndroidInjection
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.reflect.KClass
import timber.log.Timber

internal fun nightsWatchTagFor(clazz: KClass<*>): String =
    "\uD83C\uDF0C|${clazz.java.simpleName}" // 🌌|BT| // https://emojipedia.org/milky-way/

@SuppressLint("SpecifyJobSchedulerIdRange")
internal class NightsWatchOfflineBrushingsChecker : BaseDaggerJobService() {

    @VisibleForTesting
    val disposables = CompositeDisposable()

    @Inject
    internal lateinit var bluetoothUtils: IBluetoothUtils

    @Inject
    internal lateinit var offlineBrushingsNotifier: OfflineBrushingsNotifier

    @Inject
    internal lateinit var offlineSyncResultMapper: OfflineSyncResultProcessor

    @Inject
    internal lateinit var offlineBrushingsBackgroundExtractor: OfflineBrushingsBackgroundExtractor

    @Inject
    @field:SingleThreadScheduler
    internal lateinit var timeoutScheduler: Scheduler

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.tag(tag()).i("Job was stop")
        disposables.dispose()
        return false
    }

    @VisibleForTesting
    fun finalizeJob(params: JobParameters?) {
        Timber.tag(tag()).i("Killing the job")
        jobFinished(params, false)
    }

    @VisibleForTesting
    override fun isDaggerReady() = ::bluetoothUtils.isInitialized

    override fun injectSelf() {
        AndroidInjection.inject(this)
    }

    override fun internalOnStartJob(params: JobParameters): Boolean {
        return if (bluetoothUtils.isBluetoothEnabled) {
            disposables += offlineBrushingsBackgroundExtractor.extractOfflineBrushingsOnce()
                .delay(WAIT_FOR_BACKEND_REWARDS_SECONDS, TimeUnit.SECONDS, timeoutScheduler, false)
                .flatMap { offlineSyncResultMapper.createNotificationContent(it) }
                .doAfterTerminate { finalizeJob(params) }
                .subscribe(offlineBrushingsNotifier::showNotification, Timber.tag(tag())::e)
            true
        } else {
            Timber.tag(tag()).w("Bluetooth BLE not available cancel Job")
            false
        }
    }

    companion object {
        internal const val NOTIFICATION_CHANNEL_ID =
            "com.kolibree.offlineBrushingNotificationChannel"

        /**
         * @return JobInfo that will schedule [NightsWatchOfflineBrushingsChecker] to be run
         * immediately
         *
         * This Job won't be persisted after reboot, since we can't say if there'll still be a
         * toothbrush nearby
         */
        internal fun scheduleImmediatelyJobInfo(context: Context): JobInfo {
            return JobInfo.Builder(
                EXTRACT_OFFLINE,
                ComponentName(context, NightsWatchOfflineBrushingsChecker::class.java)
            )
                .setOverrideDeadline(0) // run immediately
                .setPersisted(false)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE).build()
        }
    }
}

@VisibleForTesting
const val WAIT_FOR_BACKEND_REWARDS_SECONDS = 5L
