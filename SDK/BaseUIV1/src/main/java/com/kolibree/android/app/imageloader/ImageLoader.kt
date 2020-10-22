/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.imageloader

import android.widget.ImageView
import androidx.annotation.Keep
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import javax.inject.Inject

@Keep
interface ImageLoader {
    fun load(imageView: ImageView, url: String, onComplete: (success: Boolean) -> Unit = {})
}

internal class ImageLoaderImpl @Inject constructor() : ImageLoader {

    /**
     * TODO: please rewrite it to databinding adapter + dagger
     * https://jira.kolibree.com/browse/KLTB002-6747
     */
    override fun load(imageView: ImageView, url: String, onComplete: (Boolean) -> Unit) {
        Picasso.get()
            .load(url)
            .into(imageView, LogoCallback(onComplete))
    }

    inner class LogoCallback(private val onComplete: (Boolean) -> Unit) : Callback {
        override fun onError(e: Exception?) {
            onComplete(false)
        }

        override fun onSuccess() {
            onComplete(true)
        }
    }
}
