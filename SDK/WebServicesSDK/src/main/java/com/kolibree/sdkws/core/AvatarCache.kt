package com.kolibree.sdkws.core

import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface AvatarCache {
    fun cache(profileId: Long, pictureUrl: String?, pictureLastModifier: String? = "")

    fun getAvatarUrl(profile: IProfile): String?
}

/**
 * AvatarCache implementation that does nothing
 */
@VisibleForApp
object NoOpAvatarCache : AvatarCache {
    override fun cache(profileId: Long, pictureUrl: String?, pictureLastModifier: String?) {
        // no-op
    }

    override fun getAvatarUrl(profile: IProfile): String? {
        return profile.pictureUrl
    }
}
