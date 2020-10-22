/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class CheckupResultsActivityModule {

    @Binds
    internal abstract fun bindAppCompatActivity(activity: CheckupResultsActivity): AppCompatActivity

    internal companion object {
        @Provides
        internal fun provideOrigin(activity: CheckupResultsActivity) =
            activity.extractOrigin()
    }
}
