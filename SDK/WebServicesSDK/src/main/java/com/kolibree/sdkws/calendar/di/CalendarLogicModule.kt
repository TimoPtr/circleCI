/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.di

import com.kolibree.android.calendar.logic.api.BrushingStreaksApi
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCase
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import retrofit2.Retrofit

@Module(includes = [CalendarUseCaseModule::class, CalendarApiModule::class, CalendarRoomModule::class])
abstract class CalendarLogicModule

@Module
internal abstract class CalendarUseCaseModule {

    @Binds
    internal abstract fun bindCalendarBrushingsUseCase(impl: CalendarBrushingsUseCaseImpl): CalendarBrushingsUseCase
}

@Module
internal object CalendarApiModule {

    const val CALCULATE_STREAKS_ON_THE_FLY = "CalendarApiModule.CALCULATE_STREAKS_ON_THE_FLY"

    @Provides
    internal fun providesBrushingStreaksApi(retrofit: Retrofit): BrushingStreaksApi {
        return retrofit.create(BrushingStreaksApi::class.java)
    }

    @Provides
    @Named(CALCULATE_STREAKS_ON_THE_FLY)
    internal fun providesCalculateStreaksOnTheFly() = true // TODO change to false once API is fixed
}
