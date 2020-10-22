/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.Address.Input.PROVINCE
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class Address(
    val firstName: String? = null,
    val lastName: String? = null,
    val company: String? = null,
    val street: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    // TODO remove this after non-US checkout is integrated
    val country: String? = DEFAULT_COUNTRY,
    val province: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
) : Parcelable {

    fun isEmpty(): Boolean = this == empty()

    fun hasAllMandatoryFields(): Boolean =
        getInputErrors().isEmpty()

    internal fun getValue(input: Input): String? = when (input) {
        FIRST_NAME -> firstName
        LAST_NAME -> lastName
        ADDRESS_LINE_1 -> street
        CITY -> city
        POSTAL_CODE -> postalCode
        COUNTRY -> country
        PROVINCE -> province
    }

    /**
     * Returns a [List] of [Input] containing an error. For now an error is represented by an empty
     * field or a null value
     */
    internal fun getInputErrors(): List<Input> {
        return addressInputs.filter { input -> getValue(input).isNullOrEmpty() }
    }

    companion object {

        fun empty() = Address()

        internal val addressInputs = listOf(
            FIRST_NAME, LAST_NAME, ADDRESS_LINE_1, CITY, POSTAL_CODE, COUNTRY, PROVINCE
        )

        // TODO remove this after non-US checkout is integrated
        const val DEFAULT_COUNTRY = "United States"
    }

    internal enum class Input {
        FIRST_NAME,
        LAST_NAME,
        ADDRESS_LINE_1,
        CITY,
        POSTAL_CODE,
        COUNTRY,
        PROVINCE
    }
}
