/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.di

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.tracker.EventTracker
import com.kolibree.android.tracker.logic.KolibreeEventTracker
import com.kolibree.android.tracker.studies.StudiesForProfileUseCase
import com.kolibree.android.tracker.studies.StudiesForProfileUseCaseImpl
import com.kolibree.android.tracker.studies.StudiesRepository
import com.kolibree.android.tracker.studies.StudiesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Created by Kornel on 3/14/2018.  */
@Module(includes = [EventTrackerBindModule::class, EventTrackerInternalModule::class])
object EventTrackerModule

@Module
internal abstract class EventTrackerBindModule {

    @Binds
    abstract fun providesEventTracker(tracker: KolibreeEventTracker): EventTracker

    @Binds
    abstract fun bindStudiesRepository(impl: StudiesRepositoryImpl): StudiesRepository

    @Binds
    abstract fun bindProfileStudiesManager(impl: StudiesForProfileUseCaseImpl): StudiesForProfileUseCase
}

@Module
internal object EventTrackerInternalModule {

    @Provides
    @AppScope
    fun providesEventExecutorService(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
}
