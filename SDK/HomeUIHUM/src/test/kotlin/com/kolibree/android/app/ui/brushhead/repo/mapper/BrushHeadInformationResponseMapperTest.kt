/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.clock.TrustedClock
import org.junit.Assert.assertEquals
import org.junit.Test

class BrushHeadInformationResponseMapperTest : BaseUnitTest() {

    private val mapper = BrushHeadInformationResponseMapper

    @Test
    fun `response is mapped as expected`() {
        val expectedDate = TrustedClock.getNowOffsetDateTime()
        val expectedMac = "das"
        val expectedPercentage = 78
        val expectedBrushHeadInformation = BrushHeadInformation(
            macAddress = expectedMac,
            percentageLeft = expectedPercentage,
            resetDate = expectedDate
        )

        val brushHeadInformation = mapper.map(
            expectedMac,
            BrushHeadInformationResponse(expectedDate, 0, 0, 0, expectedPercentage)
        )

        assertEquals(expectedBrushHeadInformation, brushHeadInformation)
    }
}
