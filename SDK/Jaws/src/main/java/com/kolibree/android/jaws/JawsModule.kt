package com.kolibree.android.jaws

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererFactory
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererFactoryImpl
import com.kolibree.android.jaws.color.ColorJawsModule
import dagger.Binds
import dagger.Module

/**
 * Jaws module
 */
@Module(includes = [ColorJawsModule::class])
@Keep
abstract class JawsModule {

    @Binds
    internal abstract fun bindCoachPlusRendererFactory(
        factory: CoachPlusRendererFactoryImpl
    ): CoachPlusRendererFactory

    @AppScope
    @Binds
    internal abstract fun provideMemoryManager(memoryManagerImpl: MemoryManagerImpl): MemoryManager

    @AppScope
    @Binds
    internal abstract fun bindMemoryManagerInternal(
        memoryManagerImpl: MemoryManagerImpl
    ): MemoryManagerInternal
}
