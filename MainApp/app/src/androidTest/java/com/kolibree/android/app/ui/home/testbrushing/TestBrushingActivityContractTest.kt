/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing

import android.app.Activity
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class TestBrushingActivityContractTest : BaseInstrumentationTest() {
    override fun context(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun result_match_result_of_activity_result() {
        val contract = TestBrushingActivityContract()

        assertTrue(contract.parseResult(Activity.RESULT_OK, null))
        assertFalse(contract.parseResult(Activity.RESULT_CANCELED, null))
    }
}
