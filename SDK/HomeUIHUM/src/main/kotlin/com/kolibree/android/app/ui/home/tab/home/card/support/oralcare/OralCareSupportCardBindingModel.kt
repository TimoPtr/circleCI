/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.oralcare

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class OralCareSupportCardBindingModel(
    val viewState: OralCareSupportCardViewState,
    override val layoutId: Int = R.layout.home_card_oral_care_support
) : DynamicCardBindingModel(viewState)
