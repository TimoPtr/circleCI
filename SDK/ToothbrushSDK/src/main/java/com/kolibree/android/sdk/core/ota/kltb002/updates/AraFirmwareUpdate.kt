/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.commons.ToothbrushModel

/** Created by miguelaragues on 5/1/18.  */
internal class AraFirmwareUpdate(data: ByteArray, version: String, crc32: Long?) :
    BaseFirmwareUpdate(data, version, crc32) {

    override fun isCompatible(model: ToothbrushModel): Boolean {
        return model === ToothbrushModel.ARA
    }
}
