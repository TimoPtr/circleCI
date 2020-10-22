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

class BuildReelsTests {

    @Test
    fun `123 to 456 constructs correct reels`() {
        val expected = arrayOf(
            arrayOf("1", "2", "3", "4"),
            arrayOf("2", "3", "4", "5"),
            arrayOf("3", "4", "5", "6")
        )

        val start = intArrayOf(1, 2, 3)
        val end = intArrayOf(4, 5, 6)

        performExpectedSuccessfulTest(start, end, expected)
    }

    @Test
    fun `Least significant digit wrap constructs correct reels`() {
        val expected = arrayOf(
            arrayOf("1"),
            arrayOf("2", "3"),
            arrayOf("3", "4", "5", "6", "7", "8", "9", "0", "1", "2")
        )

        val start = intArrayOf(1, 2, 3)
        val end = intArrayOf(1, 3, 2)

        performExpectedSuccessfulTest(start, end, expected)
    }

    @Test
    fun `Tens digit wrap constructs correct reels`() {
        val expected = arrayOf(
            arrayOf("1", "2"),
            arrayOf("2", "3", "4", "5", "6", "7", "8", "9", "0", "1"),
            arrayOf("3", "4")
        )

        val start = intArrayOf(1, 2, 3)
        val end = intArrayOf(2, 1, 4)

        performExpectedSuccessfulTest(start, end, expected)
    }

    private fun performExpectedSuccessfulTest(
        start: IntArray,
        end: IntArray,
        expected: Array<Array<String>>
    ) {
        val result = buildReels(start, end)

        assertTrue(result.contentDeepEquals(expected))
    }
}
