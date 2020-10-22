/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.samples.wallet.GoogleWalletConfiguration
import com.google.android.gms.samples.wallet.GoogleWalletConfigurationImpl
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.shop.data.googlewallet.requests.IsReadyToPayRequestUseCase
import com.kolibree.android.shop.data.googlewallet.requests.IsReadyToPayRequestUseCaseImpl
import com.kolibree.android.shop.data.googlewallet.requests.PaymentDataRequestUseCase
import com.kolibree.android.shop.data.googlewallet.requests.PaymentDataRequestUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module(includes = [IsReadyForGooglePayModule::class])
internal abstract class GooglePayModule {
    @ActivityScope
    @Binds
    abstract fun bindsGooglePayClientWrapper(impl: GooglePayClientFacade): GooglePayClientWrapper

    companion object {

        @ActivityScope
        @Provides
        fun providesPaymentDataRequestUseCase(
            activity: AppCompatActivity,
            walletConfiguration: GoogleWalletConfiguration
        ): PaymentDataRequestUseCase {
            return PaymentDataRequestUseCaseImpl(
                activity = activity,
                paymentsClient = paymentsClient(
                    activity,
                    googleWalletOptionsProvider(walletConfiguration)
                ),
                walletRequestProvider = googleWalletRequestProvider(walletConfiguration),
                googleWalletConfiguration = walletConfiguration
            )
        }
    }
}

@Module
internal abstract class GooglePayConfigurationModule {
    @Binds
    abstract fun bindsGoogleWalletConfiguration(impl: GoogleWalletConfigurationImpl): GoogleWalletConfiguration

    companion object {
        @Provides
        @GooglePayTheme
        fun providesGooglePayTheme(): Int = WalletConstants.THEME_LIGHT
    }
}

@Module(includes = [GooglePayConfigurationModule::class])
internal object IsReadyForGooglePayModule {
    @Provides
    fun providesIsReadyToPayRequestUseCase(
        activity: AppCompatActivity,
        walletConfiguration: GoogleWalletConfiguration
    ): IsReadyToPayRequestUseCase {
        return IsReadyToPayRequestUseCaseImpl(
            paymentsClient = paymentsClient(
                activity,
                googleWalletOptionsProvider(walletConfiguration)
            ),
            walletRequestProvider = googleWalletRequestProvider(walletConfiguration)
        )
    }
}

@Module(includes = [IsReadyForGooglePayModule::class])
internal abstract class GooglePayAvailabilityModule {

    @Binds
    abstract fun bindsGooglePayAvailabilityWatcher(impl: GooglePayAvailabilityUseCaseImpl): GooglePayAvailabilityUseCase
}

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class GooglePayTheme

/*
Build dependencies manually to avoid forcing app to declare a implementation dependency on
Google Wallet

If any Google Wallet Type is exposed in a Module or in an Injected constructor, the dagger
generated code living in app/build/ needs the dependency to compile
 */
private fun googleWalletOptionsProvider(
    walletConfiguration: GoogleWalletConfiguration
): GoogleWalletOptionsProvider = GoogleWalletOptionsProviderImpl(walletConfiguration)

private fun paymentsClient(
    activity: AppCompatActivity,
    googleWalletOptionsProvider: GoogleWalletOptionsProvider
): PaymentsClient =
    Wallet.getPaymentsClient(activity, googleWalletOptionsProvider.provide())

private fun googleWalletRequestProvider(
    googleWalletConfiguration: GoogleWalletConfiguration
): GoogleWalletRequestProvider = GoogleWalletRequestProviderImpl(googleWalletConfiguration)
