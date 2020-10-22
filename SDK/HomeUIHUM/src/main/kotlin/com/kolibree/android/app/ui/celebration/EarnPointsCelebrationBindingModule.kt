/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Keep
@Module
abstract class EarnPointsCelebrationBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [EarnPointsCelebrationModule::class])
    internal abstract fun bindCelebrationActivity(): EarnPointsCelebrationActivity
}
