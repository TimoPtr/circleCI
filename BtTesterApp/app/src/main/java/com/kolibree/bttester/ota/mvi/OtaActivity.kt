/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.mvi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.bttester.R
import com.kolibree.bttester.databinding.ActivityOtaBinding
import com.kolibree.bttester.legacy.PairDialogFragment
import javax.inject.Inject

private const val OTA_PERMISSIONS_REQUEST_CODE = 12323

internal class OtaActivity :
    BaseMVIActivity<OtaViewState, OtaAction, OtaViewModel.Factory, OtaViewModel, ActivityOtaBinding>(),
    KolibreeServiceInteractor.Listener, PairDialogFragment.OnDeviceSelectedListener {
    override fun onDialogCanceled() {
        // no-op
    }

    override fun onDeviceSelected(scanResult: ToothbrushScanResult) {
        viewModel.onDeviceSelected(scanResult)
    }

    @Inject
    internal lateinit var serviceInteractor: KolibreeServiceInteractor

    override fun getViewModelClass(): Class<OtaViewModel> = OtaViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_ota

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceInteractor.setLifecycleOwner(this)
        serviceInteractor.addListener(this)
        checkPermissions()
    }

    override fun onDestroy() {
        serviceInteractor.removeListener(this)
        super.onDestroy()
    }

    override fun execute(action: OtaAction) {
        when (action) {
            ScanClicked -> PairDialogFragment.showIfNotPresent(supportFragmentManager)
        }
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        //
    }

    override fun onKolibreeServiceDisconnected() {
        //
    }

    override fun onBackPressed() {
        if (viewModel.getViewState()?.otaInProgress == true) {
            Toast.makeText(this, "Cannot leave OTA view when OTA is in progress!", LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            OTA_PERMISSIONS_REQUEST_CODE -> {
                val allPermissionsGranted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                viewModel.updateViewState { copy(permissionsGranted = allPermissionsGranted) }
            }
        }
    }

    private fun checkPermissions() {
        val allPermissionsGranted = isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
            isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
            isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

        viewModel.updateViewState { copy(permissionsGranted = allPermissionsGranted) }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                OTA_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
