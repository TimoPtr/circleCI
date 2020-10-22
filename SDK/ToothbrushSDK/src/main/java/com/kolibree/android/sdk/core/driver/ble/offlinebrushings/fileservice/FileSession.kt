/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.FILES_DATA_CHAR
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.ERASE_SELECTED_FILE_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.GET_SELECTED_FILE_COMMAND
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_BAD_STORAGE
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_BUSY
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_NO_FILE_SELECTED
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_NO_SESSION_ACTIVE
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.RESPONSE_SUCCESS
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl.Companion.SESSION_COMMAND
import com.kolibree.android.sdk.error.BadRecordException
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.ShortVector
import com.kolibree.kml.StoredBrushingProcessor
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider

/**
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755383/BLE+Protocol
 */
internal class FileSessionImpl @Inject constructor(
    private val klNordicBleManager: KLNordicBleManager,
    private val fileType: FileType,
    // an instance should only process 1 brushing
    private val storedBrushingProcessor: Provider<StoredBrushingProcessor>,
    private val fileParser: KLTBFileParser
) : FileSession {
    private val isDisposed = AtomicBoolean(false)

    override fun countFiles(): Single<Int> {
        val payloadWriter = PayloadWriter(2)
        payloadWriter.writeByte(FileServiceInteractorImpl.COUNT_FILES_COMMAND)
        payloadWriter.writeByte(fileType.value)

        return klNordicBleManager.fileServiceCommand(payloadWriter.bytes)
            .flatMap {
                val status = readStatus(it)
                return@flatMap when (status) {
                    RESPONSE_SUCCESS -> Single.just(it.readInt16().toInt())
                    else -> Single.error(mapStatusToFileSessionException(status))
                }
            }
    }

    override fun selectNextFile(): Completable {
        val payloadWriter = PayloadWriter(2)
        payloadWriter.writeByte(FileServiceInteractorImpl.SELECT_NEXT_FILE_COMMAND)
        payloadWriter.writeByte(fileType.value)

        return klNordicBleManager.fileServiceCommand(payloadWriter.bytes)
            .flatMapCompletable {
                when (val status = readStatus(it)) {
                    RESPONSE_SUCCESS -> {
                        Completable.complete()
                    }
                    else -> Completable.error(mapStatusToFileSessionException(status))
                }
            }
    }

    override fun getSelectedFile(): Single<ProcessedBrushing16> {
        val characteristicStream: Flowable<Byte> = fileCharacteristicNotificationsStream()

        return fileParser.parse(characteristicStream)
            .map(::mapFileToShortVector)
            .map { storedBrushingProcessor.get().processBrushing(it) }
            .onErrorResumeNext { throwable ->
                if (throwable is RuntimeException) {
                    Single.error(BadRecordException(throwable.message))
                } else {
                    Single.error(throwable)
                }
            }
    }

    @VisibleForTesting
    fun mapFileToShortVector(file: KLTBFile) = ShortVector(file.bytes.map { it.toShort() })

    @VisibleForTesting
    fun fileCharacteristicNotificationsStream(): Flowable<Byte> {
        return klNordicBleManager.characteristicStreamer
            .characteristicStream(
                FILES_DATA_CHAR,
                { klNordicBleManager.enableFileServiceNotifications() },
                { klNordicBleManager.disableFileServiceNotifications() }
            )
            .mergeWith(sendGetSelectedFileCompletable())
            .concatMap { Flowable.range(0, it.size).map { index -> it[index] } }
    }

    @VisibleForTesting
    fun sendGetSelectedFileCompletable(): Completable {
        return klNordicBleManager
            .fileServiceCommand(byteArrayOf(GET_SELECTED_FILE_COMMAND))
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                when (val status = readStatus(it)) {
                    RESPONSE_SUCCESS -> Completable.complete()
                    else -> Completable.error(mapStatusToFileSessionException(status))
                }
            }
    }

    override fun eraseSelectedFile(): Completable {
        return klNordicBleManager.fileServiceCommand(byteArrayOf(ERASE_SELECTED_FILE_COMMAND))
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                return@flatMapCompletable when (val status = readStatus(it)) {
                    RESPONSE_SUCCESS -> Completable.complete()
                    else -> Completable.error(mapStatusToFileSessionException(status))
                }
            }
    }

    override fun eraseAllFiles(): Completable {
        val payloadWriter = PayloadWriter(2)
        payloadWriter
            .writeByte(FileServiceInteractorImpl.ERASE_ALL_FILES_COMMAND)
            .writeByte(fileType.value)

        return klNordicBleManager.fileServiceCommand(payloadWriter.bytes)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                val status = readStatus(it)
                return@flatMapCompletable when (status) {
                    RESPONSE_SUCCESS -> Completable.complete()
                    else -> Completable.error(mapStatusToFileSessionException(status))
                }
            }
    }

    override fun dispose(): Completable {
        val payloadWriter = PayloadWriter(2)
        payloadWriter.writeByte(SESSION_COMMAND)
        payloadWriter.writeByte(FileServiceInteractorImpl.END_SESSION_PAYLOAD)

        return Completable.defer {
            if (isDisposed.compareAndSet(false, true)) {
                klNordicBleManager.fileServiceCommand(payloadWriter.bytes)
                    .ignoreElement()
            } else {
                Completable.complete()
            }
        }
    }

    override fun isDisposed(): Single<Boolean> = Single.fromCallable { isDisposed.get() }

    @VisibleForTesting
    fun readStatus(response: PayloadReader) = response.skip(1).readInt8()

    @VisibleForTesting
    fun mapStatusToFileSessionException(nonSuccessStatusByte: Byte): FileSessionException {
        return when (nonSuccessStatusByte) {
            RESPONSE_NO_SESSION_ACTIVE -> FileSessionNotActiveException
            RESPONSE_NO_FILE_SELECTED -> FileSessionNoFileSelectedException
            RESPONSE_BUSY -> FileSessionBusyException
            RESPONSE_BAD_STORAGE -> FileStorageError
            else -> FileSessionUnknownException
        }
    }
}

internal interface FileSession {

    /**
     * Returns a [Single] that will return the number of remaining files
     *
     * The [Single] will emit a [FileSessionException] if we were unable to count the number of remaining files
     */
    fun countFiles(): Single<Int>

    /**
     * @return [Completable] that will complete when the next file is selected
     *
     * It will emit a [FileSessionException] if we were unable to select the next file
     */
    fun selectNextFile(): Completable

    /**
     * Returns a [Flowable]<[ByteArray]> that will complete when [FILES_DATA_CHAR] completes the
     * emission of the selected file
     *
     * The [Flowable] will emit a [FileSessionException] if the next file can't be transmitted
     */
    fun getSelectedFile(): Single<ProcessedBrushing16>

    /**
     * Returns a [Completable] that will complete when the selected file is erased
     *
     * The [Completable] will emit a [FileSessionException] if the next file can't be transmitted
     */
    fun eraseSelectedFile(): Completable

    /**
     * Returns a [Completable] that will complete when the all files are deleted
     *
     * The [Completable] will emit a [FileSessionException] if the next file can't be transmitted
     */
    fun eraseAllFiles(): Completable

    /**
     * Returns a [Completable] that will complete when the [FileSession] has been disposed.
     *
     * From this point on, all operations will fail with [FileSessionNotActiveException] and
     * [isDisposed] will emit true
     */
    fun dispose(): Completable

    /**
     * Returns a [Single] that emit true if this [FileSession] is disposed, or false is if it's not
     * disposed
     */
    fun isDisposed(): Single<Boolean>
}
