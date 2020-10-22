/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCase
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
class EspressoCalendarLogicModule {

    @Provides
    @AppScope
    fun provideCalendarBrushingsUseCase(): CalendarBrushingsUseCase = mock()
}
