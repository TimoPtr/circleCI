/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ConnectionScannedTrackerTest : BaseUnitTest() {

    private val context = mock<Context>()
    private val preferences = mock<SharedPreferences>()
    private val editor = mock<SharedPreferences.Editor>()

    private lateinit var scannedTracker: ConnectionScannedTracker

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)

        scannedTracker = spy(ConnectionScannedTracker(context))
    }

    @Test
    fun `onConnectionScanned stores connection mac with value true`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        scannedTracker.onConnectionScanned(mac)

        verify(editor).putBoolean(mac, true)
    }

    @Test
    fun `isConnectionAlreadyScanned returns boolean value from toothbrush mac`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        scannedTracker.isConnectionAlreadyScanned(mac)

        verify(preferences).getBoolean(mac, false)
    }

    /*
    truncate
     */

    @Test
    fun `truncate invokes clear then apply on subscription`() {
        whenever(editor.clear()).thenReturn(editor)

        val completable = scannedTracker.truncate()

        verify(editor, never()).clear()

        completable.test().assertComplete()

        inOrder(editor) {
            verify(editor).clear()
            verify(editor).apply()
        }
    }
}
