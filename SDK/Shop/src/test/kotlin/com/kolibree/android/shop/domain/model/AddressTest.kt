/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AddressTest : BaseUnitTest() {

    @Test
    fun `country is set to United States by default for now`() {
        assertEquals(
            "United States",
            Address().country
        )
    }

    @Test
    fun `newly created address is treated as empty`() {
        assertTrue(Address().isEmpty())
    }

    @Test
    fun `all mandatory fields needs to be non-null and non-empty`() {
        var address = Address()
        assertFalse(address.hasAllMandatoryFields())
        address = address.copy(firstName = "Jon").also { assertFalse(it.hasAllMandatoryFields()) }
        address = address.copy(lastName = "Snow").also { assertFalse(it.hasAllMandatoryFields()) }
        address =
            address.copy(street = "66 Bastard Ave").also { assertFalse(it.hasAllMandatoryFields()) }
        address =
            address.copy(city = "Winterfell").also { assertFalse(it.hasAllMandatoryFields()) }
        address =
            address.copy(postalCode = "WEST01 WIN01")
                .also { assertFalse(it.hasAllMandatoryFields()) }
        address =
            address.copy(country = "Westeros").also { assertFalse(it.hasAllMandatoryFields()) }
        address =
            address.copy(province = "The North").also { assertTrue(it.hasAllMandatoryFields()) }
        address =
            address.copy(email = "jon@winterfell.we")
                .also { assertTrue(it.hasAllMandatoryFields()) }
        address.copy(phoneNumber = "00-66-JON-SNOW").also { assertTrue(it.hasAllMandatoryFields()) }
        address.copy(company = "Stark Industries").also { assertTrue(it.hasAllMandatoryFields()) }
    }

    @Test
    fun `all null or empty input fields are returned as errors`() {
        val address = Address()
        assertEquals(
            listOf(
                Address.Input.FIRST_NAME,
                Address.Input.LAST_NAME,
                Address.Input.ADDRESS_LINE_1,
                Address.Input.CITY,
                Address.Input.POSTAL_CODE,
                // TODO remove this after non-US checkout is integrated
                // Address.Input.COUNTRY,
                Address.Input.PROVINCE
            ),
            address.getInputErrors()
        )
        assertFalse(address.hasAllMandatoryFields())
    }

    @Test
    fun `address with all mandatory fields doesn't have errors`() {
        val address = Address(
            firstName = "Jon",
            lastName = "Snow",
            street = "66 Bastard Ave",
            city = "Winterfell",
            postalCode = "WEST01 WIN01",
            country = "Westeros",
            province = "The North"
        )
        assertTrue(address.getInputErrors().isEmpty())
        assertTrue(address.hasAllMandatoryFields())
    }
}
