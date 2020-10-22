/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.LOWER_VERSION
import com.kolibree.android.test.mocks.OtaUpdates.createBootloaderUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createDspUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createFirmwareUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createGruUpdate
import com.kolibree.android.test.mocks.OtaUpdates.defaultSoftwareVersion
import com.kolibree.android.test.mocks.OtaUpdates.higherSoftwareVersion
import com.kolibree.android.test.mocks.OtaUpdates.lowerSoftwareVersion
import com.kolibree.android.test.mocks.createGruwareData
import com.kolibree.android.test.utils.TestFeatureToggle
import com.kolibree.sdkws.data.model.GruwareData.Companion.EMPTY
import junit.framework.TestCase.assertNull
import org.junit.Ignore
import org.junit.Test

/**
 * See usecases in https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755011/Bootloader+OTA
 */
internal class GruDataFilterTest : BaseUnitTest() {

    private val alwaysOfferOtaUpdateFeatureToggle = TestFeatureToggle(AlwaysOfferOtaUpdateFeature)

    private val filterAvailableUpdates = GruwareFilter(setOf(alwaysOfferOtaUpdateFeatureToggle))

    override fun setup() {
        super.setup()
        alwaysOfferOtaUpdateFeatureToggle.value = false
    }

    /*
     * Bootloader + Firmware
     */

    @Test
    fun `Returns empty steps if Update from the server only contains the bootloader file`() {
        val gruwareData = createGruwareData(bootloaderUpdate = createBootloaderUpdate())

        filterAvailableUpdates.filterAvailableUpdates(connection(), gruwareData).test()
            .assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if Toothbrush bootloader version is lower but GruwareData FW version is lower, even if always ota is enabled`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate(LOWER_VERSION)
        )

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = defaultSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if Toothbrush bootloader version is lower but GruwareData FW version is equals`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = defaultSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns untouched gruware if Toothbrush bootloader version is lower, GruwareData FW version is equals but always ota is enabled`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = defaultSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(gruwareData)
    }

    /*
    Firmware should be updated
     */

    @Test
    fun `Returns untouched gruware if Toothbrush bootloader version is lower and GruwareData FW version is higher`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = lowerSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(gruwareData)
    }

    @Test
    fun `Returns bootloader + firmware + Gru untouched gruware if Toothbrush bootloader version is lower and GruwareData FW version is higher`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate(),
            gruUpdate = createGruUpdate()
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = lowerSoftwareVersion(),
                gruVersion = lowerSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(gruwareData)
    }

    @Test
    @Ignore("Waiting for emmanuel to refactor dsp version read in sprint 91")
    fun `Returns bootloader + firmware + DSP untouched gruware if Toothbrush bootloader version is lower and GruwareData FW version is higher`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate(),
            dspUpdate = createDspUpdate()
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = lowerSoftwareVersion(),
                firmwareVersion = lowerSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(gruwareData)
    }

    @Test
    fun `Returns only FW if toothbrush bootloader version is unknown and GruwareData FW version is higher than the one in the toothbrush`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.firmwareUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = lowerSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns only FW if toothbrush bootloader version is equals and GruwareData FW version is higher than the one in the toothbrush`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.firmwareUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = defaultSoftwareVersion(),
                firmwareVersion = lowerSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns only FW if toothbrush bootloader version is higher and GruwareData FW version is higher than the one in the toothbrush`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.firmwareUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(
                bootloaderVersion = higherSoftwareVersion(),
                firmwareVersion = lowerSoftwareVersion()
            ),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    /*
    Unknown Bootloader and FW not applicable
     */

    @Test
    fun `Returns empty steps if toothbrush bootloader version is unknown and GruwareData FW version is equal than the one in the toothbrush`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate()
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if toothbrush bootloader version is unknown and GruwareData FW version is lower than the one in the toothbrush`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            firmwareUpdate = createFirmwareUpdate(version = LOWER_VERSION)
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    /*
    Only Gru
     */

    @Test
    fun `Returns Gru if GruwareData contains GRU and connection has lower GRU version`() {
        val gruwareData = createGruwareData(gruUpdate = createGruUpdate())

        val expectedGruwareData = createGruwareData(
            gruUpdate = gruwareData.gruUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(gruVersion = lowerSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns Gru if GruwareData contains GRU and connection has equals GRU version but always ota is true`() {
        val gruwareData = createGruwareData(gruUpdate = createGruUpdate())

        val expectedGruwareData = createGruwareData(
            gruUpdate = gruwareData.gruUpdate
        )

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            connection(gruVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns empty steps if GruwareData contains GRU and connection does not support gru, even if always ota is true`() {
        val gruwareData = createGruwareData(gruUpdate = createGruUpdate())

        val gruLessConnection = KLTBConnectionBuilder.createAndroidLess()
            .withNullRNN()
            .build()

        assertNull(gruLessConnection.detectors().mostProbableMouthZones())

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            gruLessConnection,
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if GruwareData contains GRU and connection has equals GRU version`() {
        val gruwareData = createGruwareData(gruUpdate = createGruUpdate())

        filterAvailableUpdates.filterAvailableUpdates(
            connection(gruVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if GruwareData contains GRU and connection has higher GRU version`() {
        val gruwareData = createGruwareData(gruUpdate = createGruUpdate())

        filterAvailableUpdates.filterAvailableUpdates(
            connection(gruVersion = higherSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns only Gru if GruwareData contains Bootloader + GRU`() {
        val gruwareData = createGruwareData(
            bootloaderUpdate = createBootloaderUpdate(),
            gruUpdate = createGruUpdate()
        )

        val expectedGruwareData = createGruwareData(
            gruUpdate = gruwareData.gruUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(gruVersion = lowerSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    /*
    Only Firmware
     */

    @Test
    fun `Returns firmware step if GruwareData FW version is higher`() {
        val gruwareData = createGruwareData(firmwareUpdate = createFirmwareUpdate())

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.firmwareUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = lowerSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns firmware step if GruwareData FW version is equals but always ota is enabled`() {
        val gruwareData = createGruwareData(firmwareUpdate = createFirmwareUpdate())

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.firmwareUpdate
        )

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `Returns empty steps if GruwareData FW version is equals`() {
        val gruwareData = createGruwareData(firmwareUpdate = createFirmwareUpdate())

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = defaultSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    @Test
    fun `Returns empty steps if GruwareData FW version is lower, even if always ota is enabled`() {
        val gruwareData = createGruwareData(firmwareUpdate = createFirmwareUpdate())

        enableAlwaysOta()

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = higherSoftwareVersion()),
            gruwareData
        ).test().assertValue(EMPTY)
    }

    /*
    Only DSP
     */

    @Test
    @Ignore("Waiting for emmanuel to refactor dsp version read in sprint 91")
    fun `Returns dsp step if GruwareData Dsp version is higher`() {
        val gruwareData = createGruwareData(dspUpdate = createDspUpdate())

        val expectedGruwareData = createGruwareData(
            firmwareUpdate = gruwareData.dspUpdate
        )

        filterAvailableUpdates.filterAvailableUpdates(
            connection(firmwareVersion = lowerSoftwareVersion()),
            gruwareData
        ).test().assertValue(expectedGruwareData)
    }

    /*
    Utils
     */
    private fun connection(
        bootloaderVersion: SoftwareVersion = SoftwareVersion.NULL,
        firmwareVersion: SoftwareVersion = SoftwareVersion.NULL,
        dspVersion: SoftwareVersion = SoftwareVersion.NULL,
        gruVersion: SoftwareVersion = SoftwareVersion.NULL
    ): KLTBConnection = KLTBConnectionBuilder.createAndroidLess()
        .withBootloaderVersion(bootloaderVersion)
        .withFirmwareVersion(firmwareVersion)
        .withGruDataVersion(gruVersion)
        .build()

    private fun enableAlwaysOta() {
        alwaysOfferOtaUpdateFeatureToggle.value = true
    }
}
