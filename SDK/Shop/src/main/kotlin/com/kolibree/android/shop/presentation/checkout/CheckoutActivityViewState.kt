/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.shop.domain.model.Cart
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class CheckoutActivityViewState(
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration(),
    val progressVisible: Boolean = false,
    val cart: Cart? = null
) : BaseViewState {

    fun withSnackbarDismissed(): CheckoutActivityViewState =
        copy(snackbarConfiguration = snackbarConfiguration.copy(false))

    companion object {
        fun initial() = CheckoutActivityViewState()
    }
}
