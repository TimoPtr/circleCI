/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import org.junit.Assert.assertEquals
import org.junit.Test

/** [FileType] tests */
class FileTypeTest : BaseUnitTest() {

    @Test
    fun `value of BRUSHING file type is 0x01`() {
        assertEquals(0x01.toByte(), FileType.BRUSHING.value)
    }

    @Test
    fun `value of PLAQLESS file type is 0x02`() {
        assertEquals(0x02.toByte(), FileType.PLAQLESS.value)
    }

    @Test
    fun `value of GLINT file type is 0x03`() {
        assertEquals(0x03.toByte(), FileType.GLINT.value)
    }
}
