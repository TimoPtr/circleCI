/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.data.request.query.mutation.toMailingAddressInput
import com.kolibree.android.shop.googleWalletPayment
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GoogleWalletPaymentToShopifyAddressTest : BaseUnitTest() {

    @Test
    fun `toMailingAddressInput returns a MailingAddressInput filled from GoogleWalletPayment`() {
        val googleWalletPayment = googleWalletPayment()

        val mailingAddressInput = googleWalletPayment.toMailingAddressInput()

        val billingAddress = googleWalletPayment.billingAddress
        assertEquals(billingAddress.address1, mailingAddressInput.address1)
        assertEquals(billingAddress.address2, mailingAddressInput.address2)
        assertEquals(billingAddress.countryCode, mailingAddressInput.country)
        assertEquals(billingAddress.postalCode, mailingAddressInput.zip)
        assertEquals(billingAddress.name, mailingAddressInput.firstName)
        assertEquals(billingAddress.administrativeArea, mailingAddressInput.province)
        assertEquals(billingAddress.locality, mailingAddressInput.city)
    }
}
