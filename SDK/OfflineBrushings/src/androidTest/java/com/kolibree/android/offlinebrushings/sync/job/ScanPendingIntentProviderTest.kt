/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanPendingIntentProviderTest : BaseInstrumentationTest() {

    private val scanPendingIntentProvider = ScanPendingIntentProvider(context())

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun provide_creates_expected_pending_intent_with_expected_action() {
        val expectedClass = ToothbrushScannedBroadcastReceiver::class.java
        val expectedCode = TOOTHBRUSH_FOUND_REQUEST_CODE
        val expectedFlags = PendingIntent.FLAG_UPDATE_CURRENT

        val expectedIntent = Intent(context(), expectedClass)
        expectedIntent.action = "com.kolibree.android.offlinebrushings.TOOTHBRUSH_FOUND"

        val expectedPendingIntent = PendingIntent.getBroadcast(
            context(),
            expectedCode,
            expectedIntent,
            expectedFlags
        )

        assertEquals(expectedPendingIntent, scanPendingIntentProvider.provide())
    }
}
