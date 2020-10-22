/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.di

import android.os.Handler
import com.kolibree.android.mouthmap.widget.timer.MouthMapTimer
import com.kolibree.android.mouthmap.widget.timer.MouthMapTimerImpl
import com.kolibree.android.mouthmap.widget.timer.RealtimeProvider
import com.kolibree.android.mouthmap.widget.timer.RealtimeProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module(includes = [MouthMapTimerBindsModule::class])
class MouthMapTimerModule

@Module
internal abstract class MouthMapTimerBindsModule {

    @Binds
    abstract fun bindsRealtimeProvider(implementation: RealtimeProviderImpl): RealtimeProvider

    @Binds
    abstract fun bindsCheckupTimer(implementation: MouthMapTimerImpl): MouthMapTimer

    companion object {

        const val MOUTH_MAP_TIMER_HANDLER = "MOUTH_MAP_TIMER_HANDLER"

        @Provides
        @Named(MOUTH_MAP_TIMER_HANDLER)
        fun providesHandler(): Handler {
            return Handler()
        }
    }
}
