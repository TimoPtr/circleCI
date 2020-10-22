package com.kolibree.android.sdk.core.driver.ble.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.CharacteristicNotificationStreamer
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.COUNT_FILES_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.END_SESSION_PAYLOAD
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.ERASE_ALL_FILES_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.ERASE_SELECTED_FILE_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.GET_SELECTED_FILE_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_BAD_STORAGE
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_BLE_ERROR
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_BUSY
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_NO_FILE_SELECTED
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_NO_SESSION_ACTIVE
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_SUCCESS
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.SELECT_NEXT_FILE_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.SESSION_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionBusyException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionImpl
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNoFileSelectedException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNotActiveException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNotOpenedException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionUnknownException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileStorageError
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.KLTBFile
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.KLTBFileParser
import com.kolibree.kml.StoredBrushingProcessor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class FileSessionTest : BaseUnitTest() {

    private val bleManager: KLNordicBleManager = mock()

    private val fileParser: KLTBFileParser = mock()

    lateinit var fileSession: FileSessionImpl

    private val fileType = FileType.BRUSHING

    override fun setup() {
        super.setup()

        fileSession =
            FileSessionImpl(
                klNordicBleManager = bleManager,
                fileType = fileType,
                storedBrushingProcessor = Provider { mock<StoredBrushingProcessor>() },
                fileParser = fileParser
            )
    }

    /*
    COUNT FILES
     */
    @Test
    fun countFiles_invokesWithExpectedPayload() {
        val writer = PayloadWriter(2)

        val expectedPayload = writer.writeByte(0x01).writeByte(FileType.BRUSHING.value).bytes

        mockFileServiceCommandResponse(byteArrayOf(COUNT_FILES_COMMAND, fileType.value))

        fileSession.countFiles().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun countFiles_statusOk_returnsNumberOfFiles() {
        val responseWriter = PayloadWriter(4)
        val expectedFileCount = 6547
        val response = PayloadReader(
            responseWriter
                .writeByte(COUNT_FILES_COMMAND)
                .writeByte(RESPONSE_SUCCESS)
                .writeUnsignedInt16(expectedFileCount)
                .bytes
        )

        mockFileServiceCommandResponse(byteArrayOf(COUNT_FILES_COMMAND, fileType.value), response)

        fileSession.countFiles().test().assertValue(expectedFileCount)
    }

    @Test
    fun countFiles_statusNoActiveSession_emitsFileSessionNotActiveException() {
        val responseWriter = PayloadWriter(4)
        val response = PayloadReader(
            responseWriter
                .writeByte(COUNT_FILES_COMMAND)
                .writeByte(RESPONSE_NO_SESSION_ACTIVE)
                .writeUnsignedInt16(0)
                .bytes
        )

        mockFileServiceCommandResponse(byteArrayOf(COUNT_FILES_COMMAND, fileType.value), response)

        fileSession.countFiles().test()
            .assertError(FileSessionNotActiveException)
    }

    @Test
    fun countFiles_statusBusy_emitsBusyException() {
        val responseWriter = PayloadWriter(4)
        val response = PayloadReader(
            responseWriter
                .writeByte(COUNT_FILES_COMMAND)
                .writeByte(RESPONSE_BUSY)
                .writeUnsignedInt16(0)
                .bytes
        )

        mockFileServiceCommandResponse(byteArrayOf(COUNT_FILES_COMMAND, fileType.value), response)

        fileSession.countFiles().test()
            .assertError(FileSessionBusyException)
    }

    @Test
    fun countFiles_statusOther_emitsException() {
        val responseWriter = PayloadWriter(4)
        val response = PayloadReader(
            responseWriter
                .writeByte(COUNT_FILES_COMMAND)
                .writeByte(0x12)
                .writeUnsignedInt16(0)
                .bytes
        )

        mockFileServiceCommandResponse(byteArrayOf(COUNT_FILES_COMMAND, fileType.value), response)

        fileSession.countFiles().test()
            .assertError(FileSessionUnknownException)
    }

    /*
    DISPOSE
     */
    @Test
    fun dispose_invokesWithExpectedPayload() {
        val writer = PayloadWriter(2)

        val expectedPayload = writer.writeByte(0x00).writeByte(END_SESSION_PAYLOAD).bytes

        mockFileServiceCommandResponse(expectedPayload)

        fileSession.dispose().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun dispose_statusSuccess_completesAndFlagsAsDisposed() {
        prepareDispose()

        fileSession.isDisposed().test().assertValue(false)

        fileSession.dispose().test().assertComplete()

        fileSession.isDisposed().test().assertValue(true)
    }

    @Test
    fun dispose_statusNoSessionOpened_completesAndFlagsAsDisposed() {
        prepareDispose(responseByte = RESPONSE_NO_SESSION_ACTIVE)

        fileSession.isDisposed().test().assertValue(false)

        fileSession.dispose().test().assertComplete()

        fileSession.isDisposed().test().assertValue(true)
    }

    @Test
    fun `dispose doesn't flag session as disposed until we subscribe to the observable`() {
        prepareDispose()

        fileSession.isDisposed().test().assertValue(false)

        val observable = fileSession.dispose()

        fileSession.isDisposed().test().assertValue(false)

        observable.test()

        fileSession.isDisposed().test().assertValue(true)
    }

    @Test
    fun `multiple subscriptions to dispose only send command once`() {
        val expectedPayload = prepareDispose()

        fileSession.isDisposed().test().assertValue(false)

        val disposeObservable1 = fileSession.dispose()
        val disposeObservable2 = fileSession.dispose()
        val disposeObservable3 = fileSession.dispose()

        fileSession.isDisposed().test().assertValue(false)

        disposeObservable1.test()
        disposeObservable2.test()
        disposeObservable3.test()

        fileSession.isDisposed().test().assertValue(true)

        verify(bleManager, times(1))
            .fileServiceCommand(byteArrayOf(SESSION_COMMAND, END_SESSION_PAYLOAD))
    }

    /*
    SELECT NEXT FILE
     */
    @Test
    fun selectNextFile_invokesWithExpectedPayload() {
        val writer = PayloadWriter(2)

        val expectedPayload = writer.writeByte(0x02).writeByte(fileType.value).bytes

        mockFileServiceCommandResponse(byteArrayOf(SELECT_NEXT_FILE_COMMAND, fileType.value))

        fileSession.selectNextFile().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun selectNextFile_statusSuccess_completes() {
        val responseWriter = PayloadWriter(6)
        val response = PayloadReader(
            responseWriter
                .writeByte(SELECT_NEXT_FILE_COMMAND)
                .writeByte(RESPONSE_SUCCESS)
                .writeInt32(32)
                .bytes
        )

        mockFileServiceCommandResponse(
            byteArrayOf(SELECT_NEXT_FILE_COMMAND, fileType.value),
            response
        )

        fileSession.selectNextFile().test().assertComplete()
    }

    @Test
    fun `selectNextFile completes with error if fileServiceCommand returns something different than RESPONSE_SUCCESS`() {
        spyFileSession()

        val errorResponse = RESPONSE_NO_SESSION_ACTIVE

        mockFileServiceCommandResponse(
            byteArrayOf(SELECT_NEXT_FILE_COMMAND, fileType.value),
            payloadReader(SELECT_NEXT_FILE_COMMAND, errorResponse)
        )

        doReturn(FileSessionNotOpenedException).whenever(fileSession)
            .mapStatusToFileSessionException(errorResponse)

        fileSession.selectNextFile().test()
            .assertError(FileSessionNotOpenedException)
    }

    /*
    GET SELECTED FILE
     */
    @Test
    fun `getSelectedFile passes fileCharacteristicNotificationsStream to fileParser`() {
        spyFileSession()

        val expectedFlowable = Flowable.empty<Byte>()
        doReturn(expectedFlowable).whenever(fileSession).fileCharacteristicNotificationsStream()

        val parseSubject = SingleSubject.create<KLTBFile>()
        whenever(fileParser.parse(expectedFlowable)).thenReturn(parseSubject)

        fileSession.getSelectedFile().test()

        verify(fileParser).parse(expectedFlowable)
        assertTrue(parseSubject.hasObservers())
    }

    /*
    fileCharacteristicNotificationsStream
     */
    @Test
    fun `fileCharacteristicNotificationsStream subscribes to sendGetSelectedFileCompletable on subscription `() {
        spyFileSession()

        val streamer = CharacteristicNotificationStreamer()
        whenever(bleManager.characteristicStreamer).thenReturn(streamer)

        val sendGetSelectedSubject = CompletableSubject.create()
        doReturn(sendGetSelectedSubject).whenever(fileSession).sendGetSelectedFileCompletable()

        fileSession.fileCharacteristicNotificationsStream().test()

        assertTrue(sendGetSelectedSubject.hasObservers())
    }

    @Test
    fun `fileCharacteristicNotificationsStream enables file service characteristic on subscribe`() {
        spyFileSession()

        val streamer = CharacteristicNotificationStreamer()
        whenever(bleManager.characteristicStreamer).thenReturn(streamer)

        doReturn(Completable.complete()).whenever(fileSession).sendGetSelectedFileCompletable()

        fileSession.fileCharacteristicNotificationsStream().test()

        verify(bleManager).enableFileServiceNotifications()
    }

    @Test
    fun `fileCharacteristicNotificationsStream disables file service characteristic on disposal`() {
        spyFileSession()

        val streamer = CharacteristicNotificationStreamer()
        whenever(bleManager.characteristicStreamer).thenReturn(streamer)

        doReturn(Completable.complete()).whenever(fileSession).sendGetSelectedFileCompletable()

        fileSession.fileCharacteristicNotificationsStream().test().dispose()

        verify(bleManager).disableFileServiceNotifications()
    }

    /*
    sendGetSelectedFileCompletable
     */
    @Test
    fun `sendGetSelectedFileCompletable invokes fileServiceCommand with GET_SELECTED_FILE_COMMAND`() {
        val expectedPayload = byteArrayOf(GET_SELECTED_FILE_COMMAND)
        val subject = SingleSubject.create<PayloadReader>()
        whenever(bleManager.fileServiceCommand(expectedPayload)).thenReturn(subject)

        fileSession.sendGetSelectedFileCompletable().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `sendGetSelectedFileCompletable completes successfuly if fileServiceCommand returns RESPONSE_SUCCESS`() {
        mockFileServiceCommandResponse(
            byteArrayOf(GET_SELECTED_FILE_COMMAND),
            payloadReader(GET_SELECTED_FILE_COMMAND, RESPONSE_SUCCESS)
        )

        fileSession.sendGetSelectedFileCompletable().test().assertComplete()
    }

    @Test
    fun `sendGetSelectedFileCompletable completes with error if fileServiceCommand returns something different than RESPONSE_SUCCESS`() {
        spyFileSession()

        val errorResponse = RESPONSE_NO_SESSION_ACTIVE

        mockFileServiceCommandResponse(
            byteArrayOf(GET_SELECTED_FILE_COMMAND),
            payloadReader(GET_SELECTED_FILE_COMMAND, errorResponse)
        )

        doReturn(FileSessionNotOpenedException).whenever(fileSession)
            .mapStatusToFileSessionException(errorResponse)

        fileSession.sendGetSelectedFileCompletable().test()
            .assertError(FileSessionNotOpenedException)
    }

    /*
    readStatus
     */
    @Test
    fun `readStatus returns 2nd byte`() {
        val expectedStatus: Byte = 7
        val payload = payloadReader(0, expectedStatus)

        assertEquals(expectedStatus, fileSession.readStatus(payload))
    }

    /*
    mapStatusToFileSessionException
     */
    @Test
    fun `mapStatusToFileSessionException returns FileSessionNotActiveException for RESPONSE_NO_SESSION_ACTIVE`() {
        assertEquals(
            FileSessionNotActiveException,
            fileSession.mapStatusToFileSessionException(RESPONSE_NO_SESSION_ACTIVE)
        )
    }

    @Test
    fun `mapStatusToFileSessionException returns FileSessionNoFileSelectedException for RESPONSE_NO_FILE_SELECTED`() {
        assertEquals(
            FileSessionNoFileSelectedException,
            fileSession.mapStatusToFileSessionException(RESPONSE_NO_FILE_SELECTED)
        )
    }

    @Test
    fun `mapStatusToFileSessionException returns FileSessionBusyException for RESPONSE_BUSY`() {
        assertEquals(
            FileSessionBusyException,
            fileSession.mapStatusToFileSessionException(RESPONSE_BUSY)
        )
    }

    @Test
    fun `mapStatusToFileSessionException returns FileStorageError for RESPONSE_BAD_STORAGE`() {
        assertEquals(
            FileStorageError,
            fileSession.mapStatusToFileSessionException(RESPONSE_BAD_STORAGE)
        )
    }

    @Test
    fun `mapStatusToFileSessionException returns FileSessionUnknownException for any other value`() {
        assertEquals(
            FileSessionUnknownException,
            fileSession.mapStatusToFileSessionException(RESPONSE_BLE_ERROR)
        )
        assertEquals(FileSessionUnknownException, fileSession.mapStatusToFileSessionException(9))
        assertEquals(
            FileSessionUnknownException,
            fileSession.mapStatusToFileSessionException(800.toByte())
        )
    }

    /*
    ERASE SELECTED FILE
     */
    @Test
    fun eraseSelectedFile_invokesWithExpectedPayload() {
        val writer = PayloadWriter(1)

        val expectedPayload = writer.writeByte(0x04).bytes

        mockFileServiceCommandResponse(byteArrayOf(ERASE_SELECTED_FILE_COMMAND))

        fileSession.eraseSelectedFile().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun `eraseSelectedFile_statusSuccess_completes`() {
        val response = payloadReader(ERASE_SELECTED_FILE_COMMAND, RESPONSE_SUCCESS)

        mockFileServiceCommandResponse(byteArrayOf(ERASE_SELECTED_FILE_COMMAND), response)

        fileSession.eraseSelectedFile().test().assertComplete()
    }

    @Test
    fun `eraseSelectedFile completes with error if fileServiceCommand returns something different than RESPONSE_SUCCESS`() {
        spyFileSession()

        val response = payloadReader(ERASE_SELECTED_FILE_COMMAND, RESPONSE_NO_SESSION_ACTIVE)

        mockFileServiceCommandResponse(byteArrayOf(ERASE_SELECTED_FILE_COMMAND), response)

        doReturn(FileSessionNotActiveException).whenever(fileSession)
            .mapStatusToFileSessionException(RESPONSE_NO_SESSION_ACTIVE)

        fileSession.eraseSelectedFile().test()
            .assertError(FileSessionNotActiveException)
    }

    /*
    ERASE ALL FILES
     */
    @Test
    fun eraseAllFiles_invokesWithExpectedPayload() {
        val writer = PayloadWriter(2)

        val expectedPayload = writer.writeByte(0x05).writeByte(fileType.value).bytes

        mockFileServiceCommandResponse(byteArrayOf(ERASE_ALL_FILES_COMMAND, fileType.value))

        fileSession.eraseAllFiles().test()

        verify(bleManager).fileServiceCommand(expectedPayload)
    }

    @Test
    fun eraseAllFiles_statusSuccess_completes() {
        val response = payloadReader(ERASE_ALL_FILES_COMMAND, RESPONSE_SUCCESS)

        mockFileServiceCommandResponse(
            byteArrayOf(ERASE_ALL_FILES_COMMAND, fileType.value),
            response
        )

        fileSession.eraseAllFiles().test().assertComplete()
    }

    @Test
    fun `eraseAllFiles completes with error if fileServiceCommand returns something different than RESPONSE_SUCCESS`() {
        spyFileSession()

        val response = payloadReader(ERASE_ALL_FILES_COMMAND, RESPONSE_NO_SESSION_ACTIVE)

        mockFileServiceCommandResponse(
            byteArrayOf(ERASE_ALL_FILES_COMMAND, fileType.value),
            response
        )

        doReturn(FileSessionNotActiveException).whenever(fileSession)
            .mapStatusToFileSessionException(RESPONSE_NO_SESSION_ACTIVE)

        fileSession.eraseAllFiles().test()
            .assertError(FileSessionNotActiveException)
    }

    /*
    Utils
     */

    private fun spyFileSession() {
        fileSession = spy(fileSession)
    }

    private fun payloadReader(vararg bytes: Byte): PayloadReader {
        val responseWriter = PayloadWriter(bytes.size)

        bytes.forEach {
            responseWriter.writeByte(it)
        }

        return PayloadReader(responseWriter.bytes)
    }

    private fun mockFileServiceCommandResponse(
        commandPayload: ByteArray,
        responsePayload: PayloadReader = PayloadReader(byteArrayOf())
    ) {
        whenever(bleManager.fileServiceCommand(commandPayload))
            .thenReturn(Single.just(responsePayload))
    }

    private fun prepareDispose(responseByte: Byte = RESPONSE_SUCCESS) {
        val responseWriter = PayloadWriter(2)
        val response = PayloadReader(
            responseWriter
                .writeByte(SESSION_COMMAND)
                .writeByte(responseByte)
                .bytes
        )

        mockFileServiceCommandResponse(
            byteArrayOf(SESSION_COMMAND, END_SESSION_PAYLOAD),
            response
        )
    }
}
