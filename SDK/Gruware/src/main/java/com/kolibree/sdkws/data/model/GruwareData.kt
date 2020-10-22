package com.kolibree.sdkws.data.model

import androidx.annotation.Keep
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType

@Keep
data class GruwareData(
    val firmwareUpdate: AvailableUpdate,
    val gruUpdate: AvailableUpdate,
    val bootloaderUpdate: AvailableUpdate,
    val dspUpdate: AvailableUpdate
) {
    val availableUpdates = listOf(firmwareUpdate, gruUpdate, bootloaderUpdate, dspUpdate)

    @Keep
    companion object {
        @JvmStatic
        fun create(
            firmwareUpdate: AvailableUpdate,
            gruUpdate: AvailableUpdate,
            bootloaderUpdate: AvailableUpdate,
            dspUpdate: AvailableUpdate
        ): GruwareData {
            return GruwareData(firmwareUpdate, gruUpdate, bootloaderUpdate, dspUpdate)
        }

        @JvmStatic
        fun empty(): GruwareData {
            return create(
                firmwareUpdate = AvailableUpdate.empty(UpdateType.TYPE_FIRMWARE),
                gruUpdate = AvailableUpdate.empty(UpdateType.TYPE_GRU),
                bootloaderUpdate = AvailableUpdate.empty(UpdateType.TYPE_BOOTLOADER),
                dspUpdate = AvailableUpdate.empty(UpdateType.TYPE_DSP)
            )
        }

        val EMPTY = empty()
    }

    fun validate() {
        firmwareUpdate.validate()
        gruUpdate.validate()
        bootloaderUpdate.validate()
        dspUpdate.validate()
    }

    fun isNotEmpty(): Boolean {
        return !firmwareUpdate.isEmpty() ||
            !gruUpdate.isEmpty() ||
            !bootloaderUpdate.isEmpty() ||
            !dspUpdate.isEmpty()
    }
}
