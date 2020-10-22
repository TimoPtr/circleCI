/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.pairing.usecases

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.RNNDetector
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createAndroidLess
import com.kolibree.android.tracker.studies.StudiesRepository
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.utils.ApiSDKUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateToothbrushUseCaseImplTest : BaseUnitTest() {

    private val connector = mock<IKolibreeConnector>()

    private val apiSDKUtils = mock<ApiSDKUtils>()

    private val studiesRepository = mock<StudiesRepository>()

    private lateinit var updateToothbrushUseCaseImpl: UpdateToothbrushUseCaseImpl

    override fun setup() {
        super.setup()

        updateToothbrushUseCaseImpl = UpdateToothbrushUseCaseImpl(
            connector = connector,
            apiSdkUtils = apiSDKUtils,
            studiesRepository = studiesRepository
        )
    }

    /*
    updateToothbrush
     */

    @Test
    fun `updateToothbrush creates update data and invokes connector's updateToothbrush`() {
        val connection = KLTBConnectionBuilder.createWithDefaultState(true).build()
        val expectedResponse = mock<UpdateToothbrushResponse>()
        whenever(connector.updateToothbrush(any())).thenReturn(Single.just(expectedResponse))

        updateToothbrushUseCaseImpl
            .updateToothbrush(connection)
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(connector).updateToothbrush(any())
    }

    /*
    createUpdateToothbrushData
     */

    @Test
    fun `createUpdateToothbrushData creates object with proper data`() {
        val expectedDeviceId = "dasdsadsa23"
        whenever(apiSDKUtils.deviceId).thenReturn(expectedDeviceId)
        val expectedProfileId: Long = 34
        val profile = mock<Profile>()
        whenever(profile.id).thenReturn(expectedProfileId)
        whenever(connector.currentProfile)
            .thenReturn(profile)
        val fwVersion = KLTBConnectionBuilder.DEFAULT_FW_VERSION
        val hwVersion = KLTBConnectionBuilder.DEFAULT_HW_VERSION
        val expectedSerialNumber = "124343gfdgfd"
        val mac = "AA:BB:CC"

        val expectedGru = 88L
        val connection: KLTBConnection = createAndroidLess()
            .withSerialNumber(expectedSerialNumber)
            .withMac(mac)
            .withGruDataBinaryVersion(expectedGru)
            .withFirmwareVersion(fwVersion)
            .withHardwareVersion(hwVersion)
            .build()

        val expectedMac = StrippedMac.fromMac(mac).value

        updateToothbrushUseCaseImpl
            .createUpdateToothbrushData(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue {
                expectedDeviceId == it.deviceId &&
                    expectedSerialNumber == it.serial &&
                    expectedMac == it.macAddress &&
                    fwVersion.toBinary() == it.fwVersion &&
                    hwVersion.toBinary() == it.hwVersion &&
                    expectedProfileId == it.profileId &&
                    expectedGru == it.gruVersion
            }
    }

    /*
    getBinaryVersion
     */

    @Test
    fun testBinaryVersionNonNull_returnsVersion() {
        val detector = mock<RNNDetector>()
        val binaryVersion: Long = 8080
        val version = SoftwareVersion(binaryVersion)
        whenever(detector.gruDataVersion).thenReturn(version)
        assertEquals(binaryVersion, updateToothbrushUseCaseImpl.getBinaryVersion(detector))
    }
}
