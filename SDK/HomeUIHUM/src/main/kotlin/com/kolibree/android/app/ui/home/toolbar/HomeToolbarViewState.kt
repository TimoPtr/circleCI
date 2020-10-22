/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class HomeToolbarViewState(
    val toolbarProfilePickerEnabled: Boolean,
    val toolbarIconResult: ToolbarIconResult = ToolbarIconResult(),
    val profileName: String = "",
    val productsInCart: Int = 0,
    val topOffset: Int = 0
) : BaseViewState {

    @VisibleForApp
    companion object {
        fun initial(toolbarProfilePickerEnabled: Boolean = false) =
            HomeToolbarViewState(toolbarProfilePickerEnabled)
    }
}
