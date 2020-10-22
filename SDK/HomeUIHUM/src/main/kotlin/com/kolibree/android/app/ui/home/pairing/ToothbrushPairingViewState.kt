/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.pairing.PairingViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class ToothbrushPairingViewState(
    private val pairingViewState: PairingViewState?,
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration()
) : BaseViewState {

    fun progressVisible() = pairingViewState?.progressVisible ?: false

    fun withSnackbarDismissed() = copy(snackbarConfiguration = snackbarConfiguration.copy(false))

    fun withPairingViewState(newPairingViewState: PairingViewState?): ToothbrushPairingViewState {
        val newViewState = copy(pairingViewState = newPairingViewState)

        return if (newViewState.progressVisible()) {
            newViewState.withSnackbarDismissed()
        } else {
            newViewState
        }
    }

    internal companion object {
        fun initial() = ToothbrushPairingViewState(pairingViewState = PairingViewState.initial())
    }
}
