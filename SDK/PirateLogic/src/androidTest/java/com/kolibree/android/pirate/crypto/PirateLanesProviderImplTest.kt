/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.crypto

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.crypto.KolibreeGuard
import junit.framework.TestCase
import org.junit.Test

class PirateLanesProviderImplTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pirateLanes_isValidJsonObject() {
        val jsonParser = JsonParser()
        val pirateLanesProvider = PirateLanesProviderImpl(
            context(),
            KolibreeGuard.createInstance()
        )

        val json = jsonParser.parse(pirateLanesProvider.getPirateLanes())

        TestCase.assertTrue(json.isJsonObject)
    }
}
