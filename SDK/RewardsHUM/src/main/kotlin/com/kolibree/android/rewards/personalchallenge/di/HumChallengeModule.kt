/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.di

import com.kolibree.android.rewards.personalchallenge.logic.HumChallengeUseCase
import com.kolibree.android.rewards.personalchallenge.logic.HumChallengeUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
abstract class HumChallengeModule {

    @Binds
    internal abstract fun bindsHumChallengeUseCase(useCaseImpl: HumChallengeUseCaseImpl): HumChallengeUseCase
}
