/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class BrushingStreakCompletionModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: BrushingStreakCompletionActivity): AppCompatActivity

    companion object {
        @Provides
        fun providesSmiles(activity: BrushingStreakCompletionActivity): Int = activity.readSmiles()
    }
}
