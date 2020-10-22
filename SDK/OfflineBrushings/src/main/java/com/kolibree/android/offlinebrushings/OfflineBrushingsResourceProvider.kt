/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
interface OfflineBrushingsResourceProvider {

    @get:StringRes
    val notificationChannelName: Int

    @get:DrawableRes
    val smallNotificationIcon: Int

    @get:DrawableRes
    val largeNotificationIcon: Int?

    @get:StringRes
    val multipleBrushingWithSmilesNotificationTitle: Int

    @get:StringRes
    val multipleBrushingNoSmilesNotificationTitle: Int

    @get:StringRes
    val singleBrushingWithSmilesNotificationTitle: Int

    @get:StringRes
    val singleBrushingNoSmilesNotificationTitle: Int

    @get:StringRes
    val multipleBrushingWithSmilesNotificationBody: Int

    @get:StringRes
    val multipleBrushingNoSmilesNotificationBody: Int

    @get:StringRes
    val singleBrushingWithSmilesNotificationBody: Int

    @get:StringRes
    val singleBrushingNoSmilesNotificationBody: Int
}
