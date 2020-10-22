/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.BrushingSessionHistoryEventStatus
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.COMPLETED
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.Companion.fromBrushingSessionHistoryStatus
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.DAILY_LIMIT_REACH
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.INCOMPLETE
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class SmilesHistoryItemTest : BaseUnitTest() {

    @Test
    fun `fromBrushingSessionHistoryStatus maps properly COMPLETE`() {
        assertEquals(
            COMPLETED,
            fromBrushingSessionHistoryStatus(BrushingSessionHistoryEventStatus.COMPLETED)
        )
    }

    @Test
    fun `fromBrushingSessionHistoryStatus maps properly INCOMPLETE`() {
        assertEquals(
            INCOMPLETE,
            fromBrushingSessionHistoryStatus(BrushingSessionHistoryEventStatus.INCOMPLETE)
        )
    }

    @Test
    fun `fromBrushingSessionHistoryStatus maps properly DAILY_LIMIT_REACH`() {
        assertEquals(
            DAILY_LIMIT_REACH,
            fromBrushingSessionHistoryStatus(BrushingSessionHistoryEventStatus.DAILY_LIMIT_REACH)
        )
    }
}
