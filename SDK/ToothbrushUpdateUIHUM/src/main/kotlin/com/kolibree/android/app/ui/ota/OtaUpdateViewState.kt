/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class OtaUpdateViewState(
    val snackbarConfiguration: SnackbarConfiguration? = null,
    val progressVisible: Boolean = false
) : BaseViewState {

    companion object {
        fun initial() = OtaUpdateViewState()
    }

    fun withSnackbarDismissed(): OtaUpdateViewState = copy(snackbarConfiguration = null)
}
