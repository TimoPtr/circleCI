/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import android.annotation.SuppressLint
import android.os.Parcel
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.scan.ToothbrushApp
import com.kolibree.android.sdk.scan.ToothbrushScanResult

@SuppressLint("ParcelCreator")
@VisibleForApp
data class MockToothbrushScanResult(
    override val mac: String,
    override val name: String = "name",
    override val model: ToothbrushModel,
    override val ownerDevice: Long = 1L,
    override val isRunningBootloader: Boolean = false,
    override val isSeamlessConnectionAvailable: Boolean = true,
    override val toothbrushApp: ToothbrushApp = ToothbrushApp.UNKNOWN
) : ToothbrushScanResult {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
    }

    override fun describeContents() = 0
}

@Keep
fun fakeScanResult(
    mac: String = KLTBConnectionBuilder.DEFAULT_MAC,
    name: String = KLTBConnectionBuilder.DEFAULT_NAME,
    model: ToothbrushModel = ToothbrushModel.CONNECT_E2
): ToothbrushScanResult =
    MockToothbrushScanResult(
        mac = mac,
        name = name,
        model = model
    )

@Keep
fun KLTBConnection.toScanResult(): ToothbrushScanResult = MockToothbrushScanResult(
    mac = toothbrush().mac,
    model = toothbrush().model,
    name = toothbrush().getName()
)
