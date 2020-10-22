/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import com.kolibree.android.app.test.BaseUnitTest
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UuidConvertersTest : BaseUnitTest() {
    private val converter = UuidConverters()

    @Test
    fun `fromUUID returns null if value is null or empty`() {
        assertNull(converter.fromUUID(null))
        assertNull(converter.fromUUID(""))
        assertNull(converter.fromUUID("         "))
    }

    @Test
    fun `toUUIDString returns null if value is null`() {
        assertNull(converter.toUUIDString(null))
    }

    @Test
    fun `fromUUID and toUUID return expected value`() {
        val expectedUuid = UUID.randomUUID()

        val asString = converter.toUUIDString(expectedUuid)

        assertEquals(
            expectedUuid,
            converter.fromUUID(asString)
        )
    }
}
