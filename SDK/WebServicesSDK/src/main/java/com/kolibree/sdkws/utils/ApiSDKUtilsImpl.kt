/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.utils

import android.graphics.Bitmap
import com.kolibree.sdkws.KolibreeUtils
import javax.inject.Inject

/** Created by miguelaragues on 16/1/18.  */
internal class ApiSDKUtilsImpl @Inject constructor(private val kolibreeUtils: KolibreeUtils) :
    ApiSDKUtils {
    override val deviceId: String
        get() = kolibreeUtils.deviceId

    override fun kolibrizeAvatar(bmp: Bitmap?): Bitmap? = kolibreeUtils.kolibrizeAvatar(bmp)
}
