/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.product

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ProductSupportCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition
) : DynamicCardViewState {

    override fun asBindingModel(): DynamicCardBindingModel = ProductSupportCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            ProductSupportCardViewState(visible = true, position = position)
    }
}
