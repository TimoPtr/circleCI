/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.InMemoryAddressProvider
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityModule
import com.kolibree.android.shop.data.googlewallet.GooglePayModule
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.data.repo.CartRepositoryImpl
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.presentation.checkout.CheckoutActivity
import com.kolibree.android.shop.presentation.checkout.CheckoutActivityViewModel
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.checkout.cart.ShopCartFragment
import com.kolibree.android.shop.presentation.checkout.payment.AnotherPaymentFragment
import com.kolibree.android.shop.presentation.checkout.shipping.ShippingBillingFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class CheckoutModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [CheckoutActivityModule::class])
    internal abstract fun bindCheckoutActivity(): CheckoutActivity
}

@Module(includes = [CheckoutFragmentBindingModule::class, GooglePayModule::class])
abstract class CheckoutActivityModule {

    @Binds
    internal abstract fun providesAppCompatActivity(impl: CheckoutActivity): AppCompatActivity

    internal companion object {

        @Provides
        internal fun provideActivityViewModel(
            activity: CheckoutActivity,
            viewModelFactory: CheckoutActivityViewModel.Factory
        ): CheckoutSharedViewModel {
            return ViewModelProvider(
                activity,
                viewModelFactory
            ).get(CheckoutActivityViewModel::class.java)
        }

        @Provides
        fun providesNavigator(activity: CheckoutActivity): CheckoutNavigator {
            return activity.createNavigatorAndBindToLifecycle(CheckoutNavigator::class)
        }
    }
}

@Module
abstract class CheckoutFragmentBindingModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [GooglePayAvailabilityModule::class])
    internal abstract fun contributeShopCartFragment(): ShopCartFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AnotherPaymentModule::class])
    internal abstract fun contributeAnotherPaymentFragment(): AnotherPaymentFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeShippingBillingFragment(): ShippingBillingFragment
}

@Module
object AnotherPaymentModule {
    @Provides
    internal fun providesWebViewCheckout(fragment: AnotherPaymentFragment): WebViewCheckout {
        return fragment.extraWebViewCheckout()
    }
}

@Module
abstract class CartRepositoryModule {

    @Binds
    @AppScope
    internal abstract fun bindsCartRepository(
        implementation: CartRepositoryImpl
    ): CartRepository

    @Binds
    @AppScope
    internal abstract fun bindAddressProvider(impl: InMemoryAddressProvider): AddressProvider
}
