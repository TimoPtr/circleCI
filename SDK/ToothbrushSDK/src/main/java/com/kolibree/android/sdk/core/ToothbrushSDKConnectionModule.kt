package com.kolibree.android.sdk.core

import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCaseModule
import com.kolibree.android.sdk.dagger.ToothbrushSDKScope
import dagger.Binds
import dagger.Module

@Module(includes = [SynchronizeBrushingModeUseCaseModule::class])
internal abstract class ToothbrushSDKConnectionModule {
    @Binds
    @ToothbrushSDKScope
    abstract fun
        bindsInternalKLTBConnectionPoolManager(impl: KLTBConnectionPoolManagerImpl): InternalKLTBConnectionPoolManager

    @Binds
    abstract fun
        bindsConnectionPoolManager(impl: InternalKLTBConnectionPoolManager): KLTBConnectionPool
}
