/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.offlinebrushings.ExtractionProgress.Companion.MAX_PROGRESS
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LastBrushingCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val items: List<BrushingCardData>,
    val selectedItem: BrushingCardData,
    val shouldRender: Boolean,
    val offlineBrushingSyncProgress: Float?,
    val pulsingDotVisible: Boolean
) : DynamicCardViewState {

    override fun asBindingModel() = LastBrushingCardBindingModel(this)

    fun withSelectedPosition(selectedPosition: Int): LastBrushingCardViewState {
        val newItems = createListWithSelectedItem(items, selectedPosition)
        return copy(items = newItems, selectedItem = newItems.first { it.isSelected })
    }

    val offlineBrushingSyncProgressInt
        get() = offlineBrushingSyncProgress?.let { (it * 100).toInt() } ?: 0

    fun withItems(
        items: Iterable<BrushingCardData>,
        selectedPosition: Int
    ): LastBrushingCardViewState {
        val safeSelectedPosition = if (selectedPosition >= items.count())
            items.count() - 1 // Last item has been deleted, we keep on showing the last one
        else
            selectedPosition
        val newItems = createListWithSelectedItem(items, safeSelectedPosition)
        return copy(items = newItems, selectedItem = newItems.first { it.isSelected })
    }

    private fun createListWithSelectedItem(
        items: Iterable<BrushingCardData>,
        selectedPosition: Int
    ) = items.mapIndexed { position: Int, bindingModel: BrushingCardData ->
        bindingModel.copy(isSelected = position == selectedPosition)
    }

    val isOfflineBrushingSyncing: Boolean
        get() = offlineBrushingSyncProgress != null && offlineBrushingSyncProgress < MAX_PROGRESS

    companion object {

        fun initial(position: DynamicCardPosition) =
            LastBrushingCardViewState(
                visible = true,
                position = position,
                shouldRender = false,
                items = emptyList(),
                selectedItem = BrushingCardData.empty(),
                offlineBrushingSyncProgress = null,
                pulsingDotVisible = false
            )
    }
}
