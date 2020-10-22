/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import android.os.Parcelable
import androidx.annotation.StringRes
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.shop.R
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Address.Input
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.Address.Input.PROVINCE
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ShippingBillingViewState(
    val shippingAddress: Address = Address.empty(),
    val billingAddress: Address = Address.empty(),
    val isBillingSameAsShipping: Boolean = false,
    val formError: FormError = FormError()
) : BaseViewState {

    fun withAddresses(
        newShippingAddress: Address? = null,
        newBillingAddress: Address? = null,
        newBillingSameAsShipping: Boolean? = null
    ): ShippingBillingViewState = copy(
        shippingAddress = newShippingAddress ?: shippingAddress,
        billingAddress = newBillingAddress ?: billingAddress,
        isBillingSameAsShipping = newBillingSameAsShipping ?: isBillingSameAsShipping
    )

    fun withErrorActivated(): ShippingBillingViewState =
        copy(formError = formError.copy(isErrorActivated = true))

    /**
     * Get the error message associated by the [Input] is there is any error for this one
     */
    @StringRes
    fun getError(addressType: AddressType, input: Input): Int? = when (addressType) {
        AddressType.SHIPPING -> formError.getError(shippingAddress.getValue(input), input)
        AddressType.BILLING -> formError.getError(billingAddress.getValue(input), input)
    }

    /**
     * Returns the first [Input] considered as an error, or `null` if there is no Error on screen
     */
    fun getFirstError(): Pair<AddressType, Input>? {
        shippingAddress.getInputErrors().firstOrNull()?.let { inputError ->
            return AddressType.SHIPPING to inputError
        }

        billingAddress.getInputErrors().firstOrNull()?.let { inputError ->
            return AddressType.BILLING to inputError
        }

        return null
    }

    @IgnoredOnParcel
    val isBillingVisible = !isBillingSameAsShipping

    fun isShippingValid(): Boolean = shippingAddress.hasAllMandatoryFields()

    fun isBillingValid(): Boolean =
        isBillingSameAsShipping || billingAddress.hasAllMandatoryFields()
}

@Parcelize
internal data class FormError(val isErrorActivated: Boolean = false) : Parcelable {

    @StringRes
    fun getError(value: String?, input: Input): Int? {
        return if (isErrorActivated && value.isNullOrEmpty()) mapInputToError(input) else null
    }

    private fun mapInputToError(input: Input) = when (input) {
        FIRST_NAME -> R.string.form_shipping_first_name_error
        LAST_NAME -> R.string.form_shipping_last_name_error
        ADDRESS_LINE_1 -> R.string.form_shipping_address_error
        CITY -> R.string.form_shipping_city_error
        POSTAL_CODE -> R.string.form_shipping_postal_code_error
        COUNTRY -> R.string.form_shipping_country_error
        PROVINCE -> R.string.form_shipping_province_error
    }
}

internal enum class AddressType {
    SHIPPING,
    BILLING
}
