/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GuidedBrushingStartScreenBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [GuidedBrushingStartScreenModule::class])
    internal abstract fun bindGuidedBrushingStartScreenActivity(): GuidedBrushingStartScreenActivity
}
