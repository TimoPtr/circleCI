package com.kolibree.android.app.ui.toolbartoothbrush.legacy.state

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ToolbarStateTest : BaseUnitTest() {

    @Test
    fun testState_NoToothbrush() {
        val state = NoToothbrush
        assertFalse(state.isLastSyncVisible())
        assertFalse(state.isSyncingProgressVisible())
    }

    @Test
    fun testState_Connected() {
        val state = Connected
        assertTrue(state.isLastSyncVisible())
        assertFalse(state.isSyncingProgressVisible())
    }

    @Test
    fun testState_Syncing() {
        val state = Syncing
        assertTrue(state.isLastSyncVisible())
        assertTrue(state.isSyncingProgressVisible())
    }

    @Test
    fun testState_Disconnected() {
        val state = Disconnected
        assertTrue(state.isLastSyncVisible())
        assertFalse(state.isSyncingProgressVisible())
    }

    @Test
    fun testState_NoBluetooth() {
        val state = NoBluetooth
        assertFalse(state.isLastSyncVisible())
        assertFalse(state.isSyncingProgressVisible())
    }
}
