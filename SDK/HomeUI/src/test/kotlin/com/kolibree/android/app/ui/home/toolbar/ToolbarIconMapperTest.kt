/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushOtaAvailable
import com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaInProgress
import com.kolibree.android.app.ui.toolbartoothbrush.ToolbarIcon
import com.kolibree.android.app.ui.toolbartoothbrush.ToolbarIconMapper
import com.kolibree.android.app.ui.toolbartoothbrush.ToolbarIconResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolbarIconMapperTest : BaseUnitTest() {

    @Test
    fun `multiToothbrushesIcon returns appropriate ToolbarIcon object`() {
        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushDisconnected
            ),
            ToolbarIconMapper.multiToothbrushesIcon(MultiToothbrushDisconnected(mockMac()))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushConnectedOta
            ),
            ToolbarIconMapper.multiToothbrushesIcon(MultiToothbrushOtaAvailable(mockMac()))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushConnected
            ),
            ToolbarIconMapper.multiToothbrushesIcon(MultiToothbrushConnected(listOf()))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushDisconnected
            ),
            ToolbarIconMapper.multiToothbrushesIcon(MultiToothbrushConnecting(mockMac()))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushDisconnected
            ),
            ToolbarIconMapper.multiToothbrushesIcon(MultiToothbrushDisconnected(mockMac()))
        )
    }

    @Test
    fun `singleToothbrushIcon returns appropriate ToolbarIconResult object`() {
        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushDisconnected,
                MAC_1
            ),
            ToolbarIconMapper.singleToothbrushIcon(SingleToothbrushDisconnected(MAC_1))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushDisconnected,
                MAC_1
            ),
            ToolbarIconMapper.singleToothbrushIcon(SingleToothbrushConnecting(MAC_1))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushConnected,
                MAC_1
            ),
            ToolbarIconMapper.singleToothbrushIcon(
                SingleToothbrushConnected(MAC_1)
            )
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushConnectedOta,
                MAC_1
            ),
            ToolbarIconMapper.singleToothbrushIcon(SingleToothbrushOtaInProgress(MAC_1))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushConnectedOta,
                MAC_1
            ),
            ToolbarIconMapper.singleToothbrushIcon(SingleToothbrushOtaAvailable(MAC_1))
        )
    }

    @Test
    fun `noneToothbrushIcon returns appropriate ToolbarIconResult object`() {
        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.NoToothbrush,
                relatedMacAddress = "mac1"
            ),
            ToolbarIconMapper.noneToothbrushIcon(NoBluetooth(0, "mac1"))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.ToothbrushDisconnected,
                relatedMacAddress = "mac2"
            ),
            ToolbarIconMapper.noneToothbrushIcon(NoBluetooth(1, "mac2"))
        )

        assertEquals(
            ToolbarIconResult(
                ToolbarIcon.MultiToothbrushDisconnected,
                relatedMacAddress = "mac3"
            ),
            ToolbarIconMapper.noneToothbrushIcon(NoBluetooth(2, "mac3"))
        )
    }

    @Test
    fun `iconObservable emits ToolbarIconResult object`() {
        val result = ToolbarIconMapper.map(NoToothbrushes(0))
        assertEquals(
            ToolbarIconResult(
                toolbarIcon = ToolbarIcon.NoToothbrush,
                relatedMacAddress = ""
            ),
            result
        )

        val result1 = ToolbarIconMapper.map(NoToothbrushes(1))
        assertEquals(
            ToolbarIconResult(
                toolbarIcon = ToolbarIcon.ToothbrushDisconnected,
                relatedMacAddress = ""
            ),
            result1
        )

        val result2 = ToolbarIconMapper.map(NoToothbrushes(2))
        assertEquals(
            ToolbarIconResult(
                toolbarIcon = ToolbarIcon.MultiToothbrushDisconnected,
                relatedMacAddress = ""
            ),
            result2
        )

        val mac = "01:02"
        val result3 = ToolbarIconMapper.map(SingleToothbrushDisconnected(mac))
        assertEquals(
            ToolbarIconResult(
                toolbarIcon = ToolbarIcon.ToothbrushDisconnected,
                relatedMacAddress = mac
            ),
            result3
        )
    }

    private fun mockMac() = listOf(
        MAC_1,
        MAC_2
    )
}

private const val MAC_1 = "mac:01"
private const val MAC_2 = "mac:02"
