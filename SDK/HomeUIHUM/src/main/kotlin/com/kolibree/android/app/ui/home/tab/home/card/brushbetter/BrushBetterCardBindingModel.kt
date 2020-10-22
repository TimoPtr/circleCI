/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class BrushBetterCardBindingModel(
    val viewState: BrushBetterCardViewState,
    override val layoutId: Int = R.layout.home_brush_better
) : DynamicCardBindingModel(viewState) {

    fun items() = viewState.items

    fun pulsingDotVisibility(): Int = if (viewState.pulsingDotVisible) View.VISIBLE else View.GONE
}
