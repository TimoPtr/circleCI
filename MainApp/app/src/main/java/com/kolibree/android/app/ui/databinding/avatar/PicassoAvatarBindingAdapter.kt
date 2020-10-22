/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.databinding.avatar

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.kolibree.R
import com.kolibree.android.app.ui.cache.picassoCachedAvatarRequest
import com.kolibree.android.app.utils.AvatarUtils
import com.kolibree.android.extensions.sanitizedUrl
import com.kolibree.databinding.bindingadapter.AvatarBindingAdapter
import com.squareup.picasso.Picasso
import kotlin.math.abs

class PicassoAvatarBindingAdapter : AvatarBindingAdapter {

    override fun loadImage(imageView: ImageView, avatarUrl: String?, name: String?) {
        if (name.isNullOrBlank()) return

        with(imageView) {
            Picasso.get().cancelRequest(this)

            val avatarPlaceholder = context.createPlaceholder(name)

            // Picasso attempts to load url if it's an empty string
            val sanitizedAvatar = avatarUrl?.sanitizedUrl()

            picassoCachedAvatarRequest(sanitizedAvatar)
                .placeholder(avatarPlaceholder)
                .error(avatarPlaceholder)
                .into(this)
        }
    }

    private fun Context.createPlaceholder(name: String): Drawable {
        val hashCode = abs(name.hashCode())
        val colorScheme = colorSchemes[hashCode % colorSchemes.size]

        return AvatarUtils.getGmailLikeAvatar(
            this,
            name,
            colorScheme.first,
            colorScheme.second,
            ResourcesCompat.getFont(this, R.font.hind_light),
            null
        )
    }

    companion object {

        private val colorSchemes = arrayOf(
            R.color.avatar_color_0 to R.color.white,
            R.color.avatar_color_1 to R.color.white,
            R.color.avatar_color_2 to R.color.white,
            R.color.avatar_color_3 to R.color.white,
            R.color.avatar_color_4 to R.color.white
        )
    }
}
