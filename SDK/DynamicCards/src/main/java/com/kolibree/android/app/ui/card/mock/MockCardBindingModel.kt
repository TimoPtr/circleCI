/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import android.annotation.SuppressLint
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.dynamiccards.R
import kotlinx.android.parcel.Parcelize

@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class MockCardBindingModel(
    val data: MockCardViewState,
    override val layoutId: Int = R.layout.item_home_card_mock
) : DynamicCardBindingModel(data)
