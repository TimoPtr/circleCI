package com.kolibree.sdkws.core

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface AvatarCacheWarmUp {
    fun cache(pictureUrl: String?)
}

/**
 * AvatarCache implementation that does nothing
 */
@VisibleForApp
object NoOpAvatarCacheWarmUp : AvatarCacheWarmUp {
    override fun cache(pictureUrl: String?) {
        // no-op
    }
}
