package com.kolibree.android.test.dagger

import com.kolibree.android.commons.AnimationInfoProvider
import dagger.Module
import dagger.Provides

@Module
object EspressoAnimationInfoProviderModule {
    @Provides
    fun providesAnimationInfoProvide(): AnimationInfoProvider {
        return object : AnimationInfoProvider {
            override fun isAnimationEnabled() = false
        }
    }
}
