package com.kolibree.android.sdk.wrapper

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.detectors.RNNDetector
import com.kolibree.android.sdk.connection.parameters.Parameters
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.battery.Battery
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.data.model.GruwareData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ToothbrushWrapperImplTest {

    private lateinit var toothbrushFacade: ToothbrushFacade

    private val connection = mock<KLTBConnection>()
    private val toothbrush = mock<Toothbrush>()
    private val user = mock<User>()
    private val parameters = mock<Parameters>()
    private val brushing = mock<Brushing>()
    private val gruwareRepository = mock<GruwareRepository>()
    private val battery = mock<Battery>()
    private val synchronizeBrushingModeUseCase = mock<SynchronizeBrushingModeUseCase>()

    @Before
    fun setUp() {
        whenever(toothbrush.battery()).thenReturn(battery)
        toothbrushFacade = ToothbrushFacadeImpl(
            connection,
            toothbrush,
            user,
            parameters,
            brushing,
            gruwareRepository,
            synchronizeBrushingModeUseCase)
    }

    @Test
    fun verifyHasOtaObservable() {
        val expected = true
        whenever(connection.hasOTAObservable()).thenReturn(Observable.just(expected))
        toothbrushFacade.hasOTAObservable().test().assertNoErrors().assertValue(expected)
    }

    @Test
    fun verifySetDefaultDuration() {
        val duration = 120
        toothbrushFacade.setDefaultBrushingDuration(duration)
        verify(brushing).setDefaultDuration(eq(duration))
    }

    @Test
    fun verifyGetDefaultDuration() {
        val duration = 120
        toothbrushFacade.setDefaultBrushingDuration(duration)
        verify(brushing).setDefaultDuration(eq(duration))
    }

    @Test
    fun verifySetAutoShutdownTimeout() {
        val autoShutdownTimeout = 120
        toothbrushFacade.setAutoShutdownTimeout(autoShutdownTimeout)
        verify(parameters).setAutoShutdownTimeout(eq(autoShutdownTimeout))
    }

    @Test
    fun verifyBatteryLevel() {
        val batteryLevel = 100
        whenever(battery.batteryLevel).thenReturn(Single.just(batteryLevel))
        toothbrushFacade.getBatteryLevel().test().assertNoErrors().assertValues(batteryLevel)
    }

    @Test
    fun verifyIsCharging() {
        val isCharging = true
        whenever(battery.isCharging).thenReturn(Single.just(isCharging))
        toothbrushFacade.isCharging().test().assertNoErrors().assertValues(isCharging)
    }

    @Test
    fun verifyIsRunningBootloader() {
        val isRunningBootloader = true
        whenever(toothbrush.isRunningBootloader).thenReturn(isRunningBootloader)
        Assert.assertEquals(isRunningBootloader, toothbrushFacade.isRunningBootloader())
    }

    @Test
    fun verifyFirmwareVersion() {
        val softwareVersion = SoftwareVersion(12, 13, 14)
        whenever(toothbrush.firmwareVersion).thenReturn(softwareVersion)
        Assert.assertEquals(softwareVersion.major, toothbrushFacade.getFirmwareVersion().major)
        Assert.assertEquals(softwareVersion.minor, toothbrushFacade.getFirmwareVersion().minor)
        Assert.assertEquals(
            softwareVersion.revision,
            toothbrushFacade.getFirmwareVersion().revision
        )
    }

    @Test
    fun verifyGetSerialNumber() {
        val serialNumber = "213123"
        whenever(toothbrush.serialNumber).thenReturn(serialNumber)
        Assert.assertEquals(serialNumber, toothbrushFacade.getSerialNumber())
    }

    @Test
    fun verifyHardwareVersion() {
        val hardwareVersion = HardwareVersion(42, 13)
        whenever(toothbrush.hardwareVersion).thenReturn(hardwareVersion)
        Assert.assertEquals(hardwareVersion, toothbrushFacade.getHardwareVersion())
    }

    @Test
    fun verifyToothbrushModel() {
        val model = ToothbrushModel.ARA
        whenever(toothbrush.model).thenReturn(model)
        Assert.assertEquals(model, toothbrushFacade.getModel())
    }

    @Test
    fun verifyToothbrushName() {
        val model = ToothbrushModel.ARA
        whenever(toothbrush.getName()).thenReturn(model.name)
        Assert.assertEquals(model.name, toothbrushFacade.getName())
    }

    @Test
    fun verifySharedMode() {
        val multiMode = true
        whenever(user.isSharedModeEnabled()).thenReturn(Single.just(multiMode))
        toothbrushFacade.isSharedModeEnabled().test().assertNoErrors().assertValues(multiMode)
    }

    @Test
    fun verifyEnableSharedMode() {
        toothbrushFacade.enableSharedMode()
        verify(user).enableSharedMode()
    }

    @Test
    fun verifySetToothbrushName() {
        val newName = "newName"
        toothbrushFacade.setName(newName)
        verify(toothbrush).setAndCacheName(eq(newName))
    }

    @Test
    fun verifyMacAddr() {
        val macAddr = "213123"
        whenever(toothbrush.mac).thenReturn(macAddr)
        Assert.assertEquals(macAddr, toothbrushFacade.getMac())
    }

    @Test
    fun verifyHasValidGruData_nullDetector() {
        val detectorsManager = mock<DetectorsManager>()
        whenever(detectorsManager.mostProbableMouthZones()).thenReturn(null)
        whenever(connection.detectors()).thenReturn(detectorsManager)

        Assert.assertEquals(false, toothbrushFacade.hasValidGruData())
    }

    @Test
    fun verifyHasValidGruData_detectorHasValidGruData() {
        val detector = mock<RNNDetector>()
        val detectorsManager = mock<DetectorsManager>()
        whenever(detector.hasValidGruData()).thenReturn(true)
        whenever(detectorsManager.mostProbableMouthZones()).thenReturn(detector)
        whenever(connection.detectors()).thenReturn(detectorsManager)

        Assert.assertEquals(true, toothbrushFacade.hasValidGruData())
    }

    @Test
    fun verifyHasValidGruData_detectorHasNotValidGruData() {
        val detector = mock<RNNDetector>()
        val detectorsManager = mock<DetectorsManager>()
        whenever(detector.hasValidGruData()).thenReturn(false)
        whenever(detectorsManager.mostProbableMouthZones()).thenReturn(detector)
        whenever(connection.detectors()).thenReturn(detectorsManager)

        Assert.assertEquals(false, toothbrushFacade.hasValidGruData())
    }

    @Test
    fun verifyCheckUpdates() {
        val firmwareInfo =
            AvailableUpdate.create("version1", "path1", UpdateType.TYPE_FIRMWARE, 0L)
        val gruInfo = AvailableUpdate.create("version2", "path2", UpdateType.TYPE_GRU, 0L)
        val bootloaderInfo =
            AvailableUpdate.create("version3", "path3", UpdateType.TYPE_BOOTLOADER, null)
        val dspInfo = AvailableUpdate.create("version4", "path4", UpdateType.TYPE_DSP, null)
        val result = GruwareData.create(firmwareInfo, gruInfo, bootloaderInfo, dspInfo)
        whenever(gruwareRepository.getGruwareInfo(any(), any(), any(), any()))
            .thenReturn(Single.just(result))
        mockTootbrushInfo()

        toothbrushFacade.checkUpdates()
            .test()
            .assertValue(result)
    }

    @Test
    fun verifyCheckUpdates_invokesToothbrushData() {
        val update = AvailableUpdate.create("v", "p", UpdateType.TYPE_GRU, 0L)
        mockTootbrushInfo()
        whenever(
            gruwareRepository.getGruwareInfo(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(GruwareData.create(update, update, update, update)))

        toothbrushFacade.checkUpdates()

        verify(toothbrush).model
        verify(toothbrush).hardwareVersion
        verify(toothbrush).serialNumber
        verify(toothbrush).firmwareVersion
    }

    private fun mockTootbrushInfo() {
        whenever(toothbrush.model).thenReturn(ToothbrushModel.ARA)
        whenever(toothbrush.hardwareVersion).thenReturn(HardwareVersion(2, 1))
        whenever(toothbrush.serialNumber).thenReturn("SERIAL")
        whenever(toothbrush.firmwareVersion).thenReturn(SoftwareVersion(1, 2, 3))
    }
}
