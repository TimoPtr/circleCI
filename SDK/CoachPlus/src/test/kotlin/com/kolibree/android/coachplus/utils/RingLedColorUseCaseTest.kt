/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mock

class RingLedColorUseCaseTest : BaseUnitTest() {

    @Mock
    lateinit var colorMapper: CoachPlaqlessRingLedColorMapper

    @Test
    fun `getRingLedColor with non PLAQLESS Toothbrush returns empty flowable`() {
        ToothbrushModel.values().filter { !it.isPlaqless }.forEach { model ->
            val connection = KLTBConnectionBuilder.createAndroidLess()
                .withModel(model)
                .build()

            val ringLedColorUseCase = RingLedColorUseCaseImpl(colorMapper)

            ringLedColorUseCase.getRingLedColor(connection).test().assertEmpty()
        }
    }

    @Test
    fun `getRingLedColor with PLAQLESS Toothbrush return color from PlaqlessRingLedState`() {
        val expectedState = PlaqlessRingLedState(0, 0, 0, 0)
        val expectedColor = 0
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(ToothbrushModel.PLAQLESS)
            .withPlaqlessRingLedState(expectedState)
            .build()

        whenever(colorMapper.getRingLedColor(eq(expectedState))).thenReturn(expectedColor)

        val ringLedColorUseCase = RingLedColorUseCaseImpl(colorMapper)

        ringLedColorUseCase.getRingLedColor(connection).test().assertValue(expectedColor)
    }
}
