/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class StringExtensionsKtTest {
    @Test
    fun `isNullOrNullValue returns true when string is null`() {
        val mystring: String? = null
        assertTrue(mystring.isNullOrNullValue())
    }

    @Test
    fun `isNullOrNullValue returns true when string is empty`() {
        assertTrue("".isNullOrNullValue())
    }

    @Test
    fun `isNullOrNullValue returns true when string is null text, independently of the case`() {
        assertTrue("null".isNullOrNullValue())
        assertTrue("NULL".isNullOrNullValue())
        assertTrue("NUlL".isNullOrNullValue())
        assertTrue("nulL".isNullOrNullValue())
    }

    @Test
    fun `isNullOrNullValue returns false when string is nor empty nor null`() {
        assertFalse("random".isNullOrNullValue())
    }

    @Test
    fun `takeIfNotBlank returns null when source is null`() {
        val source: String? = null
        assertNull(source.takeIfNotBlank())
    }

    @Test
    fun `takeIfNotBlank returns null when source is blank`() {
        val source: String = ""
        assertNull(source.takeIfNotBlank())
    }

    @Test
    fun `takeIfNotBlank returns source when source is not blank`() {
        val source: String = "hello"
        assertEquals("hello", source.takeIfNotBlank())
    }
}
