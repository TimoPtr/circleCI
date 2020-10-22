/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.app.utils

import android.content.Context
import android.graphics.Bitmap
import com.kolibree.android.annotation.VisibleForApp
import java.io.File

@VisibleForApp
interface AvatarDataStore {
    fun saveToStorage(
        context: Context,
        avatar: Bitmap
    ): File?
}
