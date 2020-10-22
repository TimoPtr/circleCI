/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.UseTestShopFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.shop.credentials.TestShop
import com.kolibree.android.shop.credentials.shopForLocale
import com.kolibree.android.shop.data.configuration.ShopifyCredentials
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.crypto.extractHexToByteArray
import dagger.Module
import dagger.Provides
import java.util.Locale
import timber.log.Timber

@Module
class ShopifyCredentialsModule {

    @Provides
    fun provideShopifyCredentials(
        context: Context,
        locale: Locale,
        featureToggles: FeatureToggleSet,
        kolibreeGuard: KolibreeGuard
    ): ShopifyCredentials {
        try {
            val shop = if (featureToggles.toggleForFeature(UseTestShopFeature).value) TestShop
            else shopForLocale(locale)
            return ShopifyCredentials(
                shopDomain = context.getString(shop.domain),
                shopApiKey = kolibreeGuard.reveal(
                    context.getString(shop.encryptedKey),
                    context.getString(shop.iv).extractHexToByteArray()
                )
            )
        } catch (e: RuntimeException) {
            Timber.e(e)
            throw IllegalArgumentException("Shopify credentials could not be decrypted!", e)
        }
    }
}
