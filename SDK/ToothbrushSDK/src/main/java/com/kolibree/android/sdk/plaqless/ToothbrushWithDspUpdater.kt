/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.ota.kltb003.KLTB003ToothbrushUpdater
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushDfuUpdater
import io.reactivex.Observable
import javax.inject.Inject

internal class ToothbrushWithDspUpdater @Inject constructor(
    private val dfuUpdater: ToothbrushDfuUpdater,
    private val dspUpdater: ToothbrushDspUpdater
) : KLTB003ToothbrushUpdater {
    override fun update(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent> {
        return when (availableUpdate.type) {
            TYPE_DSP -> dspUpdater.update(availableUpdate)
            TYPE_BOOTLOADER, TYPE_FIRMWARE, TYPE_GRU -> dfuUpdater.update(availableUpdate)
        }
    }

    override fun isUpdateInProgress(): Boolean {
        return dfuUpdater.isUpdateInProgress() || dspUpdater.isUpdateInProgress()
    }
}
