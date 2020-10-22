/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import android.view.View
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.OffsetDateTime

/** [LastBrushingCardBindingModel] unit tests */
class LastBrushingCardBindingModelTest : BaseUnitTest() {

    /*
    coverage
     */

    @Test
    fun `coverage returns coverage value`() {
        val expectedCoverage = 80f
        val item = item(coverage = expectedCoverage, brushingDate = TrustedClock.getNowOffsetDateTime())
        assertEquals(
            expectedCoverage,
            createInstance().coverage(item)
        )
    }

    @Test
    fun `coverage returns null when item has no data`() {
        assertNull(createInstance().coverage(item()))
    }

    /*
    duration
     */

    @Test
    fun `duration returns duration value`() {
        val expectedDuration = 80f
        val item = item(duration = expectedDuration, brushingDate = TrustedClock.getNowOffsetDateTime())
        assertEquals(
            expectedDuration,
            createInstance().durationPercentage(item)
        )
    }

    @Test
    fun `duration returns null when item has no data`() {
        assertNull(createInstance().durationPercentage(item()))
    }

    /*
    durationSeconds
     */

    @Test
    fun `durationSeconds returns durationInSeconds value`() {
        val expectedDuration = 80L
        val item = item(durationInSeconds = expectedDuration, brushingDate = TrustedClock.getNowOffsetDateTime())
        assertEquals(
            expectedDuration,
            createInstance().durationSeconds(item)
        )
    }

    @Test
    fun `durationSeconds returns null when item has no data`() {
        assertNull(createInstance().durationSeconds(item()))
    }

    /*
    showData
     */

    @Test
    fun `showData returns isBrushingDay`() {
        assertTrue(createInstance().showData(item(brushingDate = TrustedClock.getNowOffsetDateTime())))
        assertFalse(createInstance().showData(item()))
    }

    /*
    deleteButtonVisibility
     */

    @Test
    fun `deleteButtonVisibility returns VISIBLE when item has brushing day`() {
        val item = item(brushingDate = TrustedClock.getNowOffsetDateTime())
        assertEquals(
            View.VISIBLE,
            createInstance().deleteButtonVisibility(item)
        )
    }

    @Test
    fun `deleteButtonVisibility returns GONE when item has no brushing day`() {
        assertEquals(
            View.GONE,
            createInstance().deleteButtonVisibility(item())
        )
    }

    /*
    isOfflineBrushingSyncing
     */
    @Test
    fun `isOfflineBrushingSyncing returns false if offlineBrushingSyncProgress null`() {
        assertFalse(createInstance().data.isOfflineBrushingSyncing)
    }

    @Test
    fun `isOfflineBrushingSyncing returns false if offlineBrushingSyncProgress not null and equals to 1`() {
        assertFalse(createInstance(offlineBrushingSyncProgress = 1.0f).data.isOfflineBrushingSyncing)
    }

    @Test
    fun `isOfflineBrushingSyncing returns true if offlineBrushingSyncProgress not null and below 1`() {
        assertTrue(createInstance(offlineBrushingSyncProgress = 0.1f).data.isOfflineBrushingSyncing)
    }

    /*
    pulsingDot
     */
    @Test
    fun `shows pulsing dot if available`() {
        assertEquals(View.GONE, createInstance(pulsingDotVisible = false).pulsingDotVisibility())
        assertEquals(View.VISIBLE, createInstance(pulsingDotVisible = true).pulsingDotVisibility())
    }

    /*
    Utils
     */

    private fun item(
        coverage: Float = 0f,
        duration: Float = 0f,
        durationInSeconds: Long = 0,
        brushingDate: OffsetDateTime? = null
    ) = BrushingCardData.empty().copy(
        coverage = coverage,
        durationPercentage = duration,
        durationInSeconds = durationInSeconds,
        brushingDate = brushingDate
    )

    private fun createInstance(
        coverage: Int? = null,
        duration: Long? = null,
        offlineBrushingSyncProgress: Float? = null,
        pulsingDotVisible: Boolean = false
    ) = LastBrushingCardBindingModel(
        LastBrushingCardViewState(
            visible = true,
            position = DynamicCardPosition.ONE,
            shouldRender = false,
            items = listOf(),
            selectedItem = BrushingCardData.empty(),
            offlineBrushingSyncProgress = offlineBrushingSyncProgress,
            pulsingDotVisible = pulsingDotVisible
        )
    )
}
