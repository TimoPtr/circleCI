/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.notification

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.kolibree.android.annotation.VisibleForApp

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun notification(
    presenter: NotificationPresenter,
    lambda: NotificationDsl.() -> Unit
) = NotificationDsl().apply(lambda).build(presenter)

@VisibleForApp
@Suppress("TooManyFunctions")
class NotificationDsl {

    private var data = NotificationData()

    private fun updateData(updateBlock: NotificationData.() -> NotificationData) {
        data = updateBlock(data)
    }

    fun title(title: String) {
        checkIfValueNotAssigned(data.title, "title")
        updateData { copy(title = title) }
    }

    fun body(body: String) {
        checkIfValueNotAssigned(data.body, "body")
        updateData { copy(body = body) }
    }

    fun autoCancel(autoCancel: Boolean) {
        checkIfValueNotAssigned(data.autoCancel, "autoCancel")
        updateData { copy(autoCancel = autoCancel) }
    }

    fun priority(priority: Int) {
        checkIfValueNotAssigned(data.priority, "priority")
        updateData { copy(priority = priority) }
    }

    fun priorityLow() = priority(NotificationCompat.PRIORITY_LOW)

    fun priorityDefault() = priority(NotificationCompat.PRIORITY_DEFAULT)

    fun priorityMax() = priority(NotificationCompat.PRIORITY_MAX)

    fun imageUrl(imageUrl: Uri) {
        checkIfValueNotAssigned(data.imageUrl, "imageUrl")
        updateData { copy(imageUrl = imageUrl) }
    }

    fun channel(id: String, name: String) {
        checkIfValueNotAssigned(data.channel, "channel")
        updateData { copy(channel = NotificationChannel(id, name)) }
    }

    fun channel(notificationChannel: NotificationChannel) {
        checkIfValueNotAssigned(data.channel, "channel")
        updateData { copy(channel = notificationChannel) }
    }

    fun icon(icon: Int) {
        checkIfValueNotAssigned(data.icon, "icon")
        updateData { copy(icon = icon) }
    }

    private fun checkIfValueNotAssigned(fieldValue: Any?, fieldName: String) {
        if (fieldValue != null) {
            throw IllegalStateException("$fieldName already has assigned value")
        }
    }

    fun build(presenter: NotificationPresenter) {
        verify()

        presenter.show(data)
    }

    private fun verify() {
        checkNotNull(data.title) { "title can not be empty" }
        checkNotNull(data.body) { "body can not be empty" }
    }
}
