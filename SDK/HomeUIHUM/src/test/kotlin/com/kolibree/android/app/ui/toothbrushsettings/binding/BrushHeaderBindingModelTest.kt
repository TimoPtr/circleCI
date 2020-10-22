/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.toothbrushsettings.ConnectionState
import com.kolibree.android.homeui.hum.R
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BrushHeaderBindingModelTest : BaseUnitTest() {

    @Test
    fun `when connection is active then toothbrushConnectionStatus returns connected toothbrush name `() {
        val tbName = "TB_name"
        val tbConnected = "$tbName connected"
        val context = mock<Context>()
        whenever(context.getString(R.string.tb_settings_connected, tbName))
            .thenReturn(tbConnected)

        val bindingModel = BrushHeaderBindingModel(
            name = tbName,
            lastSyncDate = null,
            connectionState = ConnectionState.CONNECTED,
            optionalOtaAvailable = false,
            mandatoryOtaAvailable = false
        )
        assertEquals(tbConnected, bindingModel.toothbrushConnectionStatus(context))
    }

    @Test
    fun `when connection is not active then toothbrushConnectionStatus returns connecting`() {
        val tbName = "TB_name"
        val tbConnecting = "connecting"
        val context = mock<Context>()
        whenever(context.getString(R.string.tb_settings_connecting))
            .thenReturn(tbConnecting)

        val bindingModel = BrushHeaderBindingModel(
            name = tbName,
            lastSyncDate = null,
            connectionState = ConnectionState.CONNECTING,
            optionalOtaAvailable = false,
            mandatoryOtaAvailable = false
        )
        assertEquals(tbConnecting, bindingModel.toothbrushConnectionStatus(context))
    }

    @Test
    fun `connecting animation is on if toothbrush is not connected`() {
        val bindingModel = bindingModel(ConnectionState.CONNECTING)
        assertTrue(bindingModel.isConnectingAnimationOn())
    }

    @Test
    fun `connecting animation is off if toothbrush is connected`() {
        val bindingModel = bindingModel(ConnectionState.CONNECTED)
        assertFalse(bindingModel.isConnectingAnimationOn())
    }

    @Test
    fun `connected icon is visible if toothbrush is connected`() {
        val bindingModel = bindingModel(ConnectionState.CONNECTED)
        assertTrue(bindingModel.isConnectedIconVisible())
    }

    @Test
    fun `connected icon is not visible if toothbrush is not connected`() {
        val connectingBindingModel = bindingModel(ConnectionState.CONNECTING)
        assertFalse(connectingBindingModel.isConnectedIconVisible())

        val disconnectedBindingModel = bindingModel(ConnectionState.DISCONNECTED)
        assertFalse(disconnectedBindingModel.isConnectedIconVisible())
    }

    @Test
    fun `last sync date is visible if toothbrush is connected`() {
        val bindingModel = bindingModel(ConnectionState.CONNECTED)
        assertTrue(bindingModel.isLastSyncDateVisible())
    }

    @Test
    fun `last sync date is not visible if toothbrush is not connected`() {
        val connectingBindingModel = bindingModel(ConnectionState.CONNECTING)
        assertFalse(connectingBindingModel.isLastSyncDateVisible())

        val disconnectedBindingModel = bindingModel(ConnectionState.DISCONNECTED)
        assertFalse(disconnectedBindingModel.isLastSyncDateVisible())
    }

    @Test
    fun `waiting text is visible if toothbrush is connecting`() {
        val connectingBindingModel = bindingModel(ConnectionState.CONNECTING)
        assertTrue(connectingBindingModel.isWaitingVisible())
    }

    @Test
    fun `waiting text is not visible if toothbrush is not connecting`() {
        val connectedBindingModel = bindingModel(ConnectionState.CONNECTED)
        assertFalse(connectedBindingModel.isWaitingVisible())

        val disconnectedBindingModel = bindingModel(ConnectionState.DISCONNECTED)
        assertFalse(disconnectedBindingModel.isWaitingVisible())
    }

    @Test
    fun `not connecting button is visible if toothbrush is disconnected`() {
        val bindingModel = bindingModel(ConnectionState.DISCONNECTED)
        assertTrue(bindingModel.isNotConnectingVisible())
    }

    @Test
    fun `not connecting button is not visible if toothbrush is connected or connecting`() {
        val connectedBindingModel = bindingModel(ConnectionState.CONNECTED)
        assertFalse(connectedBindingModel.isNotConnectingVisible())

        val connectingBindingModel = bindingModel(ConnectionState.CONNECTING)
        assertFalse(connectingBindingModel.isNotConnectingVisible())
    }

    private fun bindingModel(connectionState: ConnectionState) = BrushHeaderBindingModel(
        name = "name",
        lastSyncDate = null,
        connectionState = connectionState,
        optionalOtaAvailable = false,
        mandatoryOtaAvailable = false
    )
}
