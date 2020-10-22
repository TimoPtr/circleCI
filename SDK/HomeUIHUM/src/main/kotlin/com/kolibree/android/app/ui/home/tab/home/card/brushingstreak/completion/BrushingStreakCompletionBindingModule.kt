/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Keep
@Module
abstract class BrushingStreakCompletionBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [BrushingStreakCompletionModule::class])
    internal abstract fun bindBrushingStreakCompletionActivity(): BrushingStreakCompletionActivity
}
