/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import com.kolibree.android.processedbrushings.crypto.ThresholdProviderImpl
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.crypto.KolibreeGuard
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ThresholdProviderImplTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun getThresholdBalancing_isValidJsonObject() {
        val jsonParser = JsonParser()
        val transitionProvider = ThresholdProviderImpl(
            context(),
            KolibreeGuard.createInstance()
        )

        val json = jsonParser.parse(transitionProvider.getThresholdBalancing())

        assertTrue(json.isJsonObject)
    }
}
