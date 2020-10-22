/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked
import com.kolibree.android.test.utils.randomPositiveInt
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HeadspaceTrialCardViewStateTest : BaseUnitTest() {
    @Test
    fun `initialState sets expected default values`() {
        val viewState = initialViewState()

        assertFalse(viewState.visible)
        assertFalse(viewState.isDescriptionVisible)
        assertFalse(viewState.isUnlocked)
        assertNull(viewState.redeemUrl)
        assertNull(viewState.discountCode)
    }

    @Test
    fun `withInactiveStatus sets visibility to false`() {
        val viewState = initialViewState()
            .withInactiveStatus(Inactive(profileId = 11L))

        assertFalse(viewState.visible)
    }

    @Test
    fun `withInProgressStatus updates expected fields`() {
        val expectedPointsNeeded = 65
        val inProgressStatus = HeadspacePartnershipStatus.InProgress(
            pointsNeeded = expectedPointsNeeded,
            pointsThreshold = 200,
            profileId = 11L
        )

        val viewState = initialViewState()
            .copy(visible = false, isUnlocked = true)
            .withInProgressStatus(inProgressStatus)

        assertFalse(viewState.isDescriptionVisible)
        assertNull(viewState.redeemUrl)
        assertNull(viewState.discountCode)

        assertTrue(viewState.visible)
        assertFalse(viewState.isUnlocked)
        assertEquals(expectedPointsNeeded, viewState.pointsNeeded)
        assertEquals(inProgressStatus.progress, viewState.progress)
    }

    @Test
    fun `withUnlockedStatus updates expected fields`() {
        val expectedCode = "dasdas"
        val expectedRedeemUrl = "randoom"
        val unlockedStatus = Unlocked(
            profileId = 11L,
            discountCode = expectedCode,
            redeemUrl = expectedRedeemUrl
        )

        val viewState = initialViewState()
            .copy(visible = false)
            .withUnlockedStatus(unlockedStatus)

        assertFalse(viewState.isDescriptionVisible)

        assertTrue(viewState.visible)
        assertTrue(viewState.isUnlocked)
        assertEquals(0, viewState.pointsNeeded)
        assertEquals(expectedCode, viewState.discountCode)
        assertEquals(expectedRedeemUrl, viewState.redeemUrl)
    }

    /*
    isProgressVisible
     */
    @Test
    fun `when isUnlocked is true, isProgressVisible returns false independently of pointsNeeded`() {
        arrayOf(0, randomPositiveInt(minValue = 1)).forEach { pointsNeeded ->
            val viewState = initialViewState()
                .copy(pointsNeeded = pointsNeeded, isUnlocked = true)

            assertFalse(viewState.isProgressVisible)
        }
    }

    @Test
    fun `when isUnlocked is false and pointsNeeded equals 0, isProgressVisible returns false`() {
        val viewState = initialViewState()
            .copy(pointsNeeded = 0, isUnlocked = false)

        assertFalse(viewState.isProgressVisible)
    }

    @Test
    fun `when isUnlocked is false and pointsNeeded is greater than 0, isProgressVisible returns true`() {
        val viewState = initialViewState()
            .copy(pointsNeeded = randomPositiveInt(minValue = 1), isUnlocked = false)

        assertTrue(viewState.isProgressVisible)
    }

    /*
    isUnlockable
     */

    @Test
    fun `when pointsNeeded is 0 and unlocked is false, isUnlockable returns true`() {
        val viewState = initialViewState()
            .copy(pointsNeeded = 0, isUnlocked = false)

        assertTrue(viewState.isUnlockable)
    }

    @Test
    fun `when pointsNeeded is greather than 0 and isUnlocked is false, isUnlockable returns false`() {
        val viewState = initialViewState()
            .copy(pointsNeeded = randomPositiveInt(minValue = 1), isUnlocked = false)

        assertFalse(viewState.isUnlockable)
    }

    @Test
    fun `when isUnlocked is true, isUnlockable returns false independently of pointsNeeded`() {
        arrayOf(0, randomPositiveInt(minValue = 1)).forEach { pointsNeeded ->
            val viewState = initialViewState()
                .copy(pointsNeeded = pointsNeeded, isUnlocked = true)

            assertFalse(viewState.isUnlockable)
        }
    }

    private fun initialViewState(): HeadspaceTrialCardViewState {
        return HeadspaceTrialCardViewState.initial(
            position = DynamicCardPosition.EIGHT
        )
    }
}
