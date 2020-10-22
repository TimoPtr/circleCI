/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.mutation.mapper

import com.kolibree.android.shop.domain.model.Address
import com.shopify.buy3.Storefront.MailingAddressInput

/**
 * Map a [Address] to it's internal Shopify representation -> [MailingAddressInput]
 */
internal object ShopifyAddressMapper : (Address) -> MailingAddressInput {

    override fun invoke(address: Address) = MailingAddressInput().apply {
        firstName = address.firstName
        lastName = address.lastName
        company = address.company
        address1 = address.street
        city = address.city
        country = address.country
        zip = address.postalCode
        province = address.province
        phone = address.phoneNumber
    }
}
