/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.ExtractionProgress.Companion.withBrushingProgress
import com.kolibree.android.test.utils.randomInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractionProgressTest : BaseUnitTest() {

    @Test
    fun `empty viewstate contains an empty list, 0 total brushings and both isFinish and isSuccess return false`() {
        val viewState = ExtractionProgress.empty()

        assertTrue(viewState.brushingsSynced.isEmpty())
        assertFalse(viewState.isSuccess)
        assertFalse(viewState.isFinished)
        assertEquals(0, viewState.totalBrushings)
    }

    @Test
    fun `empty viewstate flagged as completed flags extraction as finished but leaves the rest of the fields untouched`() {
        val viewState = ExtractionProgress.empty().withCompleted()

        assertTrue(viewState.brushingsSynced.isEmpty())
        assertFalse(viewState.isSuccess)
        assertEquals(0, viewState.totalBrushings)
        assertTrue(viewState.isFinished)
    }

    @Test
    fun `when withCompleted is invoked and syncedBrushings is 0, extraction progress is finished and not successful`() {
        (1 until 10).map { randomInt() }
            .filter { it > 0 }
            .forEach { totalBrushings ->
                val progress = withBrushingProgress(emptyList(), totalBrushings)
                    .withCompleted()

                assertTrue(progress.isFinished)
                assertFalse(progress.isSuccess)
            }
    }

    @Test
    fun `when withCompleted is invoked and syncedBrushings is greater than 0, extraction progress is finished and successful`() {
        val totalBrushing = 2

        withBrushingProgress(
            listOf(
                OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime())
            ), totalBrushing
        )
            .withCompleted()
            .isFinishedAndSuccessful()

        withBrushingProgress(
            listOf(
                OfflineBrushingSyncedResult(1, "123", TrustedClock.getNowOffsetDateTime()),
                OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime())
            ), totalBrushing
        )
            .withCompleted()
            .isFinishedAndSuccessful()
    }

    @Test
    fun `when brushings in sync list is equals to the total of brushings but withCompleted hasn't been invoked, extraction progress should never be finished`() {
        val totalBrushing = 2
        val syncedBrushings = listOf(
            OfflineBrushingSyncedResult(1, "123", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime())
        )

        val extractionProgress = withBrushingProgress(syncedBrushings, totalBrushing)

        assertFalse(extractionProgress.isFinished)
        assertFalse(extractionProgress.isSuccess)
    }

    /*
    progress
     */

    @Test
    fun `when 3 out of 4 brushings has been sync, extraction progress should be 75% `() {
        val totalBrushing = 4
        val syncedBrushings = listOf(
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(3, "678", TrustedClock.getNowOffsetDateTime())
        )

        val extractionProgress = withBrushingProgress(syncedBrushings, totalBrushing)

        assertEquals(0.75F, extractionProgress.progress)
    }

    @Test
    fun `when 4 out of 4 brushings has been sync, extraction progress should be 100% `() {
        val totalBrushing = 4
        val syncedBrushings = listOf(
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(2, "345", TrustedClock.getNowOffsetDateTime()),
            OfflineBrushingSyncedResult(3, "678", TrustedClock.getNowOffsetDateTime())
        )

        val extractionProgress = withBrushingProgress(syncedBrushings, totalBrushing)

        assertEquals(1f, extractionProgress.progress)
    }

    @Test
    fun `when class is empty, progress is null`() {
        assertNull(ExtractionProgress.empty().progress)
    }

    @Test
    fun `when empty is flagged as completed, progress is null`() {
        assertNull(ExtractionProgress.empty().withCompleted().progress)
    }

    @Test
    fun `when totalBrushings is 0 and withCompleted is invoked, progress is null`() {
        assertNull(
            withBrushingProgress(emptyList(), 0)
                .withCompleted()
                .progress
        )
    }

    @Test
    fun `when totalBrushings greater than 0 and withCompleted is invoked, progress is 100%`() {
        (1 until 10).map { randomInt() }
            .filter { it > 0 }
            .forEach { totalBrushings ->
                val progress = withBrushingProgress(emptyList(), totalBrushings)
                    .withCompleted()

                assertEquals(1F, progress.progress)
            }
    }

    /*
    Utils
     */

    private fun ExtractionProgress.isFinishedAndSuccessful() {
        assertTrue(isSuccess)
        assertTrue(isFinished)
    }
}
