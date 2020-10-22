/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import android.app.Activity
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class GuidedBrushingStartScreenContractTest : BaseInstrumentationTest() {

    override fun context(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun creating_an_intent_with_param_will_result_on_a_result_with_the_given_params() {
        val contract = GuidedBrushingStartScreenContract()
        val params =
            GuidedBrushingStartScreenParams(
                false,
                "hello",
                ToothbrushModel.ARA
            )
        val intent = contract.createIntent(context(), params)

        assertEquals(params, contract.parseResult(Activity.RESULT_OK, intent).param)
        assertEquals(params, contract.parseResult(Activity.RESULT_CANCELED, intent).param)
    }

    @Test
    fun when_no_result_is_given_it_returns_null_params() {
        val contract = GuidedBrushingStartScreenContract()

        assertNull(contract.parseResult(Activity.RESULT_OK, null).param)
        assertNull(contract.parseResult(Activity.RESULT_CANCELED, null).param)
    }

    @Test
    fun shouldProceed_match_result_of_activity_result() {
        val contract = GuidedBrushingStartScreenContract()

        assertTrue(contract.parseResult(Activity.RESULT_OK, null).shouldProceed)
        assertFalse(contract.parseResult(Activity.RESULT_CANCELED, null).shouldProceed)
    }
}
