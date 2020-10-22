/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.kolibree.android.app.notification.showNotification

fun showPushNotification(
    context: Context,
    notification: RemoteMessage.Notification,
    data: Map<String, String>
) {
    showNotification(
        context = context,
        title = notification.title.orEmpty(),
        body = notification.body.orEmpty(),
        imageUrl = notification.imageUrl,
        data = data
    )
}
