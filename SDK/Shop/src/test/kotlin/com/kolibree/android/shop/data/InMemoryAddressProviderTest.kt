/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

class InMemoryAddressProviderTest : BaseUnitTest() {

    private val kolibreeConnector: IKolibreeConnector = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val addressProvider = InMemoryAddressProvider(kolibreeConnector, currentProfileProvider)

    @Test
    fun `addresses are not pre-filled by default`() {
        addressProvider.getShippingAddress().test().assertNoValues()
        addressProvider.getBillingAddress().test().assertNoValues()
    }

    @Test
    fun `addresses are pre-populated with profile and account data if they're empty`() {
        val profile = ProfileBuilder.create()
            .withName("Jon")
            .withCountry("Westeros")
            .build()
        val email = "jon@westeros.com"
        val expected = Address(
            firstName = "Jon",
            country = "Westeros",
            email = "jon@westeros.com"
        )

        whenever(kolibreeConnector.email).thenReturn(email)
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(Flowable.just(profile))

        addressProvider.observeForAddress()

        addressProvider.getShippingAddress().test().assertValue(expected)
        addressProvider.getBillingAddress().test().assertValue(expected)
    }

    @Test
    fun `billing is same as shipping by default`() {
        addressProvider.getBillingSameAsShippingAddress().test().assertValue(true)
    }

    @Test
    fun `shipping address change is propagated to listeners`() {
        val tester = addressProvider.getShippingAddress().test()

        addressProvider.updateShippingAddress(Address(firstName = "Jon"))
        addressProvider.updateShippingAddress(Address(firstName = "Jon"))
        addressProvider.updateShippingAddress(Address(firstName = "Jon", city = "Winterfell"))

        tester.assertValues(
            Address(firstName = "Jon"),
            Address(firstName = "Jon", city = "Winterfell")
        )
    }

    @Test
    fun `billing address change is propagated to listeners if it has to be different from shipping`() {
        val tester = addressProvider.getBillingAddress().test()
        addressProvider.setBillingSameAsShippingAddress(false)

        addressProvider.updateBillingAddress(Address(firstName = "Jon"))
        addressProvider.updateBillingAddress(Address(firstName = "Jon"))
        addressProvider.updateBillingAddress(Address(firstName = "Jon", city = "Winterfell"))

        tester.assertValues(
            Address(firstName = "Jon"),
            Address(firstName = "Jon", city = "Winterfell")
        )
    }

    @Test
    fun `shipping address change is also propagated to billing address listeners if it has to be same as shipping`() {
        val tester = addressProvider.getBillingAddress().test()
        addressProvider.setBillingSameAsShippingAddress(true)

        addressProvider.updateShippingAddress(Address(firstName = "Jon"))
        addressProvider.updateShippingAddress(Address(firstName = "Jon"))
        addressProvider.updateShippingAddress(Address(firstName = "Jon", city = "Winterfell"))

        tester.assertValues(
            Address(firstName = "Jon"),
            Address(firstName = "Jon", city = "Winterfell")
        )
    }

    @Test
    fun `billing address change is not propagated to listeners if it has to be same as shipping`() {
        val tester = addressProvider.getBillingAddress().test()
        addressProvider.setBillingSameAsShippingAddress(true)

        addressProvider.updateBillingAddress(Address(firstName = "Jon"))
        addressProvider.updateBillingAddress(Address(firstName = "Jon"))
        addressProvider.updateBillingAddress(Address(firstName = "Jon", city = "Winterfell"))

        tester.assertNoValues()
    }

    @Test
    fun `billing address listeners are notified if same address trigger value is changed`() {
        val shippingAddress = Address(firstName = "Tyrion", city = "King's Landing")
        val billingAddress = Address(firstName = "Jon", city = "Winterfell")

        val shippingTester = addressProvider.getShippingAddress().test()
        val billingTester = addressProvider.getBillingAddress().test()

        addressProvider.setBillingSameAsShippingAddress(false)

        addressProvider.updateShippingAddress(shippingAddress)
        addressProvider.updateBillingAddress(billingAddress)

        shippingTester.assertValues(shippingAddress)
        billingTester.assertValues(billingAddress)

        addressProvider.setBillingSameAsShippingAddress(true)
        shippingTester.assertValues(shippingAddress)
        billingTester.assertValues(billingAddress, shippingAddress)

        addressProvider.setBillingSameAsShippingAddress(false)
        shippingTester.assertValues(shippingAddress)
        billingTester.assertValues(billingAddress, shippingAddress, billingAddress)
    }
}
