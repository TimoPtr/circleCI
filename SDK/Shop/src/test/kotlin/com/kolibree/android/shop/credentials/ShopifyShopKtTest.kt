/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.credentials

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.R
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ShopifyShopKtTest : BaseUnitTest() {

    @Test
    fun `shopForLocale returns US creds for US locale`() {
        val shop = shopForLocale(Locale.US)
        assertEquals(R.string.shopify_us_domain_name, shop.domain)
        assertEquals(R.string.shopify_us_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_us_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns UK creds for UK locale`() {
        val shop = shopForLocale(Locale.UK)
        assertEquals(R.string.shopify_uk_domain_name, shop.domain)
        assertEquals(R.string.shopify_uk_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_uk_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns FR creds for FR locale`() {
        val shop = shopForLocale(Locale.FRANCE)
        assertEquals(R.string.shopify_fr_domain_name, shop.domain)
        assertEquals(R.string.shopify_fr_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_fr_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns EU creds for DE locale`() {
        val shop = shopForLocale(Locale.GERMANY)
        assertEquals(R.string.shopify_eu_domain_name, shop.domain)
        assertEquals(R.string.shopify_eu_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_eu_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns EU creds for AT locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("de-AT"))
        assertEquals(R.string.shopify_eu_domain_name, shop.domain)
        assertEquals(R.string.shopify_eu_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_eu_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns EU creds for ES locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("es-ES"))
        assertEquals(R.string.shopify_eu_domain_name, shop.domain)
        assertEquals(R.string.shopify_eu_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_eu_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns EU creds for NL locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("nl-NL"))
        assertEquals(R.string.shopify_eu_domain_name, shop.domain)
        assertEquals(R.string.shopify_eu_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_eu_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns EU creds for IT locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("it-IT"))
        assertEquals(R.string.shopify_eu_domain_name, shop.domain)
        assertEquals(R.string.shopify_eu_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_eu_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns SE creds for SE locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("sv-SE"))
        assertEquals(R.string.shopify_se_domain_name, shop.domain)
        assertEquals(R.string.shopify_se_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_se_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns CH creds for CH locale`() {
        val shop = shopForLocale(Locale.forLanguageTag("de-CH"))
        assertEquals(R.string.shopify_ch_domain_name, shop.domain)
        assertEquals(R.string.shopify_ch_api_key, shop.encryptedKey)
        assertEquals(R.string.shopify_ch_api_key_iv, shop.iv)
    }

    @Test
    fun `shopForLocale returns US creds as a fallback for other countries`() {
        assertEquals(AmericanShop, shopForLocale(Locale.forLanguageTag("pl-PL")))
        assertEquals(AmericanShop, shopForLocale(Locale.forLanguageTag("jp-JP")))
        assertEquals(AmericanShop, shopForLocale(Locale.KOREA))
        assertEquals(AmericanShop, shopForLocale(Locale.CHINESE))
    }
}
