/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.net.Uri
import android.widget.VideoView
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter

@Keep
@BindingAdapter("videoUrl")
fun VideoView.playVideo(videoUrl: String?) {
    videoUrl?.let {
        setVideoURI(Uri.parse(it))
        start()
    }
}
