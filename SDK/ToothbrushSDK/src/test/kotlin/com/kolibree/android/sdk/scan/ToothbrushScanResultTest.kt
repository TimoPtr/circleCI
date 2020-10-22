/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.scan.ToothbrushApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.test.FakeToothbrushScanResult
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToothbrushScanResultTest : BaseUnitTest() {
    @Test
    fun `when ToothbrushApp is DFU_BOOTLOADER, isDfu returns true`() {
        assertTrue(FakeToothbrushScanResult(toothbrushApp = DFU_BOOTLOADER).isDfu())
    }

    @Test
    fun `when ToothbrushApp is not DFU_BOOTLOADER, isDfu returns false`() {
        ToothbrushApp.values()
            .filterNot { it == DFU_BOOTLOADER }
            .forEach { app ->
                assertFalse(FakeToothbrushScanResult(toothbrushApp = app).isDfu())
            }
    }
}
