package com.kolibree.android.commons

import androidx.annotation.Keep
import dagger.Binds
import dagger.Module
import javax.inject.Inject

@Keep
interface AnimationInfoProvider {
    fun isAnimationEnabled(): Boolean
}

internal class AnimationInfoProviderImpl
@Inject constructor() : AnimationInfoProvider {
    override fun isAnimationEnabled() = true
}

@Module
abstract class AnimationInfoProviderModule {
    @Binds
    internal abstract fun bindsAnimationInfoProviderImpl(
        implementation: AnimationInfoProviderImpl
    ): AnimationInfoProvider
}
