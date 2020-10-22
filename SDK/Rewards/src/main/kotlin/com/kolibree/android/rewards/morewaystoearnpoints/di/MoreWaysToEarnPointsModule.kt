/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.morewaystoearnpoints.di

import com.kolibree.android.rewards.morewaystoearnpoints.logic.MoreWaysToGetPointsCardUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.logic.MoreWaysToGetPointsCardUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
abstract class MoreWaysToEarnPointsModule {

    @Binds
    internal abstract fun bindMoreWaysToGetPointsCardUseCase(
        impl: MoreWaysToGetPointsCardUseCaseImpl
    ): MoreWaysToGetPointsCardUseCase
}
