/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import com.google.android.gms.samples.wallet.GoogleWalletConfiguration
import com.google.android.gms.wallet.Wallet

/**
 * Provider of [com.google.android.gms.wallet.Wallet.WalletOptions]
 */
internal interface GoogleWalletOptionsProvider {
    fun provide(): Wallet.WalletOptions
}

internal class GoogleWalletOptionsProviderImpl
constructor(
    private val walletConfiguration: GoogleWalletConfiguration
) : GoogleWalletOptionsProvider {

    override fun provide(): Wallet.WalletOptions = Wallet.WalletOptions.Builder()
        .setEnvironment(walletConfiguration.paymentsEnvironment)
        .setTheme(walletConfiguration.theme)
        .build()
}
