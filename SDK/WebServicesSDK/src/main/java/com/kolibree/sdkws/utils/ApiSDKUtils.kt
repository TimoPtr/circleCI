/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.utils

import android.graphics.Bitmap
import androidx.annotation.Keep

/** Created by miguelaragues on 16/1/18.  */
@Keep
interface ApiSDKUtils {
    /**
     * Get the device unique ID
     *
     * @return String, The stored, or a new device ID
     */
    val deviceId: String
    fun kolibrizeAvatar(bmp: Bitmap?): Bitmap?
}
