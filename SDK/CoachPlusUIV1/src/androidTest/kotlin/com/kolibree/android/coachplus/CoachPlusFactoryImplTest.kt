/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CoachPlusFactoryImplTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun createConnectedCoach_returns_intent_with_args() {
        val macAddress = "hello"
        val tbModel = ToothbrushModel.ARA
        val colorSet = CoachPlusColorSet(
            backgroundColor = 1,
            titleColor = 2,
            neglectedColor = 3,
            cleanColor = 4,
            plaqueColor = 5,
            plaqlessLedBlue = 6,
            plaqlessLedRed = 7,
            plaqlessLedWhite = 8
        )
        val intent = CoachPlusFactoryImpl()
            .createConnectedCoach(context(), macAddress, tbModel, colorSet)

        assertEquals(macAddress, intent.getStringExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC))
        assertEquals(tbModel, intent.getSerializableExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL))
        assertEquals(colorSet, intent.getParcelableExtra(CoachPlusFactoryImpl.INTENT_COLOR_SET))
        assertFalse(intent.getBooleanExtra(CoachPlusFactoryImpl.EXTRA_MANUAL_MODE, true))
    }

    @Test
    fun createManualCoach_returns_intent_with_args() {
        val colorSet = CoachPlusColorSet(
            backgroundColor = 1,
            titleColor = 2,
            neglectedColor = 3,
            cleanColor = 4,
            plaqueColor = 5,
            plaqlessLedBlue = 6,
            plaqlessLedRed = 7,
            plaqlessLedWhite = 8
        )
        val intent = CoachPlusFactoryImpl().createManualCoach(context(), colorSet)

        assertNull(intent.getStringExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC))
        assertNull(intent.getSerializableExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL))
        assertEquals(colorSet, intent.getParcelableExtra(CoachPlusFactoryImpl.INTENT_COLOR_SET))
        assertTrue(intent.getBooleanExtra(CoachPlusFactoryImpl.EXTRA_MANUAL_MODE, false))
    }
}
