/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.notification

import android.net.Uri
import androidx.annotation.DrawableRes
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
data class NotificationData(
    var title: String? = null,
    var body: String? = null,
    val channel: NotificationChannel? = null,
    var autoCancel: Boolean? = null,
    var priority: Int? = null,
    var imageUrl: Uri? = null,
    @DrawableRes val icon: Int? = null
)
