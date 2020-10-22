/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.AvailableUpdate.Companion.empty
import com.kolibree.android.commons.UpdateType
import com.kolibree.sdkws.data.model.GruwareData

@Keep
@JvmOverloads
fun createGruwareData(
    firmwareUpdate: AvailableUpdate = empty(UpdateType.TYPE_FIRMWARE),
    gruUpdate: AvailableUpdate = empty(UpdateType.TYPE_GRU),
    bootloaderUpdate: AvailableUpdate = empty(UpdateType.TYPE_BOOTLOADER),
    dspUpdate: AvailableUpdate = empty(UpdateType.TYPE_DSP)
): GruwareData {
    return GruwareData.create(
        firmwareUpdate,
        gruUpdate,
        bootloaderUpdate,
        dspUpdate
    )
}
