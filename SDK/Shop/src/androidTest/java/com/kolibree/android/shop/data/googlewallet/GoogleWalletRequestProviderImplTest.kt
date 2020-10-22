/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.samples.wallet.GoogleWalletConfiguration
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.WalletConstants.THEME_LIGHT
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.test.BaseInstrumentationTest
import io.reactivex.Single
import java.math.BigDecimal
import java.util.Currency
import junit.framework.TestCase.assertEquals
import org.json.JSONObject
import org.junit.Test

internal class GoogleWalletRequestProviderImplTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val walletConfiguration = FakeGoogleWalletConfiguration()

    private val requestProvider = GoogleWalletRequestProviderImpl(walletConfiguration)

    @Test
    fun getPaymentDataRequest_returns_JSONObject_with_transactionInfo_from_price_and_country_from_store() {
        val price = Price.create(
            decimalAmount = BigDecimal.valueOf(37.22),
            currency = Currency.getInstance("EUR")
        )

        val paymentDataRequest =
            requestProvider.getPaymentDataRequest(price).test().values().single()

        val json = JSONObject(paymentDataRequest.toJson())

        val transactionInfo = json.getJSONObject("transactionInfo")

        assertEquals(price.decimalAmount.toString(), transactionInfo.getString("totalPrice"))
        assertEquals(price.currency.currencyCode, transactionInfo.getString("currencyCode"))
        assertEquals("ESTIMATED", transactionInfo.getString("totalPriceStatus"))
        assertEquals(walletConfiguration.countryCode, transactionInfo.getString("countryCode"))
    }

    @Test
    fun getPaymentDataRequest_returns_JSONObject_with_ApiVersion() {
        val price = Price.create(
            decimalAmount = BigDecimal.valueOf(37.22),
            currency = Currency.getInstance("EUR")
        )

        val paymentDataRequest =
            requestProvider.getPaymentDataRequest(price).test().values().single()

        val json = JSONObject(paymentDataRequest.toJson())

        assertEquals(API_MAJOR, json.getInt("apiVersion"))
        assertEquals(API_MINOR, json.getInt("apiVersionMinor"))
    }

    /*
    isReadyToPayRequest
     */

    @Test
    fun isReadyToPayRequest_returns_JSONObject_with_ApiVersion() {
        val isReadyToPayRequest = requestProvider.isReadyToPayRequest().test().values().single()

        val json = JSONObject(isReadyToPayRequest.toJson())

        assertEquals(API_MAJOR, json.getInt("apiVersion"))
        assertEquals(API_MINOR, json.getInt("apiVersionMinor"))
    }
}

internal data class FakeGoogleWalletConfiguration(
    override val paymentsEnvironment: Int = WalletConstants.ENVIRONMENT_TEST,
    val supportedNetworks: List<String> = listOf(),
    override val supportedMethods: List<String> = listOf(),
    override val paymentGatewayTokenizationName: String = TEST_MERCHANT_NAME,
    override val paymentGatewayTokenizationParameters: Map<String, String> = mapOf(
        "gateway" to paymentGatewayTokenizationName,
        "gatewayMerchantId" to "exampleGatewayMerchantId"
    ),
    val countryCode: String = "FR",
    override val theme: Int = THEME_LIGHT
) : GoogleWalletConfiguration {
    override fun supportedNetworks(): Single<List<String>> = Single.just(supportedNetworks)

    override fun storeCountryCode(): Single<String> = Single.just(countryCode)

    override val isProductionEnvironment: Boolean = false
}

private const val TEST_MERCHANT_NAME = "Heman"
