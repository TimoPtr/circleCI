/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import android.os.Parcelable
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SmilesHistoryViewState(
    private val itemsResources: List<SmilesHistoryListItem> = emptyList(),
    val profileSmilesItemsAvailableForTransfer: List<ProfileSmilesItemResources> = emptyList()
) : BaseViewState {

    @IgnoredOnParcel
    val itemsWithHeader: List<SmilesHistoryListItem> = itemsResources.toMutableList().apply {
        add(0, SimilesHistoryHeaderListItem)
    }.distinct()

    fun isEmpty(): Boolean = itemsWithHeader.size == 1
}

/**
 * This interface it's just use to mixed ItemResources and Header in the recyclerView
 */
internal interface SmilesHistoryListItem : Parcelable

// This object will be use as a placeholder into recyclerView
@Parcelize
internal object SimilesHistoryHeaderListItem : SmilesHistoryListItem
