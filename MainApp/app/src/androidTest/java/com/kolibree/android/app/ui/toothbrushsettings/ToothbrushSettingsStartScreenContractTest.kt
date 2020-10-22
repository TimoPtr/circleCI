/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.app.Activity
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ToothbrushSettingsStartScreenContractTest : BaseInstrumentationTest() {
    override fun context(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun shouldProceed_match_result_of_activity_result() {
        val contract = ToothbrushSettingsStartScreenContract()

        assertEquals(ToothbrushSettingsScreenResult.OpenShop, contract.parseResult(Activity.RESULT_OK, null))
        assertEquals(ToothbrushSettingsScreenResult.Canceled, contract.parseResult(Activity.RESULT_CANCELED, null))
    }
}
