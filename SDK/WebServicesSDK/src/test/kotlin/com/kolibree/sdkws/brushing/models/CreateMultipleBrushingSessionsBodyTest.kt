/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody.Companion.BRUSHINGS_FIELD
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CreateMultipleBrushingSessionsBody] unit tests */
class CreateMultipleBrushingSessionsBodyTest : BaseUnitTest() {

    @Test
    fun `value of BRUSHINGS_FIELD is 'brushings`() {
        assertEquals("brushings", BRUSHINGS_FIELD)
    }
}
