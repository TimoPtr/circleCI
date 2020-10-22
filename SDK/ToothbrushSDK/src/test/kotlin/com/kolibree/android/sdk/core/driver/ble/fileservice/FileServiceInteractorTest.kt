package com.kolibree.android.sdk.core.driver.ble.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_NO_SESSION_ACTIVE
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_SUCCESS
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.SESSION_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSession
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNotOpenedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import javax.inject.Provider
import org.junit.Test
import org.mockito.Mock

internal class FileServiceInteractorTest : BaseUnitTest() {
    @Mock
    lateinit var bleManager: KLNordicBleManager

    lateinit var fileServiceInteractor: FileServiceInteractorImpl

    private val fileSessionProvider = Provider<FileSession> { mock() }

    override fun setup() {
        super.setup()

        fileServiceInteractor = FileServiceInteractorImpl(bleManager, fileSessionProvider)
    }

    /*
    OPEN SESSION
     */
    @Test
    fun openSession_invokesFileServiceCommandWithExpectedPayload() {
        val expectedPayload = openSessionPayload()

        whenever(bleManager.fileServiceCommand(any())).thenReturn(Single.just(mock()))

        fileServiceInteractor.openSession().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun openSession_bleManagerReturnsSessionOpened_returnsNewFileSession() {
        val responseWriter = PayloadWriter(2)
        val response =
            PayloadReader(responseWriter.writeByte(SESSION_COMMAND).writeByte(RESPONSE_SUCCESS).bytes)
        whenever(bleManager.fileServiceCommand(any())).thenReturn(Single.just(response))

        fileServiceInteractor.openSession().test().assertValueCount(1)
    }

    @Test
    fun openSession_bleManagerReturnsSessionNotOpened_emitsNoSessionError() {
        val responseWriter = PayloadWriter(2)
        val response = PayloadReader(
            responseWriter.writeByte(SESSION_COMMAND).writeByte(RESPONSE_NO_SESSION_ACTIVE).bytes
        )
        whenever(bleManager.fileServiceCommand(any())).thenReturn(Single.just(response))

        fileServiceInteractor.openSession().test()
            .assertError(FileSessionNotOpenedException::class.java)
    }

    private fun openSessionPayload(): ByteArray {
        val writer = PayloadWriter(2)
        return writer.writeByte(0).writeByte(0).bytes
    }
}
