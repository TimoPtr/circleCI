package cn.colgate.colgateconnect.cache

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.kolibree.sdkws.core.AvatarCacheWarmUp
import javax.inject.Inject

class DemoAvatarCacheWarmUp @Inject constructor(private val context: Context) : AvatarCacheWarmUp {

    override fun cache(pictureUrl: String?) {
        Glide.with(context).load(pictureUrl).transform(CircleCrop()).preload()
    }
}
