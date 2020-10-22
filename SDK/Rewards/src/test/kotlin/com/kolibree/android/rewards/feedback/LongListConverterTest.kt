package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LongListConverterTest : BaseUnitTest() {

    private val converter = LongListConverter()

    @Test
    fun `fromLongList empty list returns empty string`() {
        assertEquals("", converter.fromLongList(emptyList()))
    }

    @Test
    fun `fromLongList for list (1) return string "1"`() {
        assertEquals("1", converter.fromLongList(listOf(1)))
    }

    @Test
    fun `fromLongList for list (1,2) return string "1,2"`() {
        assertEquals("1,2", converter.fromLongList(listOf(1, 2)))
    }

    @Test
    fun `fromLongList for list (1,2,3,4) return string "1,2,3,4"`() {
        assertEquals("1,2,3,4", converter.fromLongList(listOf(1, 2, 3, 4)))
    }

    @Test
    fun `toLongList for empty string returns empty list`() {
        assertEquals(emptyList<Long>(), converter.toLongList(""))
    }

    @Test
    fun `toLongList for string "1" returns list (1)`() {
        val result = converter.toLongList("1")
        assertEquals(1, result.size)
        assertEquals(1, result[0])
    }

    @Test
    fun `toLongList for string "1,2" returns list (1,2)`() {
        val result = converter.toLongList("1,2")
        assertEquals(2, result.size)
        assertEquals(1, result[0])
        assertEquals(2, result[1])
    }

    @Test
    fun `toLongList for string "1,2,3,4" returns list (1,2,3,4)`() {
        val result = converter.toLongList("1,2,3,4")
        assertEquals(4, result.size)
        assertEquals(1, result[0])
        assertEquals(2, result[1])
        assertEquals(3, result[2])
        assertEquals(4, result[3])
    }
}
