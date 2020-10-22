/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.sdkws.data.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class UpdateToothbrushDataTest : BaseUnitTest() {
    @Test
    fun `constructor removes colons from mac`() {
        val mac = "10:D0:56:F2:B5:12"
        val expectedMac = "10D056F2B512"

        val updateToothbrushData = UpdateToothbrushData(
            KLTBConnectionBuilder.DEFAULT_SERIAL,
            mac,
            "device_id"
        )

        assertEquals(expectedMac, updateToothbrushData.macAddress)
    }
}
