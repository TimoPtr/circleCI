package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.unity.UnityGameModule
import com.kolibree.android.test.utils.AttachUnityPlayerWrapper
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object EspressoUnityGameModule {

    @Provides
    @AppScope
    internal fun providesAttachUnityPlayerWrapper(): AttachUnityPlayerWrapper = mock()

    @Provides
    @AppScope
    @Named(UnityGameModule.ATTACH_UNITY_PLAYER_TO_VIEW)
    internal fun providesAttachUnityPlayerToViewFlag(wrapper: AttachUnityPlayerWrapper): Boolean = wrapper.isAttach
}
