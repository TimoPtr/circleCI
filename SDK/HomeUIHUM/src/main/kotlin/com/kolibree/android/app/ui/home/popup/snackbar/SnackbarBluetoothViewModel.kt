/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.snackbar

import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.persistence.SessionFlags.Companion.SHOULD_NOTIFY_BLUETOOTH_NEEDED
import com.kolibree.android.sdk.util.IBluetoothUtils
import io.reactivex.Flowable
import io.reactivex.Single.fromCallable
import javax.inject.Inject

@VisibleForApp
interface BluetoothPermissionCallback {
    fun onBluetoothPermissionRetrieved(permissionGranted: Boolean)
}

@VisibleForApp
class SnackbarBluetoothViewModel(
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val sessionFlags: SessionFlags,
    private val bluetoothUtils: IBluetoothUtils
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState),
    BluetoothPermissionCallback {

    /**
     * [Flowable] of [Boolean] sending `true` if the user already connected a ToothBrush
     * which currently need the bluetooth before being paired, or `false` in the other cases.
     * It also depends on the sessionFlag, which returns `true` if the Snackbar
     * can be displayed and has not been dismissed
     */
    fun startBluetoothSnackbarChecker(): Flowable<Boolean> =
        getToothBrushStateFlowable()
            .pairWithSessionFlag(SHOULD_NOTIFY_BLUETOOTH_NEEDED)
            .map { (displayMessage, state) ->
                displayMessage && state is NoBluetooth && state.toothbrushes > 0
            }

    private fun getToothBrushStateFlowable(): Flowable<ToothbrushConnectionState> =
        toothbrushConnectionStateViewModel
            .viewStateFlowable
            .map { it.state }

    private fun <V> Flowable<V>.pairWithSessionFlag(sessionFlag: String): Flowable<Pair<Boolean, V>> =
        flatMapSingle { value ->
            fromCallable { sessionFlags.readSessionFlag(sessionFlag) }
                .map { sessionFlag -> sessionFlag to value }
        }

    fun onBluetoothDismiss() {
        sessionFlags.setSessionFlag(SHOULD_NOTIFY_BLUETOOTH_NEEDED, false)
    }

    override fun onBluetoothPermissionRetrieved(permissionGranted: Boolean) {
        if (permissionGranted) {
            bluetoothUtils.enableBluetooth(true)
        }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val sessionFlags: SessionFlags,
        private val bluetoothUtils: IBluetoothUtils
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SnackbarBluetoothViewModel(
                toothbrushConnectionStateViewModel,
                sessionFlags,
                bluetoothUtils
            ) as T
    }
}
