/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.retrofit

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.gson.StrippedMacTypeAdapter
import com.kolibree.android.commons.models.StrippedMac
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class StrippedMacTypeAdapterTest : BaseUnitTest() {

    private val adapter = StrippedMacTypeAdapter()

    @Test
    fun `verify can write StrippedMac`() {
        assertEquals(EQUIVALENT_MAC, adapter.toJsonTree(StrippedMac.fromMac(MAC)).asString)
    }

    @Test
    fun `write null writes empty string`() {
        assertEquals("", adapter.toJson(null))
    }

    @Test
    fun `verify can read StrippedMac`() {
        val json = """"$EQUIVALENT_MAC""""
        val fromJson = adapter.fromJson(json)

        assertEquals(StrippedMac.fromMac(MAC), fromJson)
    }

    @Test
    fun `verify null if empty StrippedMac`() {
        val json = """"""""

        assertNull(adapter.fromJson(json))
    }
}

private const val MAC = "10:D0:56:F2:B5:12"
private const val EQUIVALENT_MAC = "10D056F2B512"
