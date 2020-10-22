/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel
import org.threeten.bp.OffsetDateTime

@VisibleForApp
sealed class SelectProfileItem(
    open val isSelected: Boolean
) : ItemBindingModel, Parcelable

@Parcelize
internal data class ProfileItem(
    val profileId: Long,
    val profileName: String,
    val profileAvatarUrl: String?,
    val creationDate: OffsetDateTime,
    override val isSelected: Boolean = false
) : SelectProfileItem(isSelected) {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_select_profile)
    }
}

@Parcelize
internal data class AddProfileItem(
    override val isSelected: Boolean = false
) : SelectProfileItem(isSelected) {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_add_profile_item)
    }
}
