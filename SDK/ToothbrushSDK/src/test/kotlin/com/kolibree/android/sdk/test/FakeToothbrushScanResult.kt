/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.test

import android.os.Parcel
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.scan.ToothbrushApp
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.KLTBConnectionBuilder

internal class FakeToothbrushScanResult(
    override val mac: String = KLTBConnectionBuilder.DEFAULT_MAC,
    override val name: String = KLTBConnectionBuilder.DEFAULT_NAME,
    override val model: ToothbrushModel = KLTBConnectionBuilder.DEFAULT_MODEL,
    override val ownerDevice: Long = KLTBConnectionBuilder.DEFAULT_OWNER_ID,
    override val isRunningBootloader: Boolean = false,
    override val isSeamlessConnectionAvailable: Boolean = false,
    override val toothbrushApp: ToothbrushApp = ToothbrushApp.MAIN
) : ToothbrushScanResult {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }
}
