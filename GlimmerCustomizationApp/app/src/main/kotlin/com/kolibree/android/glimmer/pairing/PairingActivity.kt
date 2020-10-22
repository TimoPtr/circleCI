/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.databinding.ActivityPairingBinding
import com.kolibree.android.sdk.scan.ToothbrushScanResult

internal class PairingActivity : BaseMVIActivity<
    PairingViewState,
    PairingActions,
    PairingViewModel.Factory,
    PairingViewModel,
    ActivityPairingBinding>(), PairingNavigatorOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.scanResultList.adapter = ScanResultAdapter(viewModel::onScanResultSelected)
    }

    override fun getViewModelClass() = PairingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_pairing

    override fun execute(action: PairingActions) {
        when (action) {
            is PairingActions.ShowError ->
                Toast.makeText(this, action.error.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBluetoothPermissionState(isGranted: Boolean) {
        viewModel.onBluetoothPermissionState(isGranted)
    }

    override fun onLocationPermissionState(isGranted: Boolean) {
        viewModel.onLocationPermissionState(isGranted)
    }

    override fun onLocationSettingsClosed() {
        viewModel.onLocationSettingsClosed()
    }
}

fun startPairingIntent(context: Context) {
    context.startActivity(Intent(context, PairingActivity::class.java))
}

@Keep
@BindingAdapter("scanResults")
fun RecyclerView.bindScanResults(scanResults: List<ToothbrushScanResult>?) =
    (adapter as ScanResultAdapter)
        .onScanResultList(scanResults ?: listOf())
