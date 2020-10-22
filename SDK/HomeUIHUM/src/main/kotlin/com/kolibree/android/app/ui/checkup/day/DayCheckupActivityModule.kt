/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import dagger.Module
import dagger.Provides

@Module
object DayCheckupActivityModule {

    @Provides
    internal fun provideDay(activity: DayCheckupActivity) = activity.forDateFromIntent()
}
