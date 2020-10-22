/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.domain

import com.kolibree.android.app.test.BaseUnitTest
import java.lang.IllegalArgumentException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HeadspaceInProgressStatusTest : BaseUnitTest() {

    @Test(expected = IllegalArgumentException::class)
    fun `threshold cannot be negative value`() {
        HeadspacePartnershipStatus.InProgress(1, pointsNeeded = 100, pointsThreshold = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `points needed cannot be negative value`() {
        HeadspacePartnershipStatus.InProgress(1, pointsNeeded = -1, pointsThreshold = 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `points needed cannot be greater than threshold`() {
        HeadspacePartnershipStatus.InProgress(1, pointsNeeded = 101, pointsThreshold = 100)
    }

    @Test
    fun `progress maps points into 0-100 range`() {
        assertEquals(0, HeadspacePartnershipStatus.InProgress(1, 50, 50).progress)
        assertEquals(50, HeadspacePartnershipStatus.InProgress(1, 25, 50).progress)
        assertEquals(100, HeadspacePartnershipStatus.InProgress(1, 0, 50).progress)
    }

    @Test
    fun `challenge is ready for unlock if we don't have any points needed`() {
        assertFalse(HeadspacePartnershipStatus.InProgress(1, 50, 50).readyToBeUnlocked)
        assertFalse(HeadspacePartnershipStatus.InProgress(1, 25, 50).readyToBeUnlocked)
        assertFalse(HeadspacePartnershipStatus.InProgress(1, 1, 50).readyToBeUnlocked)
        assertTrue(HeadspacePartnershipStatus.InProgress(1, 0, 50).readyToBeUnlocked)
    }
}
