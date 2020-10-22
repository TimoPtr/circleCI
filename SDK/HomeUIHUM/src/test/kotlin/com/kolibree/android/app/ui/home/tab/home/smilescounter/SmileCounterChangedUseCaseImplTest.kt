/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Idle
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.test.extensions.assertLastValue
import org.junit.Test

class SmileCounterChangedUseCaseImplTest : BaseUnitTest() {

    private val useCase = SmileCounterChangedUseCaseImpl()

    @Test
    fun `when onSmileCounterChanged is called the value is dispatched to the Observable`() {
        val testCounterObservable = useCase.counterStateObservable.test()
        val idleState = Idle(10)
        val playIncreaseState = PlayIncrease(1, 2)
        val pendingState = Pending

        useCase.onSmileCounterChanged(pendingState)
        testCounterObservable.assertLastValue(pendingState)

        useCase.onSmileCounterChanged(idleState)
        testCounterObservable.assertLastValue(idleState)

        useCase.onSmileCounterChanged(playIncreaseState)
        testCounterObservable.assertLastValue(playIncreaseState)

        testCounterObservable.assertValueSequenceOnly(
            listOf(pendingState, idleState, playIncreaseState)
        )
    }
}
