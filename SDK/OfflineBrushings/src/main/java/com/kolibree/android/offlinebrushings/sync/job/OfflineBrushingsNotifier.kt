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
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import com.google.common.base.Optional
import com.kolibree.android.commons.JobServiceIdConstants.EXTRACT_OFFLINE
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.OfflineBrushingsNotificationsFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.offlinebrushings.di.OfflineNotificationIntent
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber

/**
 * Manages offline brushings notifications and provides easy interface to display them
 *
 * If Parental Consent is needed, it'll never display a notification
 */
internal class OfflineBrushingsNotifier @Inject constructor(
    context: Context,
    @OfflineNotificationIntent private val notificationIntent: Optional<Provider<Intent>>,
    featuresToggles: FeatureToggleSet,
    private val resourceProvider: OfflineBrushingsResourceProvider
) {

    private val showNotificationsFeature =
        featuresToggles.toggleForFeature(OfflineBrushingsNotificationsFeature)

    private val appContext: Context = context.applicationContext

    /**
     * Create and setup a NotificationChannel for the NightsWatchOfflineBrushingsChecker notification
     *
     *
     * Mandatory for Android >= 8.1
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val notificationChannel = NotificationChannel(
            NightsWatchOfflineBrushingsChecker.NOTIFICATION_CHANNEL_ID,
            context.getString(resourceProvider.notificationChannelName),
            NotificationManager.IMPORTANCE_LOW
        ) // Low = don't vibrate
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(notificationChannel)
    }

    private fun sendNotification(
        context: Context,
        notificationBuilder: NotificationCompat.Builder,
        tag: String? = null,
        id: Int = EXTRACT_OFFLINE
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
            notificationBuilder.setChannelId(NightsWatchOfflineBrushingsChecker.NOTIFICATION_CHANNEL_ID)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(tag, id, notificationBuilder.build())
    }

    fun showNotification(notificationContent: OfflineBrushingNotificationContent) {
        Timber.d(
            "showNotification %s. Show notifications enabled %s.",
            showNotificationsFeature.value,
            notificationContent
        )

        if (!showNotificationsFeature.value) return

        if (notificationContent == OfflineBrushingNotificationContent.EMPTY) return

        val icon = resourceProvider.largeNotificationIcon?.let {
            BitmapFactory.decodeResource(appContext.resources, it)
        }

        val pendingIntent = createContentIntent(notificationContent)

        val notificationBuilder = NotificationCompat.Builder(
                appContext,
                NightsWatchOfflineBrushingsChecker.NOTIFICATION_CHANNEL_ID
            )
            .setSmallIcon(resourceProvider.smallNotificationIcon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText(notificationContent.message)
            .setContentTitle(notificationContent.title)
            .setAutoCancel(true)

        icon?.let { notificationBuilder.setLargeIcon(it) }

        pendingIntent?.let {
            notificationBuilder.setContentIntent(it)
        }

        sendNotification(appContext, notificationBuilder)
    }

    @SuppressLint("ExperimentalClassUse")
    private fun createContentIntent(notificationContent: OfflineBrushingNotificationContent): PendingIntent? {
        if (!notificationIntent.isPresent) return null

        val intent = notificationIntent.get().get()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(EXTRA_EXTRACTION_TYPE, notificationContent.notificationCode())
        intent.putExtra(EXTRA_DATETIME_TO_SHOW, notificationContent.dateToShow())

        return PendingIntent.getActivity(
            appContext,
            OFFLINE_PENDING_INTENT_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
    }
}

/**
 * Returns [Long] representing the epoch millis of the most recent OfflineBrushing on which to
 * open Checkup after clicking on a notification
 *
 * @return [Long] representing epoch millis
 *
 * null If we also extracted any [OrphanBrushing] or we didn't extract any [OfflineBrushing]
 */
@VisibleForTesting
internal fun OfflineBrushingNotificationContent.dateToShow(): Long? {
    if (orphanBrushingsDateTimes.isNotEmpty() || offlineBrushingsDateTimes.isEmpty()) return null

    return offlineBrushingsDateTimes.max()!!.toInstant().toEpochMilli()
}

@VisibleForTesting
internal fun OfflineBrushingNotificationContent.notificationCode(): Int {
    val totalOffline = offlineBrushingsDateTimes.size
    val totalOrphan = orphanBrushingsDateTimes.size

    return if (totalOrphan > 0) {
        when (totalOffline) {
            0 -> ORPHAN_BRUSHING_EXTRACTED
            else -> OFFLINE_BRUSHING_EXTRACTED // ignore the orphan
        }
    } else {
        OFFLINE_BRUSHING_EXTRACTED
    }
}

const val OFFLINE_BRUSHING_EXTRACTED = 0
const val ORPHAN_BRUSHING_EXTRACTED = 1

private const val OFFLINE_PENDING_INTENT_CODE = 9990

const val EXTRA_DATETIME_TO_SHOW = "extra_date_time"
const val EXTRA_EXTRACTION_TYPE = "extra_extraction_type"
