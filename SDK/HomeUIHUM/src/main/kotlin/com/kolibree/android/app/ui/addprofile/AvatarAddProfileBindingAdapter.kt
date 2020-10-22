/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.utils.CircleTransform
import com.kolibree.android.extensions.sanitizedUrl
import com.kolibree.android.homeui.hum.R
import com.squareup.picasso.Picasso

@BindingAdapter(value = ["avatarUrl"])
internal fun ImageView.loadImage(avatarUrl: String?) {
    if (avatarUrl == null) {
        setImageResource(R.drawable.ic_add_photo)
    } else {
        val picasso = Picasso.get()
        picasso.cancelRequest(this)

        // Picasso attempts to load url if it's an empty string
        val sanitizedAvatar = avatarUrl.sanitizedUrl()

        picasso.load(sanitizedAvatar)
            .transform(CircleTransform())
            .placeholder(R.drawable.ic_add_photo)
            .error(R.drawable.ic_add_photo)
            .into(this)
    }
}
