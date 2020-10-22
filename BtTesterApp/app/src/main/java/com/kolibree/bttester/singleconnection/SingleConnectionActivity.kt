/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.singleconnection

import android.os.Bundle
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.bttester.R
import com.kolibree.bttester.databinding.ActivitySingleConnectionBinding
import com.kolibree.bttester.utils.BluetoothHelper
import javax.inject.Inject

class SingleConnectionActivity : BaseMVIActivity<
    SingleConnectionViewState,
    SingleConnectionActions,
    SingleConnectionViewModel.Factory,
    SingleConnectionViewModel,
    ActivitySingleConnectionBinding
    >(), KolibreeServiceInteractor.Listener {

    @Inject
    internal lateinit var serviceInteractor: KolibreeServiceInteractor

    @Inject
    internal lateinit var bluetoothUtils: IBluetoothUtils

    override fun getLayoutId() = R.layout.activity_single_connection

    // Android scope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initInteractors()
        initListeners()
    }

    override fun onDestroy() {
        unsubscribeInteractors()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this) {
            viewModel.startConnection(binding.tbName.text.toString())
        }
    }

    // ViewModel creation scope

    override fun getViewModelClass(): Class<SingleConnectionViewModel> = SingleConnectionViewModel::class.java

    // KolibreeServiceInteractor.Listener scope

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        viewModel.onServiceAvailable()
    }

    override fun onKolibreeServiceDisconnected() {
        viewModel.onServiceNotAvailable()
    }

    // Action scope

    override fun execute(action: SingleConnectionActions) {
        // no-op
    }

    // Internal scope

    private fun initInteractors() {
        serviceInteractor.setLifecycleOwner(this)
        serviceInteractor.addListener(this)
    }

    private fun unsubscribeInteractors() {
        serviceInteractor.removeListener(this)
    }

    private fun initListeners() {
        binding.singleConnectionBtn.setOnDebouncedClickListener {
            if (BluetoothHelper.isBluetoothAvailable(this, bluetoothUtils)) {
                viewModel.startConnection(binding.tbName.text.toString())
            }
        }
    }
}
