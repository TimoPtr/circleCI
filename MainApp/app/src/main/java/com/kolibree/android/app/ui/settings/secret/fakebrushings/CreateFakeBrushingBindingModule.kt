/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class CreateFakeBrushingBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [CreateFakeBrushingModule::class])
    internal abstract fun bindCreateFakeBrushingActivity(): CreateFakeBrushingActivity
}
