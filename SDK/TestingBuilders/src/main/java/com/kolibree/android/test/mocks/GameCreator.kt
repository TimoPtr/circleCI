/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable

@Keep
fun mockFacadeWithLifecycleSupport(): GameToothbrushInteractorFacade {
    return mock<GameToothbrushInteractorFacade>().apply {
        whenever(gameLifeCycleObservable()).thenReturn(Observable.empty())
    }
}
