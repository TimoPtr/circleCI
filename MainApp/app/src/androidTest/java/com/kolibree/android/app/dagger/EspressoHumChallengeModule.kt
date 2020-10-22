/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.rewards.personalchallenge.logic.HumChallengeUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable

@Module
object EspressoHumChallengeModule {

    val mock: HumChallengeUseCase by lazy {
        mock<HumChallengeUseCase>().also {
            whenever(it.challengeStream()).thenReturn(Flowable.never())
        }
    }

    @Provides
    @AppScope
    internal fun providesHumChallengeUseCase(): HumChallengeUseCase = mock
}
