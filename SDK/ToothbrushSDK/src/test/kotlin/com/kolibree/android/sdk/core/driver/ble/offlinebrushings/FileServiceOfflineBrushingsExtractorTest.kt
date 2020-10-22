package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceOfflineBrushingsExtractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSession
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionBusyException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNoFileSelectedException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNotActiveException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionNotOpenedException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionUnknownException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileStorageError
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.ZeroFilesException
import com.kolibree.kml.ProcessedBrushing16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class FileServiceOfflineBrushingsExtractorTest : BaseUnitTest() {

    private val fileServiceInteractor = FakeFileServiceInteractor()

    private lateinit var offlineExtractor: FileServiceOfflineBrushingsExtractor

    override fun setup() {
        super.setup()

        offlineExtractor = spy(FileServiceOfflineBrushingsExtractor(fileServiceInteractor))
    }

    /*
    popRecord
     */
    @Test
    fun `popRecord emits error when open session fails`() {
        val expectedError = FileSessionNotOpenedException
        fileServiceInteractor.errorOnOpenSession = expectedError

        offlineExtractor.popRecord().test().assertError(expectedError)
    }

    @Test
    fun `popRecord emits the file from the session`() {
        val session = fakeFileSessionWithFiles(1)

        val extractSubject = SingleSubject.create<OfflineBrushing>()
        doReturn(extractSubject).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)

        val observer = offlineExtractor.popRecord().test().assertNotComplete()

        verify(offlineExtractor).selectAndGetNextFileSingle(session)

        val expectedRecord: OfflineBrushing = mock()
        extractSubject.onSuccess(expectedRecord)

        observer.assertValue(expectedRecord)
    }

    @Test
    fun `popRecord emits error if there are 0 files`() {
        fakeFileSessionWithFiles(0)

        val observer = offlineExtractor.popRecord().test().assertNotComplete()

        observer.assertError(ZeroFilesException::class.java)
    }

    /*
    We delegate the closing to the client
     */
    @Test
    fun `popRecord never disposes session `() {
        listOf(
            FileSessionNotOpenedException,
            FileSessionNotActiveException,
            FileSessionBusyException,
            FileSessionNoFileSelectedException,
            FileSessionUnknownException,
            FileStorageError
        )
            .forEach { error ->
                val session = fakeFileSessionWithError(error)

                offlineExtractor.popRecord().test()

                assertFalse("Disposed for $error", session.isDisposed)
            }
    }

    /*
    GET REMAINING RECORD COUNT
     */
    @Test
    fun recordCount_openSessionEmitsError_throwsNoFileSessionOpenedException() {
        fileServiceInteractor.errorOnOpenSession = FileSessionNotOpenedException

        offlineExtractor.recordCount().test().assertError(FileSessionNotOpenedException::class.java)
    }

    @Test
    fun recordCount_openSessionEmitsThrowsException_throwsException() {
        fileServiceInteractor.errorOnOpenSession = Exception()

        offlineExtractor.recordCount().test().assertError(Exception::class.java)
    }

    @Test
    fun recordCount_openSessionSuccess_returnsCountBrushingsAndNeverDisposesSession() {
        val expectedCount = 4
        val session = fakeFileSessionWithFiles(expectedCount)

        offlineExtractor.recordCount().test().assertValue(expectedCount)

        assertFalse(session.isDisposed)
    }

    /*
    finishExtractFileSession
     */
    @Test
    fun `finishExtractFileSession completes normally if no session is active`() {
        offlineExtractor.finishExtractFileSession().test().assertComplete().assertNoErrors()
    }

    @Test
    fun `finishExtractFileSession disposes session`() {
        offlineExtractor.startExtractFileSession().test()

        assertFalse(fileServiceInteractor.fakeFileSession.isDisposed)

        offlineExtractor.finishExtractFileSession().test().assertComplete()

        assertTrue(fileServiceInteractor.fakeFileSession.isDisposed)
        assertFalse(fileServiceInteractor.isSessionActive())
    }

    /*
    startExtractFileSession
     */
    @Test
    fun `when there's no active session, startExtractFileSession invokes fileServiceInteractor openSession`() {
        offlineExtractor.startExtractFileSession().test()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    @Test
    fun `when there's an active session but it's disposed, multiple startExtractFileSession opens multiple sessions`() {
        offlineExtractor.startExtractFileSession().test()

        fileServiceInteractor.fakeFileSession.isDisposed = true

        offlineExtractor.startExtractFileSession().test()

        assertEquals(2, fileServiceInteractor.openSessionSuccessCounter)
    }

    @Test
    fun `when there's an active session and it's NOT disposed, multiple startExtractFileSession only invoke openSession once`() {
        fileServiceInteractor.fakeFileSession.isDisposed = false

        offlineExtractor.startExtractFileSession().test()

        offlineExtractor.startExtractFileSession().test()

        assertTrue(fileServiceInteractor.isSessionActive())

        assertEquals(1, fileServiceInteractor.openSessionSuccessCounter)
    }

    /*
    GET RECORDS
     */
    @Test
    fun getRecords_openSessionFailure_emitsFailure() {
        val expectedError = FileSessionNotOpenedException
        fileServiceInteractor.errorOnOpenSession = expectedError

        offlineExtractor.getRecords().test().assertError(expectedError)
    }

    @Test
    fun getRecords_openSessionSuccess_countFile0_returnsEmptyObservable() {
        fakeFileSessionWithFiles(0)

        offlineExtractor.getRecords().test()
            .assertValueCount(0)
            .assertComplete()
    }

    @Test
    fun `getRecords does not dispose session if countFiles emits FileSessionNotOpenedException`() {
        val session = fakeFileSessionWithError(FileSessionNotOpenedException)

        offlineExtractor.getRecords().test().assertError(FileSessionNotOpenedException)

        assertFalse(session.isDisposed)
    }

    @Test
    fun `getRecords does not dispose session if countFiles emits FileSessionNotActiveException`() {
        val session = fakeFileSessionWithError(FileSessionNotActiveException)

        offlineExtractor.getRecords().test().assertError(FileSessionNotActiveException)

        assertFalse(session.isDisposed)
    }

    @Test
    fun `getRecords disposes session if countFiles emits FileSessionBusyException`() {
        val session = fakeFileSessionWithError(FileSessionBusyException)

        offlineExtractor.getRecords().test().assertError(FileSessionBusyException)

        assertTrue(session.isDisposed)
    }

    @Test
    fun `getRecords disposes session if countFiles emits FileSessionNoFileSelectedException`() {
        val session = fakeFileSessionWithError(FileSessionNoFileSelectedException)

        offlineExtractor.getRecords().test().assertError(FileSessionNoFileSelectedException)

        assertTrue(session.isDisposed)
    }

    @Test
    fun `getRecords disposes session if countFiles emits FileSessionUnknownException`() {
        val session = fakeFileSessionWithError(FileSessionUnknownException)

        offlineExtractor.getRecords().test().assertError(FileSessionUnknownException)

        assertTrue(session.isDisposed)
    }

    @Test
    fun `getRecords disposes session if countFiles emits FileStorageError`() {
        val session = fakeFileSessionWithError(FileStorageError)

        offlineExtractor.getRecords().test().assertError(FileStorageError)

        assertTrue(session.isDisposed)
    }

    @Test
    fun getRecords_openSessionSuccess_countFile0_completesAndNeverClosesSession() {
        val session = fakeFileSessionWithFiles(0)

        val observer = offlineExtractor.getRecords().test()

        observer.assertValueCount(0)
            .assertComplete()
            .assertNoErrors()

        assertFalse(session.isDisposed)
    }

    @Test
    fun getRecords_openSessionSuccess_countFile1_selectAndGetNextFileSingleSucceeds_neverInvokesSessionDispose() {
        val session = fakeFileSessionWithFiles(1)

        val extractSubject = SingleSubject.create<OfflineBrushing>()
        doReturn(extractSubject).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)

        val observer = offlineExtractor.getRecords().test()

        verify(offlineExtractor).selectAndGetNextFileSingle(session)

        val expectedRecord: OfflineBrushing = mock()
        extractSubject.onSuccess(expectedRecord)
        observer
            .assertValue(expectedRecord)
            .assertComplete()
            .assertNoErrors()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    @Test
    fun getRecords_openSessionSuccess_countFile1_selectAndGetNextFileSingleError_resumeNextFileSessionExceptionObservableEmitsEmptyObservable_neverInvokesSessionDispose() {
        val session = fakeFileSessionWithFiles(1)

        doReturn(Single.error<OfflineBrushing>(FileSessionBusyException)).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)
        doReturn(Observable.empty<OfflineBrushing>()).whenever(offlineExtractor)
            .observableAfterExtractionError(any())

        val observer = offlineExtractor.getRecords().test()

        verify(offlineExtractor).selectAndGetNextFileSingle(session)

        observer.assertNoValues()
            .assertComplete()
            .assertNoErrors()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    @Test
    fun getRecords_openSessionSuccess_countFile1_selectAndGetNextFileSingleError_resumeNextFileSessionExceptionObservableEmitsErrorObservable_neverInvokesSessionDispose() {
        val session = fakeFileSessionWithFiles(1)

        val exception = FileSessionBusyException
        doReturn(Single.error<OfflineBrushing>(exception)).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)
        doReturn(Observable.error<OfflineBrushing>(exception)).whenever(offlineExtractor)
            .observableAfterExtractionError(exception)

        val observer = offlineExtractor.getRecords().test()

        verify(offlineExtractor).selectAndGetNextFileSingle(session)

        observer.assertError(exception)

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    @Test
    fun getRecords_openSessionSuccess_countFile2_extractAndErase2Files_completesWithSuccessAndNeverDisposesSession() {
        val session = fakeFileSessionWithFiles(2)

        val extractSubject1 = SingleSubject.create<OfflineBrushing>()
        val extractSubject2 = SingleSubject.create<OfflineBrushing>()
        doReturn(extractSubject1, extractSubject2).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)

        val observer = offlineExtractor.getRecords().test()

        verify(offlineExtractor, times(2)).selectAndGetNextFileSingle(session)

        val expectedRecord1: OfflineBrushing = mock()
        extractSubject1.onSuccess(expectedRecord1)

        observer.assertValueCount(1).assertValue(expectedRecord1).assertNotComplete()

        val expectedRecord2: OfflineBrushing = mock()
        extractSubject2.onSuccess(expectedRecord2)

        observer.assertValues(expectedRecord1, expectedRecord2)
            .assertComplete()
            .assertNoErrors()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    @Test
    fun `getRecords proceeds with extraction of second file even if the first emitted error`() {
        val session = fakeFileSessionWithFiles(2)

        val extractSubject1 = SingleSubject.create<OfflineBrushing>()
        val extractSubject2 = SingleSubject.create<OfflineBrushing>()
        doReturn(extractSubject1, extractSubject2).whenever(offlineExtractor)
            .selectAndGetNextFileSingle(session)

        val observer = offlineExtractor.getRecords().test()

        verify(offlineExtractor, times(2)).selectAndGetNextFileSingle(session)

        extractSubject1.onError(FileSessionBusyException)

        val expectedRecord2: OfflineBrushing = mock()
        extractSubject2.onSuccess(expectedRecord2)

        observer.assertValues(expectedRecord2)
            .assertComplete()
            .assertNoErrors()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    /*
    EXTRACT AND ERASE NEXT FILE OBSERVABLE
     */
    @Test
    fun selectAndGetNextFileSingle_selectNextBrushingFailure_emitsError() {
        val session = fakeFileSessionWithError(FileSessionNoFileSelectedException)

        offlineExtractor.selectAndGetNextFileSingle(session).test()
            .assertError(FileSessionNoFileSelectedException)
    }

    @Test
    fun `selectAndGetNextFileSingle doesn't erase record if getNextBrushingRecord emits any error`() {
        val session = fakeFileSessionWithFiles(1)

        arrayOf(
            FileSessionNotOpenedException,
            FileSessionNotActiveException,
            FileSessionBusyException,
            FileSessionNoFileSelectedException,
            FileSessionUnknownException,
            FileStorageError
        ).forEach { exception ->
            doReturn(Single.error<OfflineBrushing>(exception))
                .whenever(offlineExtractor)
                .getNextBrushingRecord(session)

            offlineExtractor.selectAndGetNextFileSingle(session).test()
                .assertError(exception)
        }

        assertEquals(1, session.files.size)
    }

    @Test
    fun selectAndGetNextFileSingle_selectNextBrushingSuccess_getNextBrushingRecordEmitsRecord_invokesEraseFile_emitsBrushingRecord() {
        val session = fakeFileSessionWithFiles(1)

        val expectedBrushing: OfflineBrushing = mock()

        val getNextBrushingSubject = SingleSubject.create<OfflineBrushing>()
        doReturn(getNextBrushingSubject).whenever(offlineExtractor)
            .getNextBrushingRecord(session)

        val observer = offlineExtractor.selectAndGetNextFileSingle(session).test()
            .assertNoValues()
            .assertNotComplete()

        getNextBrushingSubject.onSuccess(expectedBrushing)

        observer.assertValue(expectedBrushing).assertComplete()
    }

    /*
    HANDLE EXTRACT EXCEPTION OBSERVABLE
     */
    @Test
    fun handleExtractExceptionObservable_FileSessionBusyException_emitsEmptyObservable() {
        offlineExtractor.observableAfterExtractionError(FileSessionBusyException).test()
            .assertNoValues().assertComplete()
    }

    @Test
    fun handleExtractExceptionObservable_FileSessionNoFileSelectedException_emitsEmptyObservable() {
        offlineExtractor.observableAfterExtractionError(FileSessionNoFileSelectedException).test()
            .assertNoValues().assertComplete()
    }

    @Test
    fun handleExtractExceptionObservable_FileSessionUnknownException_emitsEmptyObservable() {
        offlineExtractor.observableAfterExtractionError(FileSessionUnknownException).test()
            .assertNoValues().assertComplete()
    }

    @Test
    fun handleExtractExceptionObservable_FileStorageError_emitsEmptyObservable() {
        offlineExtractor.observableAfterExtractionError(FileStorageError).test()
            .assertNoValues().assertComplete()
    }

    @Test
    fun handleExtractExceptionObservable_RuntimeException_emitsEmptyObservable() {
        offlineExtractor.observableAfterExtractionError(java.lang.RuntimeException("KML test forced error"))
            .test()
            .assertNoValues().assertComplete()
    }

    @Test
    fun handleExtractExceptionObservable_FileSessionNotOpenedException_emitsError() {
        offlineExtractor.observableAfterExtractionError(FileSessionNotOpenedException).test()
            .assertError(FileSessionNotOpenedException)
    }

    @Test
    fun handleExtractExceptionObservable_FileSessionNotActiveException_emitsError() {
        offlineExtractor.observableAfterExtractionError(FileSessionNotActiveException).test()
            .assertError(FileSessionNotActiveException)
    }

    @Test
    fun handleExtractExceptionObservable_isNotFileSessionException_emitsError() {
        val expectedException = Exception()
        offlineExtractor.observableAfterExtractionError(expectedException).test()
            .assertError(expectedException)
    }

    /*
    getNextBrushingRecord
     */
    @Test
    fun `getNextBrushingRecord emits error if selectNextFileEmitsError`() {
        val session = fakeFileSessionWithError(FileSessionNoFileSelectedException)

        offlineExtractor.getNextBrushingRecord(session).test()
            .assertError(FileSessionNoFileSelectedException)
    }

    /*
    deleteRecord
     */
    @Test
    fun `deleteRecord completes without disposing the session`() {
        fakeFileSessionWithFiles(1)

        offlineExtractor.deleteRecord().test().assertComplete()

        assertTrue(fileServiceInteractor.isSessionActive())
    }

    /*
    Utils
     */

    private fun fakeFileSession(): FakeFileSession {
        return fileServiceInteractor.fakeFileSession
    }

    private fun fakeFileSessionWithFiles(nbOfFiles: Int): FakeFileSession {
        assertTrue(
            "Provide a smaller value. We don't want to create $nbOfFiles mocks",
            nbOfFiles < 5
        )

        nbOfFiles.downTo(1).forEach { _ ->
            fileServiceInteractor.fakeFileSession.files.add(mock())
        }

        return fileServiceInteractor.fakeFileSession
    }

    private fun fakeFileSessionWithError(throwable: Throwable): FakeFileSession {
        fileServiceInteractor.fakeFileSession.errorOnNextInteraction = throwable

        return fileServiceInteractor.fakeFileSession
    }
}

private class FakeFileServiceInteractor : FileServiceInteractor {
    val fakeFileSession = FakeFileSession()

    var openSessionSuccessCounter = 0

    private var isSessionOpened = false

    var errorOnOpenSession: Throwable? = null

    fun isSessionActive(): Boolean = isSessionOpened && !fakeFileSession.isDisposed

    override fun openSession(): Single<FileSession> = Single.defer {
        errorOnOpenSession?.let { error -> Single.error<FileSession>(error) }
            ?: Single.just(fakeFileSession)
                .doOnSubscribe { openSessionSuccessCounter++ }
                .doOnSuccess { isSessionOpened = true }
    }
}

private class FakeFileSession : FileSession {
    var files = mutableListOf<ProcessedBrushing16>()
    private var selectedIndex = 0

    var isDisposed = false

    var errorOnNextInteraction: Throwable? = null

    override fun countFiles(): Single<Int> = Single.fromCallable {
        maybeThrow()

        files.size
    }

    override fun selectNextFile(): Completable = Completable.fromAction {
        maybeThrow()

        selectedIndex++
    }

    override fun getSelectedFile(): Single<ProcessedBrushing16> = Single.fromCallable {
        maybeThrow()

        files[selectedIndex]
    }

    override fun eraseSelectedFile(): Completable = Completable.fromAction {
        maybeThrow()

        files.removeAt(selectedIndex)
    }

    override fun eraseAllFiles(): Completable = Completable.fromAction {
        maybeThrow()

        files.clear()
    }

    override fun dispose(): Completable = Completable.fromAction {
        maybeThrow()

        isDisposed = true
    }

    override fun isDisposed(): Single<Boolean> = Single.fromCallable { isDisposed }

    private fun maybeThrow() {
        errorOnNextInteraction?.let {
            errorOnNextInteraction = null

            throw it
        }

        if (isDisposed) throw FileSessionNotOpenedException
    }
}
