/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.credentials

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.kolibree.android.shop.R
import java.util.Locale

internal sealed class ShopifyShop(
    @StringRes val domain: Int,
    @StringRes val encryptedKey: Int,
    @StringRes val iv: Int
)

internal object TestShop : ShopifyShop(
    domain = R.string.shopify_test_domain_name,
    encryptedKey = R.string.shopify_test_api_key,
    iv = R.string.shopify_test_api_key_iv
)

internal object AmericanShop : ShopifyShop(
    domain = R.string.shopify_us_domain_name,
    encryptedKey = R.string.shopify_us_api_key,
    iv = R.string.shopify_us_api_key_iv
)

internal object DefaultEuropeanShop : ShopifyShop(
    domain = R.string.shopify_eu_domain_name,
    encryptedKey = R.string.shopify_eu_api_key,
    iv = R.string.shopify_eu_api_key_iv
)

internal object SwedishShop : ShopifyShop(
    domain = R.string.shopify_se_domain_name,
    encryptedKey = R.string.shopify_se_api_key,
    iv = R.string.shopify_se_api_key_iv
)

internal object FrenchShop : ShopifyShop(
    domain = R.string.shopify_fr_domain_name,
    encryptedKey = R.string.shopify_fr_api_key,
    iv = R.string.shopify_fr_api_key_iv
)

internal object SwissShop : ShopifyShop(
    domain = R.string.shopify_ch_domain_name,
    encryptedKey = R.string.shopify_ch_api_key,
    iv = R.string.shopify_ch_api_key_iv
)

internal object BritishShop : ShopifyShop(
    domain = R.string.shopify_uk_domain_name,
    encryptedKey = R.string.shopify_uk_api_key,
    iv = R.string.shopify_uk_api_key_iv
)

@SuppressLint("DefaultLocale")
internal fun shopForLocale(locale: Locale): ShopifyShop = when (locale.country.toLowerCase()) {
    "de", "es", "nl", "it", "at" -> DefaultEuropeanShop
    "se" -> SwedishShop
    "fr" -> FrenchShop
    "ch" -> SwissShop
    "uk", "gb" -> BritishShop
    "us" -> AmericanShop
    else -> AmericanShop
}
