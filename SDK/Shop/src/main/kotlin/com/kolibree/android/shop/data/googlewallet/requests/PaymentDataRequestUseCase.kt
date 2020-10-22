/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet.requests

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.GoogleWalletConfiguration
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.kolibree.android.shop.data.googlewallet.GoogleWalletRequestProvider
import com.kolibree.android.shop.data.googlewallet.exceptions.GooglePayIllegalResponseException
import com.kolibree.android.shop.domain.model.GoogleWalletAddress
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.kolibree.android.shop.domain.model.Price
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

internal interface PaymentDataRequestUseCase {
    /**
     * Requests a Payment Token from Google Pay with the amount in [price]
     *
     * Caller is responsible to only invoke this method once. Multiple calls will result in undefined
     * behavior
     *
     * Host Activity **must** forward its `OnActivityResult` calls to [maybeProcessActivityResult]
     *
     * @return [Maybe]<[GoogleWalletPayment]> that will
     * - emit a value on success
     * - emit errors
     * - complete if user cancelled the process
     */
    fun validatePayment(price: Price): Maybe<GoogleWalletPayment>

    /**
     * Processes the parameters to determine the result of [validatePayment]
     *
     * Host activity of whoever is subscribed to [validatePayment] must forward its `OnActivityResult`
     * methods here
     */
    fun maybeProcessActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

internal class PaymentDataRequestUseCaseImpl constructor(
    private val paymentsClient: PaymentsClient,
    private val activity: AppCompatActivity, // GooglePayClientWrapper is @ActivityScope
    private val walletRequestProvider: GoogleWalletRequestProvider,
    googleWalletConfiguration: GoogleWalletConfiguration
) : PaymentDataRequestUseCase {
    val isProductionEnvironment = googleWalletConfiguration.isProductionEnvironment

    @VisibleForTesting
    var ongoingValidation: OngoingValidation? = null

    override fun validatePayment(price: Price): Maybe<GoogleWalletPayment> {
        return walletRequestProvider.getPaymentDataRequest(price)
            .flatMapMaybe { request ->
                Maybe.create<GoogleWalletPayment> { emitter ->
                    ongoingValidation.let {
                        if (it != null) {
                            emitter.onError(
                                IllegalStateException("Already waiting on a payment validation (${it.price})")
                            )
                        } else {
                            proceedToPaymentValidation(price, emitter, request)
                        }
                    }
                }
                    .doFinally { ongoingValidation = null }
            }
    }

    private fun proceedToPaymentValidation(
        price: Price,
        emitter: MaybeEmitter<GoogleWalletPayment>,
        request: PaymentDataRequest
    ) {
        ongoingValidation = OngoingValidation(
            price = price,
            ongoingValidation = emitter
        )

        val loadPaymentData = paymentsClient.loadPaymentData(request)
        AutoResolveHelper.resolveTask(
            loadPaymentData,
            activity,
            LOAD_PAYMENT_REQUEST_CODE
        )
    }

    override fun maybeProcessActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOAD_PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> onPaymentResultOk(data)
                Activity.RESULT_CANCELED -> onPaymentResultCanceled()
                AutoResolveHelper.RESULT_ERROR -> onPaymentResultError(data)
            }
        }
    }

    private fun onPaymentResultOk(data: Intent?) {
        extractPaymentData(data)?.let(::handlePaymentSuccess)
            ?: ongoingValidation?.onError(GooglePayIllegalResponseException(message = "Result intent was null"))
    }

    private fun onPaymentResultCanceled() {
        // Nothing to do here normally - the user simply cancelled without selecting a
        // payment method.
        ongoingValidation?.onComplete()
    }

    private fun onPaymentResultError(data: Intent?) {
        val error = AutoResolveHelper.getStatusFromIntent(data)?.let { status ->
            Timber.w("loadPaymentData failed with Error code: $status")

            ApiException(status)
        } ?: IllegalStateException("Unknown error processing payment")

        ongoingValidation?.onError(error)
    }

    private fun extractPaymentData(intent: Intent?): PaymentData? =
        intent?.let { PaymentData.getFromIntent(it) }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/response-objects#PaymentData">Payment
     * Data</a>
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentDataJson = paymentData.toJson() ?: return

        try {
            ongoingValidation.takeIf { it != null && !it.isDisposed }?.let {
                it.onSuccess(paymentDataJson.toGoogleWalletPayment(it.price))
            }
                ?: Timber.w(IllegalStateException("OngoingValidation is null or disposed. We can't emit Payment info"))
        } catch (e: JSONException) {
            Timber.e(e, "Payment Error")
            ongoingValidation?.onError(
                GooglePayIllegalResponseException(
                    message = "Cant read PaymentData from $paymentDataJson",
                    cause = e
                )
            )
        }
    }

    /**
     * @see <a href="https://developers.google.com/pay/api/android/reference/response-objects#PaymentData">Payment
     * Data</a>
     */
    private fun String.toGoogleWalletPayment(price: Price): GoogleWalletPayment {
        val paymentDataJson = JSONObject(this)
        val paymentMethodDataJson = paymentDataJson.getJSONObject("paymentMethodData")

        return GoogleWalletPayment(
            amount = price,
            token = readToken(paymentMethodDataJson),
            billingAddress = readAddress(
                paymentMethodDataJson
                    .getJSONObject("info")
                    .getJSONObject("billingAddress")
            ),
            shippingAddress = readAddress(paymentDataJson.getJSONObject("shippingAddress")),
            isProductionPayment = isProductionEnvironment
        )
    }

    private fun readToken(paymentMethodData: JSONObject): String {
        return paymentMethodData
            .getJSONObject("tokenizationData")
            .getString("token")
    }

    /**g
     * @see <a href="https://developers.google.com/pay/api/android/reference/response-objects#Address">Address</a>
     */
    private fun readAddress(addressJson: JSONObject): GoogleWalletAddress {
        return GoogleWalletAddress(
            address1 = addressJson.getString("address1"),
            address2 = addressJson.getString("address2"),
            address3 = addressJson.getString("address3"),
            sortingCode = addressJson.getString("sortingCode"),
            countryCode = addressJson.getString("countryCode"),
            postalCode = addressJson.getString("postalCode"),
            name = addressJson.getString("name"),
            locality = addressJson.getString("locality"),
            administrativeArea = addressJson.getString("administrativeArea")
        )
    }
}

@VisibleForTesting
internal class OngoingValidation(
    val price: Price,
    private val ongoingValidation: MaybeEmitter<GoogleWalletPayment>
) : MaybeEmitter<GoogleWalletPayment> by ongoingValidation

@VisibleForTesting
internal const val LOAD_PAYMENT_REQUEST_CODE = 555
