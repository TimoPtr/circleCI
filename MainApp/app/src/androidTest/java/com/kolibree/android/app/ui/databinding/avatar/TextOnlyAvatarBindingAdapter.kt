/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.databinding.avatar

import android.widget.ImageView
import androidx.annotation.Keep
import com.kolibree.R
import com.kolibree.android.app.utils.AvatarUtils
import com.kolibree.databinding.bindingadapter.AvatarBindingAdapter
import javax.inject.Inject
import timber.log.Timber

@Keep
class TextOnlyAvatarBindingAdapter @Inject constructor() :
    AvatarBindingAdapter {

    override fun loadImage(imageView: ImageView, avatarUrl: String?, name: String?) {
        Timber.d("loadImage(imageView = $imageView, avatarUrl = $avatarUrl, name = $name")
        if (name == null) return
        imageView.background = AvatarUtils.getGmailLikeAvatar(imageView.context, name)
        imageView.setTag(R.id.tag_profile_name, name)
    }
}
