package com.kolibree.android.commons

import androidx.annotation.Keep
import java.io.File

@Keep
data class AvailableUpdate(
    val version: String,
    val updateFilePath: String,
    val type: UpdateType,
    val crc32: Long?
) {
    @Keep
    companion object {
        private const val EMPTY_VERSION = "0.0.0"
        private const val EMPTY_PATH = "-1"

        @JvmStatic
        fun empty(type: UpdateType) =
            AvailableUpdate(
                EMPTY_VERSION,
                EMPTY_PATH,
                type,
                null
            )

        @JvmStatic
        fun create(version: String, filePath: String, type: UpdateType, crc32: Long?) =
            AvailableUpdate(version, filePath, type, crc32)

        @JvmStatic
        fun create(version: String, file: File, type: UpdateType, crc32: Long?) =
            create(
                version = version,
                filePath = file.absolutePath,
                type = type,
                crc32 = crc32
            )

        @JvmStatic
        fun createCrcLess(version: String, file: File, type: UpdateType) =
            create(
                version = version,
                filePath = file.absolutePath,
                type = type,
                crc32 = null
            )
    }

    /**
     * Validates the AvailableUpdate.
     *
     * An empty AvailableUpdate is valid
     *
     * @throws IllegalStateException if it's not valid. The file referenced by updateFilePath does
     * not exist, it's a directory or has size equal to 0
     */
    fun validate() {
        if (isEmpty()) return

        val file = updateFile()

        if (!file.exists() || file.isDirectory || file.length() == 0L) {
            throw IllegalStateException(
                "File %s is not valid, size is %s".format(
                    updateFilePath,
                    file.length()
                )
            )
        }
    }

    fun updateFile() = File(updateFilePath)

    fun isEmpty() = updateFilePath == EMPTY_PATH
}

/**
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/70057985/FAQ+for+app+developers#Gruware-updates
 */
@Keep
enum class UpdateType(val order: Int) {
    TYPE_DSP(order = 1),
    TYPE_BOOTLOADER(order = 2),
    TYPE_FIRMWARE(order = 3),
    TYPE_GRU(order = 4)
}
