/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.di

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsActivity
import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Keep
@Module
abstract class ToothbrushSettingsBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ToothbrushSettingsModule::class])
    internal abstract fun bindToothbrushSettingsActivity(): ToothbrushSettingsActivity
}
