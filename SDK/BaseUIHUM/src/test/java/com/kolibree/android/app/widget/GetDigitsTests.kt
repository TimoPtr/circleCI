/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget

import org.junit.Assert.assertTrue
import org.junit.Test

class GetDigitsTests {

    @Test
    fun `zero creates a single zero digit`() {
        val expected = intArrayOf(0)

        val result = getDigits(0)

        assertTrue(result.contentEquals(expected))
    }

    @Test
    fun `123 creates 123 digits`() {
        val expected = intArrayOf(1, 2, 3)

        val result = getDigits(123)

        assertTrue(result.contentEquals(expected))
    }

    @Test
    fun `123 with minDigits 5 creates 00123 digits`() {
        val expected = intArrayOf(0, 0, 1, 2, 3)

        val result = getDigits(123, 5)

        assertTrue(result.contentEquals(expected))
    }
}
