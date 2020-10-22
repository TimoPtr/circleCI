/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.freebrushing

import android.os.Bundle
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.bttester.R
import com.kolibree.bttester.databinding.ActivityFreeBrushingBinding
import com.kolibree.bttester.utils.BluetoothHelper
import javax.inject.Inject

internal class FreeBrushingActivity :
    BaseMVIActivity<
        FreeBrushingViewState,
        FreeBrushingActions,
        FreeBrushingViewModel.Factory,
        FreeBrushingViewModel,
        ActivityFreeBrushingBinding>(), KolibreeServiceInteractor.Listener {

    @Inject
    internal lateinit var serviceInteractor: KolibreeServiceInteractor

    @Inject
    internal lateinit var bluetoothUtils: IBluetoothUtils

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
            viewModel.startBrushing(binding.tbName.text.toString())
        }
    }

    // ViewModel/MVI creation scope

    override fun getViewModelClass(): Class<FreeBrushingViewModel> = FreeBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_free_brushing

    override fun execute(action: FreeBrushingActions) {
        // no-op
    }

    // KolibreeServiceInteractor.Listener scope

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        viewModel.onServiceAvailable()
    }

    override fun onKolibreeServiceDisconnected() {
        viewModel.onServiceNotAvailable()
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
        binding.freeBrushingBtn.setOnDebouncedClickListener {
            if (BluetoothHelper.isBluetoothAvailable(this, bluetoothUtils)) {
                viewModel.startBrushing(binding.tbName.text.toString())
            }
        }
    }
}
