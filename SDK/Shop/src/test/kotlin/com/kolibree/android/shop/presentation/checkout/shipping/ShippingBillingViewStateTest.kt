/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.R
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.Address.Input.PROVINCE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ShippingBillingViewStateTest : BaseUnitTest() {

    @Test
    fun `billings should not be visible when it is the same as shipping`() {
        val viewState = ShippingBillingViewState(isBillingSameAsShipping = true)

        assertFalse(viewState.isBillingVisible)
    }

    @Test
    fun `billings should be visible when it different than shipping`() {
        val viewState = ShippingBillingViewState(isBillingSameAsShipping = false)

        assertTrue(viewState.isBillingVisible)
    }

    @Test
    fun `withErrorActivated should activate the error`() {
        val viewState = ShippingBillingViewState().withErrorActivated()

        assertTrue(viewState.formError.isErrorActivated)
    }

    @Test
    fun `when error is not activated errors should return null`() {
        val formError = ShippingBillingViewState().formError
        val emptyValue = ""

        assertNull(formError.getError(emptyValue, FIRST_NAME))
        assertNull(formError.getError(emptyValue, LAST_NAME))
        assertNull(formError.getError(emptyValue, COUNTRY))
        assertNull(formError.getError(emptyValue, CITY))
        assertNull(formError.getError(emptyValue, ADDRESS_LINE_1))
        assertNull(formError.getError(emptyValue, POSTAL_CODE))
        assertNull(formError.getError(emptyValue, PROVINCE))
    }

    @Test
    fun `when error is not activated errors should return the desired messages`() {

        val form = ShippingBillingViewState().withErrorActivated().formError
        val empty = ""

        assertEquals(R.string.form_shipping_first_name_error, form.getError(empty, FIRST_NAME))
        assertEquals(R.string.form_shipping_last_name_error, form.getError(empty, LAST_NAME))
        assertEquals(R.string.form_shipping_country_error, form.getError(empty, COUNTRY))
        assertEquals(R.string.form_shipping_city_error, form.getError(empty, CITY))
        assertEquals(R.string.form_shipping_address_error, form.getError(empty, ADDRESS_LINE_1))
        assertEquals(R.string.form_shipping_postal_code_error, form.getError(empty, POSTAL_CODE))
        assertEquals(R.string.form_shipping_province_error, form.getError(empty, PROVINCE))
    }
}
