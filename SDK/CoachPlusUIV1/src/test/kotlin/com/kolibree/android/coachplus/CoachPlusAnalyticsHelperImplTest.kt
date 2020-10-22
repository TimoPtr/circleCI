/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.EVENT_OPTION_1_CHOSEN
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.EVENT_OPTION_2_CHOSEN
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.EVENT_OPTION_3_CHOSEN
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.EVENT_OPTION_4_CHOSEN
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.EVENT_OPTION_5_CHOSEN
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl.Companion.SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** [CoachPlusAnalyticsHelperImpl] tests */
class CoachPlusAnalyticsHelperImplTest : BaseUnitTest() {

    private lateinit var analyticsHelper: CoachPlusAnalyticsHelperImpl

    @Before
    fun before() {
        analyticsHelper = CoachPlusAnalyticsHelperImpl(eventTracker)
    }

    /*
    onBrushingModeDialogOptionChosen
     */

    @Test
    fun `onBrushingModeDialogOptionChosen option index 0 sends EVENT_OPTION_1_CHOSEN event`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(0)
        verify(eventTracker).sendEvent(EVENT_OPTION_1_CHOSEN)
    }

    @Test
    fun `onBrushingModeDialogOptionChosen option index 1 sends EVENT_OPTION_2_CHOSEN event`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(1)
        verify(eventTracker).sendEvent(EVENT_OPTION_2_CHOSEN)
    }

    @Test
    fun `onBrushingModeDialogOptionChosen option index 2 sends EVENT_OPTION_3_CHOSEN event`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(2)
        verify(eventTracker).sendEvent(EVENT_OPTION_3_CHOSEN)
    }

    @Test
    fun `onBrushingModeDialogOptionChosen option index 3 sends EVENT_OPTION_4_CHOSEN event`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(3)
        verify(eventTracker).sendEvent(EVENT_OPTION_4_CHOSEN)
    }

    @Test
    fun `onBrushingModeDialogOptionChosen option index 4 sends EVENT_OPTION_5_CHOSEN event`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(4)
        verify(eventTracker).sendEvent(EVENT_OPTION_5_CHOSEN)
    }

    @Test(expected = AssertionError::class)
    fun `onBrushingModeDialogOptionChosen invalid option index throws AssertionError`() {
        analyticsHelper.onBrushingModeDialogOptionChosen(99)
    }

    /*
    onBrushingModeDialogShown
     */

    @Test
    fun `onBrushingModeDialogShown sends SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG event`() {
        analyticsHelper.onBrushingModeDialogShown()
        verify(eventTracker).sendEvent(SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG)
    }

    /*
    Companion
     */

    @Test
    fun `value of SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG is BrushingProgram_CoachPlus`() {
        assertEquals("BrushingProgram_CoachPlus", SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG.name)
    }

    @Test
    fun `value of EVENT_OPTION_1_CHOSEN is BrushingProgram_CoachPlus_Option1`() {
        assertEquals("BrushingProgram_CoachPlus_Option1", EVENT_OPTION_1_CHOSEN.name)
    }

    @Test
    fun `value of EVENT_OPTION_2_CHOSEN is BrushingProgram_CoachPlus_Option2`() {
        assertEquals("BrushingProgram_CoachPlus_Option2", EVENT_OPTION_2_CHOSEN.name)
    }

    @Test
    fun `value of EVENT_OPTION_3_CHOSEN is BrushingProgram_CoachPlus_Option3`() {
        assertEquals("BrushingProgram_CoachPlus_Option3", EVENT_OPTION_3_CHOSEN.name)
    }

    @Test
    fun `value of EVENT_OPTION_4_CHOSEN is BrushingProgram_CoachPlus_Option4`() {
        assertEquals("BrushingProgram_CoachPlus_Option4", EVENT_OPTION_4_CHOSEN.name)
    }

    @Test
    fun `value of EVENT_OPTION_5_CHOSEN is BrushingProgram_CoachPlus_Option5`() {
        assertEquals("BrushingProgram_CoachPlus_Option5", EVENT_OPTION_5_CHOSEN.name)
    }
}
