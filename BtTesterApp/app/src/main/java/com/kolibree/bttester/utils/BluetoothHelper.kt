/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kolibree.android.sdk.util.IBluetoothUtils

object BluetoothHelper {
    private const val REQUEST_PERMISSION_REQ_CODE = 10

    internal fun isBluetoothAvailable(activity: Activity, bluetoothUtils: IBluetoothUtils): Boolean {
        if (!bluetoothUtils.deviceSupportsBle()) {
            Toast.makeText(
                activity, "This device does not support Bluetooth Low Energy", Toast.LENGTH_LONG
            )
                .show()
            return false
        }

        if (!bluetoothUtils.isBluetoothEnabled) {
            Toast.makeText(activity, "Enable bluetooth", Toast.LENGTH_LONG).show()
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            activity.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_REQ_CODE
            )
            return false
        }

        return true
    }

    internal fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        activity: Activity,
        onSuccess: () -> Unit
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_REQ_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now
                    // we may proceed with scanning.
                    onSuccess()
                } else {
                    Toast.makeText(activity, "This doesn't work without location permission", Toast.LENGTH_LONG).show()
                    activity.finish()
                }
            }
        }
    }
}
