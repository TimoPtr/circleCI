/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ScanToothbrushListViewState(
    val items: List<ScanToothbrushItemBindingModel> = emptyList(),
    val showNoBrushFound: Boolean = false
) : BaseViewState {
    fun withBlinkProgressHidden(): ScanToothbrushListViewState {
        return copy(items = items.map {
            it.copy(isBlinkProgressVisible = false, isRowClickable = true)
        })
    }

    fun withProgress(
        item: ScanToothbrushItemBindingModel,
        isBlinkProgressVisible: Boolean
    ): ScanToothbrushListViewState {
        if (items.none { it.mac == item.mac }) return this

        return copy(items = items
            .map {
                it.copy(
                    // set blinkInProgress for specified item
                    isBlinkProgressVisible = it.mac == item.mac && isBlinkProgressVisible,
                    // disallow any row click if blink progress is visible
                    isRowClickable = !isBlinkProgressVisible
                )
            }
        )
    }

    /**
     * Creates a new ViewState instance that includes
     * - items from current instance with isProgressVisible = true
     * - items from current instance that represent the current blinking connection
     * - new items in [results] that don't represent the same scan result of the points above
     *
     * The reasoning behind this is that
     * - currentBlinkingConnection is already established, thus it won't advertise
     * - any toothbrush that has a blink attempt in progress won't advertise
     *
     * If we don't keep them manually, they'll be removed from the list after cleanup event
     *
     * @return a new [ScanToothbrushListViewState] containing items to display
     */
    fun withScannedResults(
        results: List<ToothbrushScanResult>,
        currentBlinkingConnection: KLTBConnection?
    ): ScanToothbrushListViewState {
        val itemsToKeep = itemsToKeepFromCurrentViewState(currentBlinkingConnection)

        val resultsToAdd = results.filterDuplicates(itemsToKeep)

        val itemsToReturn = itemsToKeep + resultsToAdd

        return copy(items = itemsToReturn.sortedBy { it.name })
    }

    private fun List<ToothbrushScanResult>.filterDuplicates(
        itemsToKeep: List<ScanToothbrushItemBindingModel>
    ): List<ScanToothbrushItemBindingModel> {
        return map { result -> ScanToothbrushItemBindingModel(result) }
            .filter { newItem -> itemsToKeep.firstOrNull { it.mac == newItem.mac } == null }
    }

    private fun itemsToKeepFromCurrentViewState(currentBlinkingConnection: KLTBConnection?) =
        (itemRepresentingBlinkingConnection(currentBlinkingConnection) + itemsBlinking())
            .distinctBy { it.mac }

    private fun itemsBlinking() = items.filter { it.isBlinkProgressVisible }

    private fun itemRepresentingBlinkingConnection(currentBlinkingConnection: KLTBConnection?) =
        items.filter { it.mac == currentBlinkingConnection.mac() }

    companion object {
        fun initial() = ScanToothbrushListViewState()
    }
}
