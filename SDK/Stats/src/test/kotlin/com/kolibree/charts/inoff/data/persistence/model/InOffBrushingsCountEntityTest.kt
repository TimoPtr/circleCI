/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.data.persistence.model

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class InOffBrushingsCountEntityTest : BaseUnitTest() {
    @Test
    fun `toInOffBrushingsCount creates a valid domain object`() {
        val response = InOffBrushingsCountEntity(1L, 10, 11)

        val result = response.toInOffBrushingsCount()

        assertEquals(response.offlineBrushingCount, result.offlineBrushingCount)
        assertEquals(response.onlineBrushingCount, result.onlineBrushingCount)
        assertEquals(response.profileId, result.profileId)
    }
}
