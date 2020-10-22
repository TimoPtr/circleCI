/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.widget.ImageView
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter

/**
 * This binding adapter is not static, so its implementation is injectable and paired
 * with our Dagger.
 *
 * More info: https://philio.me/using-android-data-binding-adapters-with-dagger-2/
 */
@Keep
interface AvatarBindingAdapter {

    @BindingAdapter(value = ["profileAvatarUrl", "profileName"], requireAll = true)
    fun loadImage(imageView: ImageView, avatarUrl: String?, name: String?)
}
