/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.EventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class TrackerConstantTest : BaseUnitTest() {

    @Test
    fun `CART_ACTIVITY equals Shop_Cart`() {
        assertEquals("Shop_Cart", CART_ACTIVITY.name)
    }

    @Test
    fun `SHOP_CATEGORY equals Shop`() {
        assertEquals("Shop", SHOP_CATEGORY.name)
    }

    @Test
    fun `CHECKOUT_CATEGORY equals Checkout`() {
        assertEquals("Checkout", CHECKOUT_CATEGORY.name)
    }

    @Test
    fun `GPAY_CHECKOUT equals googlepay`() {
        assertEquals("googlepay", GPAY_CHECKOUT)
    }

    @Test
    fun `CLASSIC_CHECKOUT equals classic`() {
        assertEquals("classic", CLASSIC_CHECKOUT)
    }

    @Test
    fun `onUseSmilesClick invokes sendEvent with yes when useSmiles true`() {
        val tracker = mock<EventTracker>()
        tracker.onUseSmilesClick(true)

        verify(tracker).sendEvent(CHECKOUT_CATEGORY + "UseMySmiles_On")
    }

    @Test
    fun `onUseSmilesClick invokes sendEvent with no when useSmiles false`() {
        val tracker = mock<EventTracker>()
        tracker.onUseSmilesClick(false)

        verify(tracker).sendEvent(CHECKOUT_CATEGORY + "UseMySmiles_Off")
    }

    @Test
    fun `onBuyWithAnotherMethodClick invokes sendEvent with CLASSIC_CHECKOUT`() {
        val tracker = mock<EventTracker>()
        tracker.onBuyWithAnotherMethodClick()

        verify(tracker).sendEvent(CHECKOUT_CATEGORY + CLASSIC_CHECKOUT)
    }

    @Test
    fun `onBuyWithGooglePayClick invokes sendEvent with GPAY_CHECKOUT`() {
        val tracker = mock<EventTracker>()
        tracker.onBuyWithGooglePayClick()

        verify(tracker).sendEvent(CHECKOUT_CATEGORY + GPAY_CHECKOUT)
    }

    @Test
    fun `onCheckoutIncreaseQuantityClick invokes sendEvent with plus`() {
        val tracker = mock<EventTracker>()
        val productToAdd = buildProduct(variantOrdinal = 123)

        tracker.onCheckoutIncreaseQuantityClick(productToAdd)

        verify(tracker).sendEvent((CHECKOUT_CATEGORY + "plus").plus("variantId" to "Unknown"))
    }

    @Test
    fun `onCheckoutDecreaseQuantityClick invokes sendEvent with minus`() {
        val tracker = mock<EventTracker>()
        val productToAdd = buildProduct(variantOrdinal = 123)

        tracker.onCheckoutDecreaseQuantityClick(productToAdd)

        verify(tracker).sendEvent((CHECKOUT_CATEGORY + "minus").plus("variantId" to "Unknown"))
    }
}
