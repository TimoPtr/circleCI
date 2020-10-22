/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import com.google.android.gms.samples.wallet.GoogleWalletNetwork
import com.google.android.gms.samples.wallet.toGooglePayNetwork
import com.kolibree.android.app.test.BaseUnitTest
import com.shopify.buy3.Storefront
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GoogleWalletConfigurationKtTest : BaseUnitTest() {
    @Test
    fun `CardBrand AMERICAN_EXPRESS is mapped to GoogleWalletNetwork AMERICAN_EXPRESS`() {
        assertEquals(
            GoogleWalletNetwork.AMERICAN_EXPRESS,
            Storefront.CardBrand.AMERICAN_EXPRESS.toGooglePayNetwork()
        )
    }

    @Test
    fun `CardBrand JCB is mapped to GoogleWalletNetwork JCB`() {
        assertEquals(
            GoogleWalletNetwork.JCB,
            Storefront.CardBrand.JCB.toGooglePayNetwork()
        )
    }

    @Test
    fun `CardBrand VISA is mapped to GoogleWalletNetwork VISA`() {
        assertEquals(
            GoogleWalletNetwork.VISA,
            Storefront.CardBrand.VISA.toGooglePayNetwork()
        )
    }

    @Test
    fun `CardBrand MASTERCARD is mapped to GoogleWalletNetwork MASTERCARD`() {
        assertEquals(
            GoogleWalletNetwork.MASTERCARD,
            Storefront.CardBrand.MASTERCARD.toGooglePayNetwork()
        )
    }

    @Test
    fun `CardBrand DINERS_CLUB is mapped to GoogleWalletNetwork UNKNOWN`() {
        assertEquals(
            GoogleWalletNetwork.UNKNOWN,
            Storefront.CardBrand.DINERS_CLUB.toGooglePayNetwork()
        )
    }

    @Test
    fun `CardBrand UNKNOWN_VALUE is mapped to GoogleWalletNetwork UNKNOWN`() {
        assertEquals(
            GoogleWalletNetwork.UNKNOWN,
            Storefront.CardBrand.UNKNOWN_VALUE.toGooglePayNetwork()
        )
    }
}
