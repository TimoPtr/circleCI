/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.snackbar

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth
import com.kolibree.android.app.ui.toolbartoothbrush.NoLocation
import com.kolibree.android.app.ui.toolbartoothbrush.NoService
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

class SnackbarBluetoothViewModelTest : BaseUnitTest() {

    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()

    private val bluetoothUtils: IBluetoothUtils = mock()

    private val sessionFlags: SessionFlags = mock()

    private lateinit var viewModel: SnackbarBluetoothViewModel

    override fun setup() {
        super.setup()

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.empty())
        whenever(sessionFlags.readSessionFlag(SESSION_FLAG_BLUETOOTH)).thenReturn(true)

        viewModel =
            SnackbarBluetoothViewModel(
                toothbrushConnectionStateViewModel,
                sessionFlags,
                bluetoothUtils
            )
    }

    @Test
    fun `startBluetoothSnackbarChecker emits true if state is NoBluetooth and one previous TB has been connected`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoBluetooth(1, "")))
        )

        viewModel.startBluetoothSnackbarChecker()
            .test()
            .assertValue(true)
    }

    @Test
    fun `startBluetoothSnackbarChecker emits false if session flags is set to false`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoBluetooth(1, "")))
        )

        whenever(sessionFlags.readSessionFlag(SESSION_FLAG_BLUETOOTH)).thenReturn(false)

        viewModel.startBluetoothSnackbarChecker()
            .test()
            .assertValue(false)
    }

    @Test
    fun `startBluetoothSnackbarChecker emits false if state is NoBluetooth and no previous TB has been connected`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoBluetooth(0, "")))
        )

        viewModel.startBluetoothSnackbarChecker()
            .test()
            .assertValue(false)
    }

    @Test
    fun `startBluetoothSnackbarChecker emits five times false and one time true`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(NoLocation(toothbrushes = 1, mac = "")),
                ToothbrushConnectionStateViewState(NoService(toothbrushes = 1, mac = "")),
                ToothbrushConnectionStateViewState(MultiToothbrushDisconnected(macs = listOf(""))),
                ToothbrushConnectionStateViewState(NoBluetooth(1, "")),
                ToothbrushConnectionStateViewState(SingleToothbrushConnecting(mac = "")),
                ToothbrushConnectionStateViewState(NoToothbrushes(toothbrushes = 1))
            )
        )

        viewModel.startBluetoothSnackbarChecker()
            .test()
            .assertValues(false, false, false, true, false, false)
    }

    @Test
    fun `onBluetoothDismiss should set the session flag to false`() {
        viewModel.onBluetoothDismiss()

        verify(sessionFlags).setSessionFlag(SESSION_FLAG_BLUETOOTH, false)
    }

    @Test
    fun `onBluetoothPermissionRetrieved should activate bluetooth is permission is granted`() {
        viewModel.onBluetoothPermissionRetrieved(true)

        verify(bluetoothUtils).enableBluetooth(true)
    }

    @Test
    fun `onBluetoothPermissionRetrieved should not touch bluetooth is permission is denied`() {
        viewModel.onBluetoothPermissionRetrieved(false)

        verify(bluetoothUtils, never()).enableBluetooth(any())
    }
}

private const val SESSION_FLAG_BLUETOOTH = "should_notify_bluetooth_needed"
