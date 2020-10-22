/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import com.kolibree.android.app.test.BaseUnitTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.threeten.bp.Duration

@RunWith(Parameterized::class)
class BuildValuesRangeTest(
    private val start: Duration,
    private val end: Duration,
    private val increment: Duration,
    private val expected: List<Duration>
) : BaseUnitTest() {

    companion object {
        @JvmStatic
        @Parameters
        fun data() = listOf<Any>(
            arrayOf(
                Duration.ZERO,
                Duration.ofSeconds(30),
                Duration.ofSeconds(5),
                listOf(
                    Duration.ZERO,
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(15),
                    Duration.ofSeconds(20),
                    Duration.ofSeconds(25),
                    Duration.ofSeconds(30)
                )),
            arrayOf(
                Duration.ZERO,
                Duration.ofSeconds(34),
                Duration.ofSeconds(5),
                listOf(
                    Duration.ZERO,
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(15),
                    Duration.ofSeconds(20),
                    Duration.ofSeconds(25),
                    Duration.ofSeconds(30)
                )),
            arrayOf(
                Duration.ofSeconds(30),
                Duration.ofSeconds(40),
                Duration.ofSeconds(2),
                listOf(
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(32),
                    Duration.ofSeconds(34),
                    Duration.ofSeconds(36),
                    Duration.ofSeconds(38),
                    Duration.ofSeconds(40)
                ))
        )
    }

    @Test
    fun `buildValuesRange returns correct values`() {
        val result = buildValuesRange(start, end, increment)
        assertThat(result, equalTo(expected))
    }
}

@RunWith(Parameterized::class)
class BuildMinuteValuesTest(
    private val values: List<Duration>,
    private val expected: List<Int>
) : BaseUnitTest() {

    companion object {
        @JvmStatic
        @Parameters
        fun data() = listOf<Any>(
            arrayOf(
                buildValuesRange(
                    Duration.ofSeconds(55),
                    Duration.ofSeconds(65),
                    Duration.ofSeconds(5)
                ),
                listOf(0, 1)
            ),
            arrayOf(
                buildValuesRange(
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(40),
                    Duration.ofSeconds(2)
                ),
                listOf(0)
            )
        )
    }

    @Test
    fun `buildValuesRange returns correct values`() {
        val result = buildMinuteValues(values)
        assertThat(result, equalTo(expected))
    }
}

@RunWith(Parameterized::class)
class BuildMinuteStringsTest(
    private val values: List<Int>,
    private val expected: Array<String>
) : BaseUnitTest() {

    companion object {
        @JvmStatic
        @Parameters
        fun data() = listOf<Any>(
            arrayOf(
                listOf(1, 2, 3),
                arrayOf("1", "2", "3")
            ),
            arrayOf(
                listOf(4, 5, 6),
                arrayOf("4", "5", "6")
            )
        )
    }

    @Test
    fun `buildValuesRange returns correct values`() {
        val result = buildMinuteStrings(values)
        assertThat(result, equalTo(expected))
    }
}

@RunWith(Parameterized::class)
class BuildSecondsStringsTest(
    private val values: List<Duration>,
    private val expected: Array<String>
) : BaseUnitTest() {

    companion object {
        @JvmStatic
        @Parameters
        fun data() = listOf<Any>(
            arrayOf(
                buildValuesRange(
                    Duration.ofSeconds(115),
                    Duration.ofSeconds(125),
                    Duration.ofSeconds(5)
                ),
                arrayOf("55", "00", "05")
            ),
            arrayOf(
                buildValuesRange(
                    Duration.ofSeconds(237),
                    Duration.ofSeconds(242),
                    Duration.ofSeconds(1)
                ),
                arrayOf("57", "58", "59", "00", "01", "02")
            )
        )
    }

    @Test
    fun `buildValuesRange returns correct values`() {
        val result = buildSecondsStrings(values)
        assertThat(result, equalTo(expected))
    }
}
