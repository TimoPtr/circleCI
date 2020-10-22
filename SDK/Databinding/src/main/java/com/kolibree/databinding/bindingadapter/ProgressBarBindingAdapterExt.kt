/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter

@Keep
@BindingAdapter(value = ["android:progress"])
fun ProgressBar.setAnimatedProgress(progress: Int) {
    if (Build.VERSION.SDK_INT >= N) {
        setProgress(progress, true)
    } else {
        setProgress(progress)
    }
}

@Keep
@BindingAdapter(value = ["android:progress"])
fun ProgressBar.setAnimatedProgressSafe(progress: Int?) {
    if (progress != null) {
        setAnimatedProgress(progress)
    }
}
