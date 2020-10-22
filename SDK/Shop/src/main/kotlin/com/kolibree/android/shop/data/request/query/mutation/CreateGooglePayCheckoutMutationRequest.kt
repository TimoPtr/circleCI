/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.mutation

import androidx.annotation.VisibleForTesting
import com.kolibree.android.shop.data.request.GraphClientMutationRequest
import com.kolibree.android.shop.data.request.query.error.ShopifyCheckoutUserError
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CheckoutCompleteWithTokenizedPaymentPayloadQuery
import com.shopify.buy3.Storefront.CheckoutQuery
import com.shopify.buy3.Storefront.PaymentQuery
import com.shopify.graphql.support.ID
import java.util.UUID
import timber.log.Timber

/**
 * @see [Google Pay Checkout](https://github.com/Shopify/mobile-buy-sdk-android#google-pay-checkout-)
 */
internal class CreateGooglePayCheckoutMutationRequest
@VisibleForTesting constructor(
    requestedCheckout: Checkout,
    googlePayment: GoogleWalletPayment,
    uuid: UUID
) : GraphClientMutationRequest<GooglePayCheckout>() {

    constructor(
        requestedCheckout: Checkout,
        googlePayment: GoogleWalletPayment
    ) : this(requestedCheckout, googlePayment, UUID.randomUUID())

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { mutationQuery ->
            val idempotencyKey: String = uuid.toString()

            val billingAddressInput: Storefront.MailingAddressInput =
                googlePayment.toMailingAddressInput()

            val tokenizedPaymentInput = Storefront.TokenizedPaymentInput(
                googlePayment.amount.decimalAmount,
                idempotencyKey,
                billingAddressInput,
                GOOGLE_PAY_KEY,
                googlePayment.token
            )
                .apply {
                    test = !googlePayment.isProductionPayment
                }

            mutationQuery.checkoutCompleteWithTokenizedPayment(
                ID(requestedCheckout.checkoutId), tokenizedPaymentInput
            ) { payloadQuery: CheckoutCompleteWithTokenizedPaymentPayloadQuery ->
                payloadQuery
                    .payment { paymentQuery: PaymentQuery ->
                        paymentQuery
                            .ready()
                            .errorMessage()
                    }
                    .checkout { checkoutQuery: CheckoutQuery ->
                        checkoutQuery
                            .ready()
                    }
                    .checkoutUserErrors { userErrorQuery: Storefront.CheckoutUserErrorQuery ->
                        userErrorQuery
                            .field()
                            .message()
                    }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> GooglePayCheckout = { mutation ->
        if (mutation.checkoutCreate.checkoutUserErrors.isNotEmpty()) {
            throw ShopifyCheckoutUserError(mutation.checkoutCreate.checkoutUserErrors)
        } else {
            val checkout = mutation.checkoutCreate.checkout

            Timber.d("Google Pay Received checkout $checkout")

            GooglePayCheckout(
                orderId = checkout.order,
                checkout = checkout,
                cart = requestedCheckout.cart
            )
        }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutCreate?.checkoutUserErrors?.isEmpty() ?: false
    }
}

private const val GOOGLE_PAY_KEY = "google_pay"

@VisibleForTesting
internal fun GoogleWalletPayment.toMailingAddressInput(): Storefront.MailingAddressInput {
    return Storefront.MailingAddressInput().apply {
        address1 = billingAddress.address1
        address2 = billingAddress.address2
        country = billingAddress.countryCode
        zip = billingAddress.postalCode
        firstName = billingAddress.name
        province = billingAddress.administrativeArea
        city = billingAddress.locality
    }
}
