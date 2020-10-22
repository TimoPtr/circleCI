/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.cache

import com.kolibree.android.app.utils.CircleTransform
import com.kolibree.sdkws.core.AvatarCacheWarmUp
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import javax.inject.Inject

class PicassoAvatarCacheWarmUp @Inject constructor() : AvatarCacheWarmUp {
    override fun cache(pictureUrl: String?) {
        if (pictureUrl != null && pictureUrl.isNotBlank()) {
            picassoCachedAvatarRequest(pictureUrl).fetch()
        }
    }
}

/**
 * @return [RequestCreator] used to cache a profile's avatar
 */
fun picassoCachedAvatarRequest(pictureUrl: String?): RequestCreator {
    return Picasso.get()
        .load(pictureUrl)
        .transform(CircleTransform())
}
