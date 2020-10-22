@file:SuppressWarnings("PackageNaming")

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.computeCrc
import com.kolibree.android.sdk.version.SoftwareVersion

/**
 * Type for GRU updates
 *
 * We previously did local checks of CRC, and compatibility but it was a source of issues, so we got rid of that.
 */
internal class GRUDataUpdate(
    override val data: ByteArray,
    availableUpdate: AvailableUpdate
) : OtaUpdate {

    override val version = SoftwareVersion(availableUpdate.version)

    override val type = OtaUpdate.TYPE_GRU_DATA

    override val crc = data.computeCrc()

    override fun isCompatible(version: SoftwareVersion) = version != SoftwareVersion.NULL

    override fun isCompatible(model: ToothbrushModel) = true

    override fun checkCRC() {
        // do nothing
    }
}
