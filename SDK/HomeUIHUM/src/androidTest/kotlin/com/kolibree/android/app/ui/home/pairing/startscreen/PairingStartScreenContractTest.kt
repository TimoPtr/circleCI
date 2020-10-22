/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Canceled
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.OpenShop
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Success
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PairingStartScreenContractTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val startScreenContract = PairingStartScreenContract()

    @Test
    fun parseResult_returnsCanceled_whenResultIsNotOk() {
        assertEquals(
            Canceled,
            startScreenContract.parseResult(Activity.RESULT_CANCELED, null)
        )

        assertEquals(
            Canceled,
            startScreenContract.parseResult(1234, null)
        )
    }

    @Test
    fun parseResult_returnsOpenShop_whenResultOkAndIntentContainShopExtra() {
        assertEquals(
            OpenShop,
            startScreenContract.parseResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra(EXTRA_RESULT_SHOP, true) })
        )
    }

    @Test
    fun parseResult_returnsSuccess_whenResultOkAndIntentDoesNotContainShopExtra() {
        assertEquals(
            Success,
            startScreenContract.parseResult(Activity.RESULT_OK, null)
        )

        assertEquals(
            Success,
            startScreenContract.parseResult(Activity.RESULT_OK, Intent())
        )

        assertEquals(
            Success,
            startScreenContract.parseResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra(EXTRA_RESULT_SHOP, false) })
        )

        assertEquals(
            Success,
            startScreenContract.parseResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra("Random", true) })
        )
    }
}
