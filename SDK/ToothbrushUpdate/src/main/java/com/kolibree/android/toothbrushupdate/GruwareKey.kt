package com.kolibree.android.toothbrushupdate

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.version.HardwareVersion

internal data class GruwareKey(
    val toothbrushModel: ToothbrushModel,
    val hardwareVersion: HardwareVersion
) {
    companion object {
        @JvmStatic
        fun create(kltbConnection: KLTBConnection): GruwareKey {
            kltbConnection.toothbrush().let {
                return GruwareKey(it.model, it.hardwareVersion)
            }
        }
    }
}
