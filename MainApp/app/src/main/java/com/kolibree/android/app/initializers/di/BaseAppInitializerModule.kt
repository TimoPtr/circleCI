/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers.di

import com.kolibree.android.app.initializers.ApplicationLifecyclePublisherInitializer
import com.kolibree.android.app.initializers.AuditorAppInitializer
import com.kolibree.android.app.initializers.BrushReminderAppInitializer
import com.kolibree.android.app.initializers.EventTrackerAppInitializer
import com.kolibree.android.app.initializers.FlipperInitializer
import com.kolibree.android.app.initializers.InterferingAppCheckInitializer
import com.kolibree.android.app.initializers.LoggerAppInitializer
import com.kolibree.android.app.initializers.PresyncAppInitializer
import com.kolibree.android.app.initializers.RxDogAppInitializer
import com.kolibree.android.app.initializers.RxErrorHandlerAppInitializer
import com.kolibree.android.app.initializers.SynchronizatorAppInitializer
import com.kolibree.android.app.initializers.TogglesAppInitializer
import com.kolibree.android.app.initializers.VersionCodeAppInitializer
import com.kolibree.android.app.initializers.WorkManagerAppInitializer
import com.kolibree.android.app.initializers.ZendeskAppInitializer
import com.kolibree.android.app.initializers.base.AppInitializer
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

typealias AppInitializerList = List<@JvmSuppressWildcards AppInitializer>

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseAppInitializers

/**
 * Provides [BaseAppInitializers]
 */
@Module
class BaseAppInitializerModule {

    /**
     * Order of [AppInitializer]s is very important!
     * We always need to specify it explicitly.
     */
    @SuppressWarnings("LongParameterList", "LongMethod")
    @Provides
    @BaseAppInitializers
    internal fun providesAppInitializers(
        togglesAppInitializer: TogglesAppInitializer,
        presyncAppInitializer: PresyncAppInitializer,
        rxDogAppInitializer: RxDogAppInitializer,
        rxErrorHandlerAppInitializer: RxErrorHandlerAppInitializer,
        loggerAppInitializer: LoggerAppInitializer,
        eventTrackerAppInitializer: EventTrackerAppInitializer,
        workManagerAppInitializer: WorkManagerAppInitializer,
        zendeskAppInitializer: ZendeskAppInitializer,
        synchronizatorAppInitializer: SynchronizatorAppInitializer,
        auditorAppInitializer: AuditorAppInitializer,
        brushReminderAppInitializer: BrushReminderAppInitializer,
        applicationLifecyclePublisherInitializer: ApplicationLifecyclePublisherInitializer,
        interferingAppCheckInitializer: InterferingAppCheckInitializer,
        versionCodeAppInitializer: VersionCodeAppInitializer,
        flipperInitializer: FlipperInitializer
    ): AppInitializerList {
        return listOf(
            // Needs to be before `loggerAppInitializer`, otherwise - no logs in Instabugs
            auditorAppInitializer,
            // Logger needs to be right after, because we need to have logs from the beginning
            // We should consider initializing Timber even before Dagger,
            // but it requires some refactoring
            loggerAppInitializer,
            togglesAppInitializer,
            presyncAppInitializer,
            rxDogAppInitializer,
            rxErrorHandlerAppInitializer,
            eventTrackerAppInitializer,
            workManagerAppInitializer,
            zendeskAppInitializer,
            synchronizatorAppInitializer,
            brushReminderAppInitializer,
            applicationLifecyclePublisherInitializer,
            interferingAppCheckInitializer,
            versionCodeAppInitializer,
            flipperInitializer
        )
    }
}
