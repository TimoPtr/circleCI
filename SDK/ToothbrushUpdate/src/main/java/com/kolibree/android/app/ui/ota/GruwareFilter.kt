/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Single
import javax.inject.Inject

/**
 * Given a [KLTBConnection] and a [GruwareData], filter the [AvailableUpdate] that need to be applied
 *
 * See rules in https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755011/Bootloader+OTA
 */
internal class GruwareFilter @Inject constructor(featureToggleSet: FeatureToggleSet) {

    private val alwaysOfferOtaFeatureToggle =
        featureToggleSet.toggleForFeature(AlwaysOfferOtaUpdateFeature)

    fun filterAvailableUpdates(
        connection: KLTBConnection,
        gruwareData: GruwareData
    ): Single<GruwareData> {
        return Single.fromCallable {
            val filteredFirmwareUpdate =
                filterFirmwareUpdate(connection, gruwareData.firmwareUpdate)

            val filteredBootloaderUpdate = filterBootloaderUpdate(
                connection,
                gruwareData.bootloaderUpdate,
                filteredFirmwareUpdate
            )

            val filteredGruUpdate = filterGruUpdate(connection, gruwareData.gruUpdate)

            GruwareData(
                firmwareUpdate = filteredFirmwareUpdate,
                gruUpdate = filteredGruUpdate,
                bootloaderUpdate = filteredBootloaderUpdate,
                dspUpdate = gruwareData.dspUpdate
            )
        }
    }

    /**
     * [AvailableUpdate] to be applied from the combination of [connection] and [firmwareUpdate]
     *
     * @return [firmwareUpdate] if the version in the toothbrush is older
     *
     * Empty otherwise
     */
    private fun filterFirmwareUpdate(
        connection: KLTBConnection,
        firmwareUpdate: AvailableUpdate
    ): AvailableUpdate {
        if (!firmwareUpdate.isEmpty()) {
            val toothbrushFirmwareVersion = connection.toothbrush().firmwareVersion

            if (firmwareUpdate.shouldOverride(toothbrushFirmwareVersion)) {
                return firmwareUpdate
            }
        }

        return AvailableUpdate.empty(TYPE_FIRMWARE)
    }

    /**
     * [AvailableUpdate] to be applied from the combination of [connection],
     * [bootloaderAvailableUpdate] and [firmwareAvailableUpdate]
     *
     * @return [bootloaderAvailableUpdate] if the version in the toothbrush is older
     * and [firmwareAvailableUpdate] is not empty
     *
     * Empty otherwise
     */
    private fun filterBootloaderUpdate(
        connection: KLTBConnection,
        bootloaderAvailableUpdate: AvailableUpdate,
        firmwareAvailableUpdate: AvailableUpdate
    ): AvailableUpdate {
        if (!firmwareAvailableUpdate.isEmpty() && !bootloaderAvailableUpdate.isEmpty()) {
            val toothbrushBootloaderVersion = connection.bootloaderVersion()

            if (!toothbrushBootloaderVersion.isNull() &&
                bootloaderAvailableUpdate.shouldOverride(toothbrushBootloaderVersion)
            ) {
                return bootloaderAvailableUpdate
            }
        }

        return AvailableUpdate.empty(TYPE_BOOTLOADER)
    }

    /**
     * [AvailableUpdate] to be applied to update Gru
     *
     * Can be empty
     *
     * @return [gruUpdate]
     */
    private fun filterGruUpdate(
        connection: KLTBConnection,
        gruUpdate: AvailableUpdate
    ): AvailableUpdate {
        if (!gruUpdate.isEmpty()) {
            val gruToothbrushVersion =
                connection.detectors().mostProbableMouthZones()?.gruDataVersion

            if (gruToothbrushVersion != null && gruUpdate.shouldOverride(gruToothbrushVersion)) {
                return gruUpdate
            }
        }

        return AvailableUpdate.empty(TYPE_GRU)
    }

    private fun AvailableUpdate.shouldOverride(toothbrushVersion: SoftwareVersion): Boolean {
        return softwareVersion().isNewer(toothbrushVersion) ||
            (alwaysOfferOtaFeatureToggle.value && softwareVersion() == toothbrushVersion)
    }
}

private fun AvailableUpdate.softwareVersion() = SoftwareVersion(version)
private fun KLTBConnection.bootloaderVersion() = toothbrush().bootloaderVersion
