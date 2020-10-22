/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.usecase

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.Companion.fromUsedPercentage
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.GETTING_OLDER
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.GOOD
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.NEEDS_REPLACEMENT
import com.kolibree.android.test.utils.randomPositiveInt
import junit.framework.TestCase.assertEquals
import org.junit.Test

class BrushHeadConditionTest : BaseUnitTest() {

    @Test
    fun `when percentage is between than 50% and 100%, then condition is GOOD`() {
        repeat(5) {
            val percentage = randomPositiveInt(minValue = 50, maxValue = 100)

            assertEquals(
                "Failed for $percentage",
                GOOD,
                fromUsedPercentage(percentage)
            )
        }

        assertEquals(
            GOOD,
            fromUsedPercentage(100)
        )

        assertEquals(
            GOOD,
            fromUsedPercentage(50)
        )
    }

    @Test
    fun `when percentage is between 1% and 49% then condition is GETTING_OLDER`() {
        repeat(5) {
            val percentage = randomPositiveInt(minValue = 1, maxValue = 49)

            assertEquals(
                "Failed for $percentage",
                GETTING_OLDER,
                fromUsedPercentage(percentage)
            )
        }

        assertEquals(
            GETTING_OLDER,
            fromUsedPercentage(1)
        )

        assertEquals(
            GETTING_OLDER,
            fromUsedPercentage(49)
        )
    }

    @Test
    fun `when percentage is equal or less than 0% then condition is NEEDS_REPLACEMENT`() {
        assertEquals(
            NEEDS_REPLACEMENT,
            fromUsedPercentage(0)
        )
    }
}
