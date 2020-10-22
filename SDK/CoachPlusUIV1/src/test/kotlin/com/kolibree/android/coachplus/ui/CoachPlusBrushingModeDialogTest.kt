/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CoachPlusBrushingModeDialog] tests */
class CoachPlusBrushingModeDialogTest : BaseUnitTest() {

    /*
    getOptionLabel
     */

    @Test
    fun `getOptionLabel returns brushing_program_dialog_regular for Regular`() {
        assertEquals(
            R.string.brushing_program_dialog_regular,
            CoachPlusBrushingModeDialog.getOptionLabel(BrushingMode.Regular)
        )
    }

    @Test
    fun `getOptionLabel returns brushing_program_dialog_slow for Slow`() {
        assertEquals(
            R.string.brushing_program_dialog_slow,
            CoachPlusBrushingModeDialog.getOptionLabel(BrushingMode.Slow)
        )
    }

    @Test
    fun `getOptionLabel returns brushing_program_dialog_strong for Strong`() {
        assertEquals(
            R.string.brushing_program_dialog_strong,
            CoachPlusBrushingModeDialog.getOptionLabel(BrushingMode.Strong)
        )
    }

    @Test
    fun `getOptionLabel returns brushing_program_dialog_polishing for Polishing`() {
        assertEquals(
            R.string.brushing_program_dialog_polishing,
            CoachPlusBrushingModeDialog.getOptionLabel(BrushingMode.Polishing)
        )
    }

    @Test
    fun `getOptionLabel returns brushing_program_dialog_custom for UserDefined`() {
        assertEquals(
            R.string.brushing_program_dialog_custom,
            CoachPlusBrushingModeDialog.getOptionLabel(BrushingMode.UserDefined)
        )
    }
}
