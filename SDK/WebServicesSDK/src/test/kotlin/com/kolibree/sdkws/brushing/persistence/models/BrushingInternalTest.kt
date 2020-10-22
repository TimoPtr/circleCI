/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.persistence.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.mock
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit

class BrushingInternalTest : BaseUnitTest() {

    @Test
    fun `test fromBrushingData CreateBrushingData is truncate to seconds`() {
        val date = OffsetDateTime.of(2001, 1, 1, 1, 1, 1, 42, ZoneOffset.MAX)
        val createBrushingData = CreateBrushingData("co", 100, 120, date, 0)
        val brushingInternal = BrushingInternal.fromBrushingData(createBrushingData, 42)

        assertEquals(
            date.truncatedTo(ChronoUnit.SECONDS),
            brushingInternal.dateTime
        )
    }

    @Test // https://kolibree.atlassian.net/browse/KLTB002-10325
    fun `fromBrushingData returns an instance with isSynchronized false`() {
        val date = OffsetDateTime.of(2001, 1, 1, 1, 1, 1, 42, ZoneOffset.MAX)
        val createBrushingData = CreateBrushingData("co", 100, 120, date, 0)
        val brushingInternal = BrushingInternal.fromBrushingData(createBrushingData, 42)

        assertFalse(brushingInternal.isSynchronized)
    }

    @Test
    fun `test extract brushing return date truncate to seconds`() {
        val date = OffsetDateTime.of(2001, 1, 1, 1, 1, 1, 42, ZoneOffset.MAX)
        val brushingInternal = BrushingInternal(
            null,
            "co", 0, date, 0, 0, false, 120, "", 0, 0, false, "", "", "", "", UUID.randomUUID()
        )

        val extractedBrushing = brushingInternal.extractBrushing()

        assertEquals(
            date.truncatedTo(ChronoUnit.SECONDS),
            extractedBrushing.dateTime
        )
    }

    @Test
    fun `test extractCreateBrushingData return date truncate to seconds`() {
        val date = OffsetDateTime.of(2001, 1, 1, 1, 1, 1, 42, ZoneOffset.MAX)

        val brushingInternal = BrushingInternal(
            null,
            "co", 0, date, 0, 0, false, 120, null, 0, 0, false, "", "", "", "", UUID.randomUUID()
        )

        val extractCreateBrushingData = brushingInternal.extractCreateBrushingData(mock())

        assertEquals(
            date.truncatedTo(ChronoUnit.SECONDS),
            extractCreateBrushingData.date
        )
    }
}
