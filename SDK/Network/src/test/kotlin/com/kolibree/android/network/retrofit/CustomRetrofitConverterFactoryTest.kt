/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.retrofit

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.models.StrippedMac
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test
import retrofit2.Converter

class CustomRetrofitConverterFactoryTest : BaseUnitTest() {
    private val factory = CustomRetrofitConverterFactory.create()

    @Test
    fun `strippedMac is converted to String`() {
        @Suppress("UNCHECKED_CAST")
        val converter = factory.stringConverter(
            StrippedMac::class.java,
            arrayOf(),
            mock()
        ) as Converter<StrippedMac, String>

        val mac = "aa:bb:cc:dd:ee:ff"
        val strippedMac = StrippedMac.fromMac(mac)

        assertEquals(strippedMac.value, converter.convert(strippedMac))
    }
}
