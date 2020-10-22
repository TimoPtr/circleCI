/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import androidx.lifecycle.LiveData
import com.kolibree.android.app.Error
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import io.reactivex.Flowable

internal interface CheckoutSharedViewModel {

    val sharedViewStateLiveData: LiveData<CheckoutActivityViewState>

    val checkoutStream: Flowable<Checkout>

    val cartFlowable: Flowable<Cart>

    fun getSharedViewState(): CheckoutActivityViewState?

    fun showError(error: Error)

    fun hideError()

    fun showProgress(show: Boolean)
}
