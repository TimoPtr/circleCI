/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import com.google.android.gms.samples.wallet.GoogleWalletConfigurationImpl
import com.google.android.gms.samples.wallet.GoogleWalletNetwork
import com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_PRODUCTION
import com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_TEST
import com.google.android.gms.wallet.WalletConstants.THEME_LIGHT
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.GooglePayProductionEnvironmentFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.shop.buildStoreDetails
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.domain.model.StoreDetails
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.Storefront
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class GoogleWalletConfigurationImplTest : BaseUnitTest() {

    private lateinit var walletConfiguration: GoogleWalletConfigurationImpl
    private val shopifyClientWrapper: ShopifyClientWrapper = mock()

    @Test
    fun `paymentsEnvironment returns ENVIRONMENT_TEST if production feature toggle is off`() {
        initWithFeatureToggle(false)

        assertEquals(ENVIRONMENT_TEST, walletConfiguration.paymentsEnvironment)
    }

    @Test
    fun `paymentsEnvironment returns ENVIRONMENT_PRODUCTION if production feature toggle is on`() {
        initWithFeatureToggle(true)

        assertEquals(ENVIRONMENT_PRODUCTION, walletConfiguration.paymentsEnvironment)
    }

    /*
    supportedNetworks
     */
    @Test
    fun `supportedNetworks returns a list of google supported networks mapped from shopify`() {
        init()

        val shopifyCardBrands = listOf(
            Storefront.CardBrand.AMERICAN_EXPRESS,
            Storefront.CardBrand.DISCOVER,
            Storefront.CardBrand.DINERS_CLUB // Dinners club is ignored, thus it shouldn't be returned
        )
        prepareStore(buildStoreDetails(acceptedCardBrands = shopifyCardBrands.map { it.name }))

        val expectedGoogleCards =
            listOf(GoogleWalletNetwork.AMERICAN_EXPRESS, GoogleWalletNetwork.DISCOVER)

        walletConfiguration.supportedNetworks().test()
            .assertValue(expectedGoogleCards.map { it.jsonValue })
    }

    /*
    fetchStoreDetails
     */
    @Test
    fun `fetchStoreDetails only requests StoreDetails once, even on multiple calls`() {
        init()

        val expectedStore = buildStoreDetails()
        prepareStore(expectedStore)

        walletConfiguration.fetchStoreDetails().test().assertValue(expectedStore)

        walletConfiguration.fetchStoreDetails().test().assertValue(expectedStore)

        verify(shopifyClientWrapper, times(1)).getStoreDetails()
    }

    @Test
    fun `fetchStoreDetails requests StoreDetails multiple times if the first one errored`() {
        init()

        val expectedStore = buildStoreDetails()
        whenever(shopifyClientWrapper.getStoreDetails())
            .thenReturn(Single.error(TestForcedException()), Single.just(expectedStore))

        walletConfiguration.fetchStoreDetails().test().assertError(TestForcedException::class.java)

        walletConfiguration.fetchStoreDetails().test().assertValue(expectedStore)

        verify(shopifyClientWrapper, times(2)).getStoreDetails()
    }

    /*
    storeCountryCode
     */
    @Test
    fun `storeCountryCode returns a list of google supported networks mapped from shopify`() {
        init()

        val expectedCountryCode = "ES"
        prepareStore(buildStoreDetails(countryCode = expectedCountryCode))

        walletConfiguration.storeCountryCode().test()
            .assertValue(expectedCountryCode)
    }

    /*
    Utils
     */
    private fun init() {
        initWithFeatureToggle(false)
    }

    private fun initWithFeatureToggle(productionEnabled: Boolean) {
        val toggle = ConstantFeatureToggle(
            GooglePayProductionEnvironmentFeature,
            initialValue = productionEnabled
        )
        val featureToggleSet: FeatureToggleSet = setOf(toggle)

        walletConfiguration =
            GoogleWalletConfigurationImpl(featureToggleSet, shopifyClientWrapper, THEME_LIGHT)
    }

    private fun prepareStore(storeDetails: StoreDetails) {
        whenever(shopifyClientWrapper.getStoreDetails())
            .thenReturn(Single.just(storeDetails))
    }
}
