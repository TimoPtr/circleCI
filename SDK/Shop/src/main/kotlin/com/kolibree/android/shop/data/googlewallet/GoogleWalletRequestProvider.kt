/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import androidx.annotation.VisibleForTesting
import com.google.android.gms.samples.wallet.GoogleWalletConfiguration
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.kolibree.android.shop.domain.model.Price
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal interface GoogleWalletRequestProvider {
    /**
     * @return [Single] that will emit [IsReadyToPayRequest] or JSONException
     */
    fun isReadyToPayRequest(): Single<IsReadyToPayRequest>

    /**
     * @return [Single] that will emit [PaymentDataRequest] or JSONException
     */
    fun getPaymentDataRequest(price: Price): Single<PaymentDataRequest>
}

internal class GoogleWalletRequestProviderImpl
constructor(
    private val walletConfiguration: GoogleWalletConfiguration
) : GoogleWalletRequestProvider {

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private val baseRequest = JSONObject().apply {
        put("apiVersion", API_MAJOR)
        put("apiVersionMinor", API_MINOR)
    }

    /**
     * Information about the merchant requesting payment information
     *
     * @return Information about the merchant.
     * @throws JSONException
     * @see [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
     */
    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", "Example Merchant")

    /**
     * Card authentication methods supported.
     *
     * @return Allowed card authentication methods.
     * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private val allowedCardAuthMethods = JSONArray(walletConfiguration.supportedMethods)

    /**
     * Gateway Integration: Identifies the gateway and its merchant identifier.
     *
     * The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     * @see [PaymentMethodTokenizationSpecification]
     * (https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
     */
    private fun gatewayTokenizationSpecification(): JSONObject {
        if (walletConfiguration.paymentGatewayTokenizationParameters.isEmpty()) {
            throw IllegalArgumentException(
                "Please edit the Constants.java file to add gateway name and other " +
                    "parameters your processor requires"
            )
        }

        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters",
                JSONObject(walletConfiguration.paymentGatewayTokenizationParameters)
            )
        }
    }

    /**
     * Card networks supported.
     *
     * @return Allowed card networks
     * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private fun supportedCardNetworks(): Single<JSONArray> {
        return walletConfiguration.supportedNetworks()
            .map { supportedNetworks -> JSONArray(supportedNetworks) }
    }

    /**
     * Describe support for the CARD payment method.
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private fun baseCardPaymentMethod(): Single<JSONObject> {
        return supportedCardNetworks()
            .map { supportedCardNetworks ->
                JSONObject().apply {
                    val parameters = JSONObject().apply {
                        put("allowedAuthMethods", allowedCardAuthMethods)
                        put("allowedCardNetworks", supportedCardNetworks)
                        put("billingAddressRequired", true)
                        put("billingAddressParameters", JSONObject().apply {
                            put("format", "FULL")
                        })
                    }

                    put("type", "CARD")
                    put("parameters", parameters)
                }
            }
    }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     */
    override fun isReadyToPayRequest(): Single<IsReadyToPayRequest> {
        return baseCardPaymentMethod()
            .map { baseCardPaymentMethod ->

                val isReadyToPayRequest = JSONObject(baseRequest.toString())
                isReadyToPayRequest.put(
                    "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod)
                )

                IsReadyToPayRequest.fromJson(isReadyToPayRequest.toString())
            }
    }

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     */
    override fun getPaymentDataRequest(price: Price): Single<PaymentDataRequest> {
        return paymentRequestJson(price)
            .map { paymentRequestJson ->
                PaymentDataRequest.fromJson(paymentRequestJson.toString())
            }
    }

    /**
     * @see [PaymentDataRequest]
     * w(https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
     */
    private fun paymentRequestJson(price: Price): Single<JSONObject> {
        return Single.zip(
            cardPaymentMethod(), getTransactionInfo(price),
            BiFunction<JSONObject, JSONObject, JSONObject> { cardPaymentMethod, transactionInfo ->
                JSONObject(baseRequest.toString()).apply {
                    put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
                    put("transactionInfo", transactionInfo)
                    put("merchantInfo", merchantInfo)

                    put("shippingAddressRequired", true)
                    put("shippingAddressParameters", shippingAddressParameters())
                }
            }
        )
    }

    private fun shippingAddressParameters(): JSONObject {
        return JSONObject().apply {
            /*put("phoneNumberRequired", "false")

            put(
                "allowedCountryCodes",
                JSONArray().apply { put("ES") }
            )*/
        }
    }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private fun cardPaymentMethod(): Single<JSONObject> {
        return baseCardPaymentMethod()
            .map { cardPaymentMethod ->
                cardPaymentMethod.put(
                    "tokenizationSpecification", gatewayTokenizationSpecification()
                )
            }
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     * @see [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
     */
    private fun getTransactionInfo(price: Price): Single<JSONObject> {
        return walletConfiguration.storeCountryCode()
            .map { countryCode ->
                JSONObject().apply {
                    put("totalPrice", price.decimalAmount.toString())
                    put("totalPriceStatus", "ESTIMATED")
                    put("countryCode", countryCode)
                    put("currencyCode", price.currency.currencyCode)
                }
            }
    }
}

@VisibleForTesting
internal const val API_MAJOR = 2

@VisibleForTesting
internal const val API_MINOR = 0
