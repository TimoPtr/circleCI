/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import android.os.Parcelable
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

internal interface ToothbrushItemBindingModel : ItemBindingModel, Parcelable

@Parcelize
internal data class ScanToothbrushItemBindingModel(
    val toothbrushScanResult: ToothbrushScanResult,
    val isBlinkProgressVisible: Boolean = false,
    val isRowClickable: Boolean = true
) : ToothbrushItemBindingModel {
    @IgnoredOnParcel
    val name: String = toothbrushScanResult.name

    @IgnoredOnParcel
    val mac: String = toothbrushScanResult.mac

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_scan_toothbrush)
    }
}
