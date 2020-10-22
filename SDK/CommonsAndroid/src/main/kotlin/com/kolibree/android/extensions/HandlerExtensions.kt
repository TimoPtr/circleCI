/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep

@Keep
fun Runnable.runOnMainThread() {
    Handler(Looper.getMainLooper()).post(this)
}

@Keep
fun (() -> Unit).runOnMainThread() {
    Handler(Looper.getMainLooper()).post(this)
}
