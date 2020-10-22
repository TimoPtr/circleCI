/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PlaqlessCommandSetTest : BaseUnitTest() {

    @Test
    fun `generateControlPayload return 0 when plaqlessData false and rawData false`() {
        assertEquals(0, generateControlPayload(plaqlessDataEnable = false, rawDataEnable = false))
    }

    @Test
    fun `generateControlPayload return 1 when plaqlessData true and rawData false`() {
        assertEquals(1, generateControlPayload(plaqlessDataEnable = true, rawDataEnable = false))
    }

    @Test
    fun `generateControlPayload return 2 when plaqlessData false and rawData true`() {
        assertEquals(2, generateControlPayload(plaqlessDataEnable = false, rawDataEnable = true))
    }

    @Test
    fun `generateControlPayload return 3 when plaqlessData true and rawData true`() {
        assertEquals(3, generateControlPayload(plaqlessDataEnable = true, rawDataEnable = true))
    }
}
