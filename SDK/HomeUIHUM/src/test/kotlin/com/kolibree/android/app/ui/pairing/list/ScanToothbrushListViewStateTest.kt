/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.android.test.mocks.fakeScanResult
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test

class ScanToothbrushListViewStateTest : BaseUnitTest() {
    /*
    withBlinkProgressHidden
     */
    @Test
    fun `withBlinkProgressHidden returns viewState with all items isBlinkProgressVisible = false and isRowClickable = true`() {
        val viewState = viewState(
            listOf(
                scanToothbrushItem(
                    macAndName = MAC_1,
                    isBlinkProgressVisible = true,
                    isRowClickable = false
                ),
                scanToothbrushItem(macAndName = MAC_2, isRowClickable = false)
            )
        )

        val expectedViewState = ScanToothbrushListViewState(
            listOf(
                viewState.items.first().copy(isBlinkProgressVisible = false, isRowClickable = true),
                viewState.items.last().copy(isBlinkProgressVisible = false, isRowClickable = true)
            )
        )

        assertEquals(
            expectedViewState,
            viewState.withBlinkProgressHidden()
        )
    }

    /*
    withProgress
     */
    @Test
    fun `withProgress returns viewState with isBlinkProgressVisible = true and isRowClickable = false when isBlinkProgressVisible is true`() {
        val viewState = viewStateWithTwoItems()

        val itemInProgress = viewState.items.first()

        val expectedViewState = ScanToothbrushListViewState(
            listOf(
                itemInProgress.copy(isBlinkProgressVisible = true, isRowClickable = false),
                viewState.items.last().copy(isRowClickable = false)
            )
        )

        assertEquals(
            expectedViewState,
            viewState.withProgress(itemInProgress, true)
        )
    }

    @Test
    fun `withProgress returns viewState with all items with isRowClickable = true when isBlinkProgressVisible is false`() {
        val viewState = viewStateWithTwoItems()

        val itemInProgress = viewState.items.first()

        val expectedViewState = ScanToothbrushListViewState(
            listOf(
                itemInProgress.copy(isBlinkProgressVisible = false, isRowClickable = true),
                viewState.items.last().copy(isRowClickable = true)
            )
        )

        assertEquals(
            expectedViewState,
            viewState.withProgress(itemInProgress, false)
        )
    }

    @Test
    fun `withProgress returns same instance if there's no item with the same mac`() {
        val viewState = viewStateWithTwoItems()

        assertEquals(
            viewState,
            viewState.withProgress(scanToothbrushItem(macAndName = "random"), true)
        )
    }

    /*
    withScannedResults
     */
    @Test
    fun `withScannedResults adds all results to the ViewState when current instance is empty`() {
        val viewState = viewState()
        assertTrue(viewState.items.isEmpty())

        val results = listOf(fakeScanResult(mac = "1"), fakeScanResult(mac = "2"))

        val expectedViewState =
            viewState().copy(items = results.map { ScanToothbrushItemBindingModel(it) })

        assertEquals(expectedViewState, viewState.withScannedResults(results, null))
    }

    @Test
    fun `withScannedResults returns instance with results parameter when no current item is blinking or represents blinkingConnection`() {
        val viewState =
            viewState(listOf(scanToothbrushItem(macAndName = "3", isBlinkProgressVisible = false)))

        val results = listOf(fakeScanResult(mac = "1"), fakeScanResult(mac = "2"))

        val expectedViewState =
            viewState().copy(items = results.map { ScanToothbrushItemBindingModel(it) })

        assertEquals(expectedViewState, viewState.withScannedResults(results, null))
    }

    @Test
    fun `withScannedResults returns instance with results parameter plus current blinking item`() {
        val blinkingItem = scanToothbrushItem(macAndName = "3", isBlinkProgressVisible = true)
        val viewState =
            viewState(
                listOf(
                    blinkingItem,
                    scanToothbrushItem(macAndName = "4", isBlinkProgressVisible = false)
                )
            )

        val results = listOf(fakeScanResult(mac = "1"), fakeScanResult(mac = "2"))

        val expectedItems = results.map { ScanToothbrushItemBindingModel(it) } + blinkingItem
        val expectedViewState = viewState().copy(items = expectedItems.sortedBy { it.name })

        assertEquals(expectedViewState, viewState.withScannedResults(results, null))
    }

    @Test
    fun `withScannedResults returns instance with results parameter plus item representing blinkingConnection`() {
        val blinkingConnectionMac = "blinkiing mac"
        val connectionItem =
            scanToothbrushItem(macAndName = blinkingConnectionMac, isBlinkProgressVisible = false)
        val viewState =
            viewState(
                listOf(
                    connectionItem,
                    scanToothbrushItem(macAndName = "4", isBlinkProgressVisible = false)
                )
            )

        val results = listOf(fakeScanResult(mac = "1"), fakeScanResult(mac = "2"))

        val expectedItems = results.map { ScanToothbrushItemBindingModel(it) } + connectionItem
        val expectedViewState = viewState().copy(items = expectedItems.sortedBy { it.name })

        assertEquals(
            expectedViewState,
            viewState.withScannedResults(results, mockConnection(blinkingConnectionMac))
        )
    }

    @Test
    fun `withScannedResults does not return duplicates, even if a new result represents blinkingConnection`() {
        val blinkingConnectionMac = "blinkiing mac"
        val connectionItem =
            scanToothbrushItem(macAndName = blinkingConnectionMac, isBlinkProgressVisible = false)
        val viewState =
            viewState(
                listOf(
                    connectionItem,
                    scanToothbrushItem(macAndName = "4", isBlinkProgressVisible = false)
                )
            )

        val duplicateScanResult = fakeScanResult(mac = blinkingConnectionMac)
        val nonDuplicateScanResult = fakeScanResult(mac = "2")
        val results = listOf(duplicateScanResult, nonDuplicateScanResult)

        val expectedItems = listOf(
            ScanToothbrushItemBindingModel(nonDuplicateScanResult),
            connectionItem
        )
        val expectedViewState = viewState().copy(items = expectedItems.sortedBy { it.name })

        assertEquals(
            expectedViewState,
            viewState.withScannedResults(results, mockConnection(blinkingConnectionMac))
        )
    }

    @Test
    fun `withScannedResults does not return duplicates, even if a new result represents a blinking result`() {
        val blinkingMac = "blinkiing mac"
        val blinkingItem =
            scanToothbrushItem(macAndName = blinkingMac, isBlinkProgressVisible = true)
        val viewState =
            viewState(
                listOf(
                    blinkingItem,
                    scanToothbrushItem(macAndName = "4", isBlinkProgressVisible = false)
                )
            )

        val duplicateScanResult = fakeScanResult(mac = blinkingMac)
        val nonDuplicateScanResult = fakeScanResult(mac = "2")
        val results = listOf(duplicateScanResult, nonDuplicateScanResult)

        val expectedViewState =
            viewState().copy(
                items = listOf(
                    ScanToothbrushItemBindingModel(nonDuplicateScanResult),
                    blinkingItem
                )
            )

        assertEquals(
            expectedViewState,
            viewState.withScannedResults(results, mockConnection(blinkingMac))
        )
    }

    /*
    Utils
     */
    private fun viewState(
        items: List<ScanToothbrushItemBindingModel> = listOf()
    ): ScanToothbrushListViewState {
        return ScanToothbrushListViewState(items)
    }

    private fun viewStateWithTwoItems() = viewState(
        listOf(
            scanToothbrushItem(macAndName = MAC_1),
            scanToothbrushItem(macAndName = MAC_2)
        )
    )

    private fun mockConnection(mac: String) = KLTBConnectionBuilder.createAndroidLess()
        .withMac(mac)
        .build()
}

internal fun scanToothbrushItem(
    macAndName: String = DEFAULT_MAC,
    isRowClickable: Boolean = true,
    isBlinkProgressVisible: Boolean = false
): ScanToothbrushItemBindingModel {
    return ScanToothbrushItemBindingModel(
        toothbrushScanResult = fakeScanResult(mac = macAndName, name = macAndName),
        isRowClickable = isRowClickable,
        isBlinkProgressVisible = isBlinkProgressVisible
    )
}

private const val MAC_1 = "1"
private const val MAC_2 = "2"
