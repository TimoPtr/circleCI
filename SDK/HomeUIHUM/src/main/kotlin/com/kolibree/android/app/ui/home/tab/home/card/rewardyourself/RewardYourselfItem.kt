/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import android.os.Parcelable
import com.kolibree.android.shop.domain.model.Price
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class RewardYourselfItem(
    val id: String,
    val imageUrl: String?,
    val name: String,
    val price: Price
) : Parcelable
