/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy.Companion.END_AFTER_LAST_SEGMENT_BLE_VALUE
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy.Companion.KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy.Companion.SYSTEM_DEFAULT_BLE_VALUE
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeLastSegmentStrategy] unit tests */
class BrushingModeLastSegmentStrategyTest : BaseUnitTest() {

    /*
    bleValue
     */

    @Test
    fun `bleValue of UseSystemDefault is SYSTEM_DEFAULT_BLE_VALUE`() {
        assertEquals(SYSTEM_DEFAULT_BLE_VALUE, BrushingModeLastSegmentStrategy.UseSystemDefault.bleValue)
    }

    @Test
    fun `bleValue of EndAfterLastSegment is END_AFTER_LAST_SEGMENT_BLE_VALUE`() {
        assertEquals(
            END_AFTER_LAST_SEGMENT_BLE_VALUE,
            BrushingModeLastSegmentStrategy.EndAfterLastSegment.bleValue
        )
    }

    @Test
    fun `bleValue of KeepRunningAfterLastSegment is KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE`() {
        assertEquals(
            KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE,
            BrushingModeLastSegmentStrategy.KeepRunningAfterLastSegment.bleValue
        )
    }

    /*
    Constants
     */

    @Test
    fun `value of SYSTEM_DEFAULT_BLE_VALUE is 0`() {
        assertEquals(0, SYSTEM_DEFAULT_BLE_VALUE)
    }

    @Test
    fun `value of END_AFTER_LAST_SEGMENT_BLE_VALUE is 1`() {
        assertEquals(1, END_AFTER_LAST_SEGMENT_BLE_VALUE)
    }

    @Test
    fun `value of KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE is 2`() {
        assertEquals(2, KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE)
    }
}
