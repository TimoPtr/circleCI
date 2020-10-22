/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.tracker.EventTracker
import io.mockk.mockk
import io.mockk.verify
import java.util.Currency
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TrackerConstantInstrumentedTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /*
    onProductListAddToCartClick
     */

    @Test
    fun onProductListAddToCartClick_invokes_tracker_send_unknown_if_decode_variantId_fails() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListAddToCartClick(invalidProduct)

        verify {
            tracker.sendEvent(ADD_TO_CART.plus("variantId" to "Unknown"))
        }
    }

    @Test
    fun onProductListAddToCartClick_invokes_tracker_send() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListAddToCartClick(validProduct)

        verify {
            tracker.sendEvent(ADD_TO_CART.plus("variantId" to "14429510533155"))
        }
    }

    /*
    onProductListIncreaseQuantityClick
     */

    @Test
    fun onProductListIncreaseQuantityClick_invokes_tracker_send_unknown_if_decode_variantId_fails() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListIncreaseQuantityClick(invalidProduct)

        verify {
            tracker.sendEvent((SHOP_CATEGORY + "AddToCart_plus").plus("variantId" to "Unknown"))
        }
    }

    @Test
    fun onProductListIncreaseQuantityClick_invokes_tracker_send() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListIncreaseQuantityClick(validProduct)

        verify {
            tracker.sendEvent((SHOP_CATEGORY + "AddToCart_plus").plus("variantId" to "14429510533155"))
        }
    }

    /*
    onProductListDecreaseQuantityClick
     */

    @Test
    fun onProductListDecreaseQuantityClick_invokes_tracker_send_unknown_if_decode_variantId_fails() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListDecreaseQuantityClick(invalidProduct)

        verify {
            tracker.sendEvent((SHOP_CATEGORY + "AddToCart_minus").plus("variantId" to "Unknown"))
        }
    }

    @Test
    fun onProductListDecreaseQuantityClick_invokes_tracker_send() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onProductListDecreaseQuantityClick(validProduct)

        verify {
            tracker.sendEvent((SHOP_CATEGORY + "AddToCart_minus").plus("variantId" to "14429510533155"))
        }
    }

    /*
    onCheckoutIncreaseQuantityClick
     */

    @Test
    fun onCheckoutIncreaseQuantityClick_invokes_tracker_send_unknown_if_decode_variantId_fails() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onCheckoutIncreaseQuantityClick(invalidProduct)

        verify {
            tracker.sendEvent((CHECKOUT_CATEGORY + "plus").plus("variantId" to "Unknown"))
        }
    }

    @Test
    fun onCheckoutIncreaseQuantityClick_invokes_tracker_send() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onCheckoutIncreaseQuantityClick(validProduct)

        verify {
            tracker.sendEvent((CHECKOUT_CATEGORY + "plus").plus("variantId" to "14429510533155"))
        }
    }

    /*
    onCheckoutDecreaseQuantityClick
     */

    @Test
    fun onCheckoutDecreaseQuantityClick_invokes_tracker_send_unknown_if_decode_variantId_fails() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onCheckoutDecreaseQuantityClick(invalidProduct)

        verify {
            tracker.sendEvent((CHECKOUT_CATEGORY + "minus").plus("variantId" to "Unknown"))
        }
    }

    @Test
    fun onCheckoutDecreaseQuantityClick_invokes_tracker_send() {
        val tracker = mockk<EventTracker>(relaxed = true)

        tracker.onCheckoutDecreaseQuantityClick(validProduct)

        verify {
            tracker.sendEvent((CHECKOUT_CATEGORY + "minus").plus("variantId" to "14429510533155"))
        }
    }

    private val validProduct = Product(
        "",
        "Z2lkOi8vc2hvcGlmeS9Qcm9kdWN0VmFyaWFudC8xNDQyOTUxMDUzMzE1NQ==",
        "",
        "",
        "",
        "",
        "",
        Price.Companion.createFromSmiles(1, Currency.getInstance("EUR")),
        emptyList(),
        null,
        ""
    )

    private val invalidProduct = Product(
        "",
        "a",
        "",
        "",
        "",
        "",
        "",
        Price.Companion.createFromSmiles(1, Currency.getInstance("EUR")),
        emptyList(),
        null,
        ""
    )
}
