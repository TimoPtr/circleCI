/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.checkup.day.DayCheckupActivity
import com.kolibree.android.app.ui.checkup.day.DayCheckupActivityModule
import com.kolibree.android.app.ui.checkup.results.CheckupResultsActivity
import com.kolibree.android.app.ui.checkup.results.CheckupResultsActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CheckupModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [CheckupResultsActivityModule::class])
    internal abstract fun contributeCheckupHumActivity(): CheckupResultsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DayCheckupActivityModule::class])
    internal abstract fun contributeDayCheckupActivity(): DayCheckupActivity
}
