/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui.colors

import android.animation.ArgbEvaluator
import android.graphics.Color
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class CurrentZoneColorProviderImplTest : BaseUnitTest() {

    private val colorSet = mock<CoachPlusColorSet>()

    private val argbEvaluator = mock<ArgbEvaluator>()

    private lateinit var colorProvider: CurrentZoneColorProviderImpl

    @Before
    fun before() {
        whenever(colorSet.cleanColor).thenReturn(Color.WHITE)
        whenever(argbEvaluator.evaluate(any(), any(), any())).thenReturn(Color.RED)
        colorProvider = spy(
            CurrentZoneColorProviderImpl(
                colorSet,
                argbEvaluator
            )
        )
    }

    @Test
    fun `provideCurrentZoneColor evaluates color with completionPercent and neglectedColor`() {
        val completionPercent = 50

        val neglectedColor = Color.CYAN
        whenever(colorSet.neglectedColor).thenReturn(neglectedColor)

        colorProvider.provideCurrentZoneColor(completionPercent)

        verify(colorProvider).evaluateColor(completionPercent, neglectedColor)
    }
}
