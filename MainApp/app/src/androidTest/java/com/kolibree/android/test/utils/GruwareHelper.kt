/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.content.Context
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import com.kolibree.sdkws.data.model.GruwareData
import java.io.File
import java.io.FileOutputStream

fun createGruwareDataFromOtaUpdateType(context: Context, otaUpdateType: OtaUpdateType?): GruwareData {
    // Create a dummy file in order to not throw when we want to validate the Update
    val dummyFile = createDummyFile(context, "dummy.txt")

    return when (otaUpdateType) {
        OtaUpdateType.STANDARD -> GruwareData.create(
            AvailableUpdate.create("100.0.0", dummyFile, UpdateType.TYPE_FIRMWARE, 0L),
            AvailableUpdate.empty(UpdateType.TYPE_GRU),
            AvailableUpdate.empty(UpdateType.TYPE_BOOTLOADER),
            AvailableUpdate.empty(UpdateType.TYPE_DSP)
        )
        OtaUpdateType.MANDATORY, OtaUpdateType.MANDATORY_NEEDS_INTERNET -> GruwareData.create(
            AvailableUpdate.create("100.0.0", dummyFile, UpdateType.TYPE_FIRMWARE, 0L),
            AvailableUpdate.empty(UpdateType.TYPE_GRU),
            AvailableUpdate.create("100.0.0", dummyFile, UpdateType.TYPE_BOOTLOADER, 0L),
            AvailableUpdate.empty(UpdateType.TYPE_DSP)
        )
        else -> GruwareData.EMPTY
    }
}

fun createDummyFile(context: Context, name: String): File {
    val dummyFile = File(context.cacheDir, name)
    dummyFile.createNewFile()

    FileOutputStream(dummyFile).buffered().use {
        it.write(1)
        it.flush()
    }
    return dummyFile
}
