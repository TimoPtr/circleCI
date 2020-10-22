package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createAndroidLess
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.data.model.GruwareData
import com.kolibree.sdkws.data.model.GruwareData.Companion.empty
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.io.IOException
import org.junit.Test
import org.mockito.invocation.InvocationOnMock

internal class GruwareInteractorTest : BaseUnitTest() {

    var gruwareDataStore: GruwareDataStore = mock()

    var networkChecker: NetworkChecker = mock()

    var gruwareRepository: GruwareRepository = mock()
    private lateinit var gruwareInteractor: GruwareInteractor

    override fun setup() {
        super.setup()
        gruwareInteractor = spy(
            GruwareInteractor(
                networkChecker,
                gruwareDataStore,
                gruwareRepository
            )
        )
    }

    @Test
    fun `gruware repository contains data returns data from repository`() {
        val connection = mock<KLTBConnection>()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(true)
        val expectedGruwareData = mock<GruwareData>()
        whenever(gruwareDataStore.gruwareFor(connection))
            .thenReturn(expectedGruwareData)
        gruwareInteractor.getGruware(connection).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `gruware repository does not contain Data no Internet emits NetworkNotAvailableException`() {
        val connection = mock<KLTBConnection>()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(false)
        gruwareInteractor.getGruware(connection).test()
            .assertError(NetworkNotAvailableException::class.java)
    }

    @Test
    fun `gruware repository does not contain data with Internetreturns single from connector`() {
        val hwMinor = 5
        val hwMajor = 1
        val swVersion = "1.18.0"
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val expectedGruwareData = mock<GruwareData>()
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.just(expectedGruwareData))
        doNothing()
            .whenever(gruwareInteractor)
            .onNewGruwareData(
                any(), any()
            )
        gruwareInteractor.getGruware(connection).test().assertValue(expectedGruwareData)
    }

    @Test
    fun `gruware repository does not contain data with Internet getGruwareInfo emits IOException emits empty GruwareData`() {
        val hwMinor = 5
        val hwMajor = 1
        val swVersion = "1.18.0"
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.error(IOException("Test forced error")))
        gruwareInteractor.getGruware(connection).test().assertValue(empty())
    }

    @Test
    fun `gruware repository does not contain data with Internet getGruwareInfo emits other error emits error`() {
        val hwMinor = 5
        val hwMajor = 1
        val swVersion = "1.18.0"
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.error(TestForcedException()))
        gruwareInteractor.getGruware(connection).test().assertError(TestForcedException::class.java)
    }

    @Test
    fun `gruware repository does not contain data with Internet invokes onNewGruwareData`() {
        val hwMinor = 5
        val hwMajor = 1
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val swVersion = "1.18.0"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val expectedGruwareData = mock<GruwareData>()
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.just(expectedGruwareData))
        doNothing()
            .whenever(gruwareInteractor)
            .onNewGruwareData(
                any(),
                any()
            )
        gruwareInteractor.getGruware(connection).test()
        verify(gruwareInteractor)
            .onNewGruwareData(connection, expectedGruwareData)
    }

    @Test
    fun `gruware repository does not contain data with Internet validate throws Exception emits Exception`() {
        val hwMinor = 5
        val hwMajor = 1
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val swVersion = "1.18.0"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val gruwareData = mock<GruwareData>()
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.just(gruwareData))
        val expectedError: Throwable = IllegalStateException("Test forced error")
        doAnswer { invocation: InvocationOnMock? -> throw expectedError }
            .whenever(gruwareData).validate()
        gruwareInteractor.getGruware(connection).test().assertError(expectedError)
    }

    @Test
    fun `gruware repository does not contain data with Internet validate is ok emits expected GruwareData`() {
        val hwMinor = 5
        val hwMajor = 1
        val model =
            ToothbrushModel.CONNECT_E1
        val serialNumber = "43esdfds"
        val swVersion = "1.18.0"
        val connection: KLTBConnection = createAndroidLess()
            .withModel(model)
            .withHardwareVersion(hwMajor, hwMinor)
            .withSerialNumber(serialNumber)
            .withFirmwareVersion(swVersion)
            .build()
        whenever(gruwareDataStore.containsGruwareFor(connection))
            .thenReturn(false)
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val expectedGruwareData = mock<GruwareData>()
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val expectedModel = model.internalName.toLowerCase()
        whenever(
            gruwareRepository.getGruwareInfo(
                expectedModel, expectedHwVersion, serialNumber, swVersion
            )
        )
            .thenReturn(Single.just(expectedGruwareData))
        doNothing().whenever(expectedGruwareData).validate()
        gruwareInteractor
            .getGruware(connection)
            .test()
            .assertNoErrors()
            .assertValue(expectedGruwareData)
    }

    /*
  ON NEW GRUWARE DATA
   */
    @Test
    fun `onNewGruwareData invokes repository saveGruware if Gruware is not empty`() {
        val gruwareData = mock<GruwareData>()
        val connection = mock<KLTBConnection>()
        whenever(gruwareData.isNotEmpty()).thenReturn(true)
        gruwareInteractor.onNewGruwareData(connection, gruwareData)
        verify(gruwareDataStore).saveGruware(connection, gruwareData)
    }

    @Test
    fun `onNewGruwareData never invokes repository saveGruware if GruwareData is empty`() {
        val gruwareData = mock<GruwareData>()
        val connection = mock<KLTBConnection>()
        whenever(gruwareData.isNotEmpty()).thenReturn(false)
        gruwareInteractor.onNewGruwareData(connection, gruwareData)
        verify(gruwareDataStore, never()).saveGruware(
            any(), any()
        )
    }
}
