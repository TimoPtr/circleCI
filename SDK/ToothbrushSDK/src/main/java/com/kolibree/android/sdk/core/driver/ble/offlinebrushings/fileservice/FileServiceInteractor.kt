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
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import io.reactivex.Single
import javax.inject.Provider

internal class FileServiceInteractorImpl(
    private val klNordicBleManager: KLNordicBleManager,
    private val fileSessionProvider: Provider<out FileSession>
) : FileServiceInteractor {

    @VisibleForTesting
    companion object {
        const val OPEN_SESSION_COMMAND_LENGTH = 2

        const val SESSION_COMMAND: Byte = 0x00
        const val COUNT_FILES_COMMAND: Byte = 0x01
        const val SELECT_NEXT_FILE_COMMAND: Byte = 0x02
        const val GET_SELECTED_FILE_COMMAND: Byte = 0x03
        const val ERASE_SELECTED_FILE_COMMAND: Byte = 0x04
        const val ERASE_ALL_FILES_COMMAND: Byte = 0x05

        const val OPEN_SESSION_PAYLOAD: Byte = 0x00
        const val END_SESSION_PAYLOAD: Byte = 0x01

        const val RESPONSE_SUCCESS: Byte = 0x00
        const val RESPONSE_NO_SESSION_ACTIVE: Byte = 0x01
        const val RESPONSE_NO_FILE_AVAILABLE: Byte = 0x02
        const val RESPONSE_NO_FILE_SELECTED: Byte = 0x03
        const val RESPONSE_BUSY: Byte = 0x04
        const val RESPONSE_BAD_PARAMETER: Byte = 0x06
        const val RESPONSE_BAD_STORAGE: Byte = 0x07
        const val RESPONSE_BLE_ERROR: Byte = 0x08
    }

    override fun openSession(): Single<FileSession> {
        val payloadReader = PayloadWriter(OPEN_SESSION_COMMAND_LENGTH)
        payloadReader.writeByte(SESSION_COMMAND)
        payloadReader.writeByte(OPEN_SESSION_PAYLOAD)
        return klNordicBleManager.fileServiceCommand(payloadReader.bytes)
            .flatMap {
                return@flatMap when (it.skip(1).readInt8()) {
                    RESPONSE_SUCCESS -> Single.just(fileSessionProvider.get())
                    else -> Single.error(FileSessionNotOpenedException)
                }
            }
    }
}

internal interface FileServiceInteractor {
    /**
     * Returns a Single that will emit a FileSession when an active session has been opened. It's
     * the callers responsibility to dispose the session.
     *
     * If a FileSession is disposed, either manually or automatically by the FW, any future operation
     * on the FileSession will fail with NoFileSessionOpenedException
     *
     * Future calls will attempt to open a new session, which can result in a FileSessionNotOpenedException
     * if another session is still alive
     *
     * The Single will emit a FileSessionNotOpenedException if we were unable to open an active session
     */
    fun openSession(): Single<FileSession>
}

/**
 * Represents a file type supported by FileService
 *
 * See https://confluence.kolibree.com/display/SOF/Brushings+BLE+protocol+and+storage+format
 */
@Suppress("MagicNumber")
internal enum class FileType(val value: Byte) {
    BRUSHING(0x01),
    PLAQLESS(0x02),
    GLINT(0x03)
}
