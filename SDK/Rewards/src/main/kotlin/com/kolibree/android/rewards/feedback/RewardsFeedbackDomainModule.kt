/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import dagger.Binds
import dagger.Module

@Module
abstract class RewardsFeedbackDomainModule {

    @Binds
    internal abstract fun bindsRewardsFeedback(rewardsFeedbackImpl: RewardsFeedbackImpl): RewardsFeedback
}
