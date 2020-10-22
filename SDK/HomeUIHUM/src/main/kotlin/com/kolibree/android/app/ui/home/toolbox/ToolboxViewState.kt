/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbox

import androidx.annotation.AttrRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class ToolboxViewState(
    val toolboxVisible: Boolean = false,
    val iconRes: Int? = null,
    val subTitle: String? = null,
    val title: String? = null,
    val body: String? = null,
    val detailsButton: String? = null,
    val confirmButton: String? = null,
    val pulsingDotVisible: Boolean = false,
    @AttrRes val titleTextAppearance: Int? = null
) : BaseViewState {

    @VisibleForApp
    companion object {
        fun fromConfiguration(configuration: ToolboxConfiguration) = ToolboxViewState(
            toolboxVisible = true,
            iconRes = configuration.iconRes,
            subTitle = configuration.subTitle,
            title = configuration.title,
            body = configuration.body,
            detailsButton = configuration.detailsButton?.text,
            confirmButton = configuration.confirmButton?.text,
            pulsingDotVisible = configuration.pulsingDotVisible,
            titleTextAppearance = configuration.titleTextAppearance
        )

        fun hidden() = ToolboxViewState()
    }
}
