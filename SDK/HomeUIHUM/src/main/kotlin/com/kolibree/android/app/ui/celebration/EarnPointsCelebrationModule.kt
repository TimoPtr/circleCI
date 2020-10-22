/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class EarnPointsCelebrationModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: EarnPointsCelebrationActivity): AppCompatActivity

    @Binds
    abstract fun bindResourceProvider(
        impl: EarnPointsCelebrationResourceProviderImpl
    ): EarnPointsCelebrationResourceProvider

    internal companion object {

        @Provides
        fun providesNavigator(activity: EarnPointsCelebrationActivity): EarnPointsCelebrationNavigator {
            return activity.createNavigatorAndBindToLifecycle(EarnPointsCelebrationNavigator::class)
        }

        @Provides
        fun providesChallenges(activity: EarnPointsCelebrationActivity): List<CompleteEarnPointsChallenge> {
            return activity.extractChallenges()
        }
    }
}
