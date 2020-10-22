package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushingsExtractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.toothbrushTimestampToOffsetDateTime
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

internal class FileServiceOfflineBrushingsExtractor(
    private val fileServiceInteractor: FileServiceInteractor
) : OfflineBrushingsExtractor {
    companion object {
        val fileUnrecoverableExceptions =
            arrayOf(FileSessionNotOpenedException::class, FileSessionNotActiveException::class)
    }

    @Volatile
    private var activeSession: FileSession? = null

    /**
     * Pop a single record from the record
     *
     * This is used instead of getRecords to respect the legacy [OfflineBrushingsExtractor] interface
     *
     * Consumer is responsible for closing the File session once he is done. See [finishExtractFileSession]
     */
    override fun popRecord(): Single<OfflineBrushing> {
        return sessionSingle()
            .flatMap { fileSession ->
                fileSession.countFiles().flatMap { fileCount ->
                    if (fileCount == 0) {
                        Single.error(ZeroFilesException)
                    } else {
                        selectAndGetNextFileSingle(fileSession)
                    }
                }
            }
            .onErrorResumeNext { throwable: Throwable ->
                observableAfterExtractionError(throwable)
                    .singleOrError()
            }
    }

    /**
     * Reads all records from the toothbrush
     *
     * Consumer is responsible for closing the File session once he is done. See [finishExtractFileSession]
     */
    fun getRecords(): Observable<OfflineBrushing> {
        return sessionSingle()
            .flatMapObservable { fileSession ->
                fileSession.countFiles()
                    .flatMapObservable getRecordFlatMap@{ fileCount ->
                        if (fileCount == 0)
                            return@getRecordFlatMap Observable.empty<OfflineBrushing>()

                        val observables = mutableListOf<Observable<OfflineBrushing>>()
                        observables.addAll(fileCount.downTo(1).map {
                            selectAndGetNextFileSingle(fileSession)
                                .toObservable()
                                .onErrorResumeNext { throwable: Throwable ->
                                    observableAfterExtractionError(throwable)
                                }
                        })

                        return@getRecordFlatMap Observable.concat(observables)
                    }
                    /*
                    If countFiles or extract emit error, we want to close the session unless it's
                    an unrecoverable error.

                    In all cases, we want to emit the error
                     */
                    .onErrorResumeNext { throwable: Throwable ->
                        observableAfterExtractionError(throwable)
                            .concatWith(disposeSessionCompletable())
                            .concatWith(Observable.error(throwable))
                    }
            }
    }

    @VisibleForTesting
    fun selectAndGetNextFileSingle(fileSession: FileSession): Single<OfflineBrushing> =
        fileSession.selectNextFile()
            .andThen(Single.defer { getNextBrushingRecord(fileSession) })

    /**
     * Returns an Observable used to control if the error thrown by extractAndEraseNextFileObservable
     * is rethrown or if we can proceed with other operations.
     *
     * If the error is recoverable, it'll emit an empty Observable.
     *
     * If the error is unrecoverable, it'll emit an error observable. For example, if the exception
     * signals that the FileSession has terminated, we don't want to proceed with any operation.
     *
     * An error is unrecoverable if a session is not active or the throwable is not a FileSessionException
     */
    @VisibleForTesting
    fun observableAfterExtractionError(throwable: Throwable): Observable<OfflineBrushing> {
        // kml can emit RuntimeException. We don't want to abort extraction of next record
        if (throwable !is FileSessionException && throwable !is java.lang.RuntimeException) {
            return Observable.error(throwable)
        }

        return when (throwable::class) {
            in fileUnrecoverableExceptions -> Observable.error(throwable)
            else -> Observable.empty()
        }
    }

    @VisibleForTesting
    fun getNextBrushingRecord(fileSession: FileSession): Single<OfflineBrushing> {
        return fileSession.getSelectedFile()
            .map { processedBrushing ->
                object : OfflineBrushing {
                    override val datetime: LocalDateTime =
                        processedBrushing.timestampInSeconds
                            .toothbrushTimestampToOffsetDateTime()
                            .toLocalDateTime()
                    override val duration: Duration =
                        Duration.of(processedBrushing.durationInMilliseconds, ChronoUnit.MILLIS)
                    override val processedData: String =
                        processedBrushing.toProcessedBrushing().toJSON()
                }
            }
    }

    /**
     * Count the number of stored brushings in the toothbrush
     *
     * Consumer is responsible for closing the File session once he is done. See [finishExtractFileSession]
     */
    override fun recordCount(): Single<Int> {
        try {
            return sessionSingle()
                .subscribeOn(Schedulers.io())
                .flatMap { fileSession -> fileSession.countFiles() }
        } catch (runtimeException: RuntimeException) {
            Timber.e(runtimeException)

            throw if (runtimeException.cause != null) runtimeException.cause!! else runtimeException
        }
    }

    override fun startExtractFileSession(): Completable = sessionSingle().ignoreElement()

    override fun finishExtractFileSession(): Completable = disposeSessionCompletable()

    override fun deleteRecord(): Completable {
        return sessionSingle()
            .flatMapCompletable { session ->
                session.eraseSelectedFile()
                    .doOnSubscribe { Timber.w("Erasing selected offline brushing") }
            }
    }

    /**
     * Reuses an already open [FileSession] or opens a new one
     */
    private fun sessionSingle(): Single<FileSession> {
        return Single.defer { openSessionSingle() }
            .doOnDispose { nullifyActiveSession() }
    }

    private fun openSessionSingle(): Single<FileSession> {
        return synchronized(this) {
            activeSession?.let { session ->
                session.isDisposed()
                    .flatMap { isDisposed ->
                        if (isDisposed) {
                            openAndStoreSession()
                        } else {
                            Single.just(session)
                        }
                    }
            }
        } ?: openAndStoreSession()
    }

    private fun openAndStoreSession(): Single<FileSession> = fileServiceInteractor.openSession()
        // invoked before invoker is notified of success, so it's fine that it's a side effect
        .doOnSuccess(::storeActiveSession)

    private fun disposeSessionCompletable(): Completable {
        val disposeSessionCompletable = Completable.defer {
            synchronized(this) {
                activeSession?.dispose()
                    ?: Completable.complete()
            }
        }

        return Completable.mergeDelayError(
            listOf(
                disposeSessionCompletable,
                nullifyActiveSessionCompletable()
            )
        )
    }

    private fun storeActiveSession(fileSession: FileSession) {
        synchronized(this) {
            this.activeSession = fileSession
        }
    }

    private fun nullifyActiveSessionCompletable(): Completable =
        Completable.fromAction { nullifyActiveSession() }

    private fun nullifyActiveSession() {
        synchronized(this) {
            activeSession = null
        }
    }
}
