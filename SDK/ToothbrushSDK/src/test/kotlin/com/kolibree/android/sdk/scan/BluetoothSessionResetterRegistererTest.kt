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
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class BluetoothSessionResetterRegistererTest : BaseUnitTest() {
    private val registerer = BluetoothSessionResetterRegisterer

    private val context: Context = mock()

    @Test
    fun register_registers_BluetoothSessionResetterBroadcastReceiver_if_isRegistered_returns_false() {
        registerer.isRegistered.set(false)

        registerer.register(context)

        verify(context).registerReceiver(any(), any())
    }

    @Test
    fun register_never_registers_BluetoothSessionResetterBroadcastReceiver_if_isRegistered_returns_true() {
        registerer.isRegistered.set(true)

        registerer.register(context)

        verify(context, never()).registerReceiver(any(), any())
    }

    @Test
    fun register_only_registers_once_onMultipleInvocations() {
        registerer.isRegistered.set(false)

        registerer.register(context)

        verify(context, times(1)).registerReceiver(any(), any())

        registerer.register(context)
        registerer.register(context)
        registerer.register(context)

        verify(context, times(1)).registerReceiver(any(), any())
    }
}
