/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.notification

import android.app.NotificationChannel as AndroidNotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kolibree.R
import com.kolibree.android.app.ui.home.getMainActivityIntentWithPushNotificationPayload
import com.kolibree.android.app.ui.notification.NotificationChannel
import com.kolibree.android.failearly.FailEarly
import com.squareup.picasso.Picasso
import java.io.IOException
import timber.log.Timber

internal const val MINIMUM_BODY_LENGTH_FOR_BIG_TEXT_STYLE = 100
private const val DEFAULT_NOTIFICATION_ID = 0
private const val NO_REQUEST_CODE = 0

@Suppress("LongMethod")
fun showNotification(
    context: Context,
    title: String,
    body: String,
    imageUrl: Uri? = null,
    autoCancel: Boolean = true,
    data: Map<String, String> = emptyMap(),
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    notificationChannel: NotificationChannel = DefaultNotificationChannel.get(context),
    @DrawableRes icon: Int = R.drawable.push_notification_icon
) {
    with(context) {
        val builder =
            NotificationCompat.Builder(this, notificationChannel.id)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this, R.color.push_notification_color))
                .setStyle(chooseStyleForTheMessage(imageUrl, body))
                .setPriority(priority)
                .setAutoCancel(autoCancel)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        NO_REQUEST_CODE,
                        getMainActivityIntentWithPushNotificationPayload(this, data),
                        PendingIntent.FLAG_ONE_SHOT
                    )
                )

        NotificationManagerCompat.from(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = AndroidNotificationChannel(
                    notificationChannel.id,
                    notificationChannel.name,
                    notificationImportance(priority)
                )
                createNotificationChannel(channel)
            }
            notify(DEFAULT_NOTIFICATION_ID, builder.build())
        }
    }
}

@RequiresApi(VERSION_CODES.O)
private fun notificationImportance(priority: Int) = when (priority) {
    NotificationCompat.PRIORITY_MIN -> NotificationManager.IMPORTANCE_MIN
    NotificationCompat.PRIORITY_LOW -> NotificationManager.IMPORTANCE_LOW
    NotificationCompat.PRIORITY_HIGH -> NotificationManager.IMPORTANCE_HIGH
    NotificationCompat.PRIORITY_MAX -> NotificationManager.IMPORTANCE_MAX
    else -> NotificationManager.IMPORTANCE_DEFAULT
}

@VisibleForTesting
fun chooseStyleForTheMessage(
    imageUrl: Uri?,
    body: String?
): NotificationCompat.Style? =
    when {
        imageUrl != null -> NotificationCompat.BigPictureStyle()
            .bigPicture(loadImage(imageUrl))
        body?.length?.let { it >= MINIMUM_BODY_LENGTH_FOR_BIG_TEXT_STYLE } ?: false ->
            NotificationCompat.BigTextStyle().bigText(body)
        else -> null
    }

private fun loadImage(imageUrl: Uri?): Bitmap? = imageUrl?.let { url ->
    FailEarly.failIfExecutedOnMainThread()
    return try {
        Picasso.get().load(url).get()
    } catch (e: IOException) {
        Timber.e(e)
        null
    }
}
