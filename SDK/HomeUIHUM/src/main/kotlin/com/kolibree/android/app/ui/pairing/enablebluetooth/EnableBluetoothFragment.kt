/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.enablebluetooth

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentEnableBluetoothBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class EnableBluetoothFragment :
    AnimatedBottomGroupFragment<
        EnableBluetoothViewState,
        EnableBluetoothActions,
        EnableBluetoothViewModel.Factory,
        EnableBluetoothViewModel,
        FragmentEnableBluetoothBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): EnableBluetoothFragment = EnableBluetoothFragment()

        fun createArguments(popToScanListOnSuccess: Boolean = false): Bundle {
            return Bundle().apply {
                putBoolean(EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS, popToScanListOnSuccess)
            }
        }
    }

    override fun getViewModelClass(): Class<EnableBluetoothViewModel> =
        EnableBluetoothViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_enable_bluetooth

    override fun execute(action: EnableBluetoothActions) {
        when (action) {
            is EnableBluetoothActions.RequestBluetoothPermission -> {
                askBluetoothPermission.launch(android.Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
    }

    override fun getScreenName(): AnalyticsEvent = EnableBluetoothAnalytics.main()

    private val askBluetoothPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                viewModel.onBluetoothPermissionGranted()
            } else {
                viewModel.onBluetoothPermissionDenied()
            }
        }

    override fun animatedBottomGroup(): AnimatorGroup = binding.bottomAnimatorGroup

    fun extractPopOnSuccess(): Boolean {
        return arguments?.getBoolean(EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS) ?: false
    }
}

private const val EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS = "EXTRA_POP_ON_EXIT"
