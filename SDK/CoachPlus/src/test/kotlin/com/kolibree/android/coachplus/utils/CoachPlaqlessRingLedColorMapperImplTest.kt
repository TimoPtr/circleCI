/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.ColorInt
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** [CoachPlaqlessRingLedColorMapperImpl] tests */
class CoachPlaqlessRingLedColorMapperImplTest : BaseUnitTest() {

    @ColorInt
    private val plaqlessRedColor = 1

    @ColorInt
    private val plaqlessBlueColor = 2

    @ColorInt
    private val plaqlessWhiteColor = 3

    private val colorSet = mock<CoachPlusColorSet>()

    private lateinit var mapper: CoachPlaqlessRingLedColorMapperImpl

    @Before
    fun before() {
        whenever(colorSet.plaqlessLedRed).thenReturn(plaqlessRedColor)
        whenever(colorSet.plaqlessLedBlue).thenReturn(plaqlessBlueColor)
        whenever(colorSet.plaqlessLedWhite).thenReturn(plaqlessWhiteColor)
        mapper = CoachPlaqlessRingLedColorMapperImpl(colorSet)
    }

    /*
    getRingLedColor
     */

    @Test
    fun `getRingLedColor returns plaqlessLedRed when red component is different than 0`() {
        assertEquals(
            plaqlessRedColor,
            mapper.getRingLedColor(
                PlaqlessRingLedState(
                    red = 22,
                    blue = 45,
                    green = 9,
                    white = 9
                )
            )
        )
    }

    @Test
    fun `getRingLedColor returns plaqlessLedBlue when blue component is different than 0`() {
        assertEquals(
            plaqlessBlueColor,
            mapper.getRingLedColor(
                PlaqlessRingLedState(
                    red = 0,
                    blue = 45,
                    green = 9,
                    white = 9
                )
            )
        )
    }

    @Test
    fun `getRingLedColor returns plaqlessLedWhite when white component is different than 0`() {
        assertEquals(
            plaqlessWhiteColor,
            mapper.getRingLedColor(
                PlaqlessRingLedState(
                    red = 0,
                    blue = 0,
                    green = 9,
                    white = 9
                )
            )
        )
    }

    @Test
    fun `getRingLedColor returns plaqlessLedWhite when the Plaqless toothbrush is buggy`() {
        assertEquals(
            plaqlessWhiteColor,
            mapper.getRingLedColor(
                PlaqlessRingLedState(
                    red = 0,
                    blue = 0,
                    green = 0,
                    white = 0
                )
            )
        )
    }
}
