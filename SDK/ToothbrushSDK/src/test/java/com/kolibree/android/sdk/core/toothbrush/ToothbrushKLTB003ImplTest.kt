/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.toothbrush

import android.content.Context
import com.google.common.base.Optional
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspFlashFileType
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushDfuUpdater
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.plaqless.DspStateUseCase
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

class ToothbrushKLTB003ImplTest : BaseUnitTest() {

    @Mock
    internal lateinit var driver: BleDriver

    @Mock
    internal lateinit var connection: InternalKLTBConnection

    @Mock
    internal lateinit var context: Context

    @Mock
    internal lateinit var toothbrushRepository: ToothbrushRepository

    @Mock
    internal lateinit var dspStateUseCase: DspStateUseCase

    @Mock
    internal lateinit var updater: ToothbrushDfuUpdater

    private lateinit var toothbrushImpl: ToothbrushKLTB003Impl

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        whenever(driver.getFirmwareVersion()).thenReturn(mock())
        whenever(driver.getHardwareVersion()).thenReturn(mock())

        toothbrushImpl = createSpy()
    }

    @Test
    fun `setSupervisedMouthZone invokes setAndGetDeviceParameter when subscribe`() {
        whenever(driver.setAndGetDeviceParameter(any())).thenReturn(
            PayloadReader(
                byteArrayOf(
                    0,
                    1
                )
            )
        )
        toothbrushImpl.setSupervisedMouthZone(MouthZone16.LoIncExt, 1).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameter(any())
    }

    /*
    dspState
     */

    @Test
    fun `dspState listens to dspStateSingle when model has DSP`() {
        ToothbrushModel.values()
            .filter { it.hasDsp }
            .forEach { model ->
                val dspStateSubject = SingleSubject.create<DspState>()
                whenever(dspStateUseCase.dspStateSingle()).thenReturn(dspStateSubject)

                val observer = createSpy(model)
                    .dspState()
                    .test()
                    .assertNotComplete()

                dspStateSubject.assertHasObserversAndComplete(
                    DspState(
                        hasValidFirmware = false,
                        usesDeprecatedFirmwareFormat = false,
                        firmwareVersion = DspVersion.NULL,
                        flashFileType = DspFlashFileType.NO_FLASH_FILE,
                        flashFileVersion = DspVersion.NULL,
                        bootloaderVersion = 0
                    )
                )

                observer.assertComplete()
            }
    }

    @Test
    fun `dspState emits CommandNotSupportedException on every model that has no DSP`() {
        ToothbrushModel.values()
            .filterNot { it.hasDsp }
            .forEach { model ->
                createSpy(model)
                    .dspState()
                    .test()
                    .assertNotComplete()
                    .assertNoValues()
                    .assertError(CommandNotSupportedException::class.java)
            }
    }

    /*
    Utils
     */

    private fun createSpy(model: ToothbrushModel = MODEL) =
        spy(
            ToothbrushKLTB003Impl(
                driver = driver,
                mac = Optional.of(MAC),
                model = model,
                toothbrushName = NAME,
                updater = updater,
                dspStateUseCase = dspStateUseCase
            )
        )

    companion object {

        private const val MAC = "AA:BB:CC:DD:EE"

        private val MODEL = ToothbrushModel.PLAQLESS

        private const val NAME = "default name"
    }
}
