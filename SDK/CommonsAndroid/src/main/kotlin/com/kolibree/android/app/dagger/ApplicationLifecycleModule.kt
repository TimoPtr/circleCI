/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import com.kolibree.android.utils.lifecycle.ApplicationLifecyclePublisher
import com.kolibree.android.utils.lifecycle.ApplicationLifecyclePublisherImpl
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleUseCase
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleUseCaseImpl
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
object ApplicationLifecycleModule {

    @Provides
    @ProcessLifecycle
    internal fun provideProcessLifecycle(): Lifecycle {
        return ProcessLifecycleOwner.get().lifecycle
    }

    @Provides
    @MainHandler
    internal fun provideMainHandler(appContext: ApplicationContext): Handler {
        return Handler(appContext.mainLooper)
    }

    @Provides
    fun provideLifecycleUseCase(
        @ProcessLifecycle processLifecycle: Lifecycle,
        @MainHandler mainHandler: Handler
    ): ApplicationLifecycleUseCase {
        return ApplicationLifecycleUseCaseImpl(processLifecycle, mainHandler)
    }

    @Provides
    fun provideApplicationLifecyclePublisher(
        @ProcessLifecycle processLifecycle: Lifecycle,
        @MainHandler mainHandler: Handler,
        observers: Set<@JvmSuppressWildcards ApplicationLifecycleObserver>
    ): ApplicationLifecyclePublisher {
        return ApplicationLifecyclePublisherImpl(processLifecycle, mainHandler, observers)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class ProcessLifecycle

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class MainHandler
