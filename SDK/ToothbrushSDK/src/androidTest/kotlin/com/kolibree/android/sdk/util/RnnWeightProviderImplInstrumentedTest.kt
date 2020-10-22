/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.kml.Kml
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RnnWeightProviderImplInstrumentedTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var kolibreeGuard: KolibreeGuard

    @Before
    override fun setUp() {
        super.setUp()
        Kml.init()
        kolibreeGuard = KolibreeGuard.createInstance()
    }

    @Test
    fun reveal_ce1_weight_no_crash() {
        assertWeight(ToothbrushModel.CONNECT_E1)
    }

    @Test
    fun reveal_cm1_weight_no_crash() {
        assertWeight(ToothbrushModel.CONNECT_M1)
    }

    @Test
    fun reveal_ce2_weight_no_crash() {
        assertWeight(ToothbrushModel.CONNECT_E2)
    }

    private fun assertWeight(toothbrushModel: ToothbrushModel) {
        val rnnWeightProvider = RnnWeightProviderImpl(context(), kolibreeGuard, toothbrushModel)
        val result = rnnWeightProvider.getRnnWeight()
        assertTrue(result.isNotEmpty())
    }
}
