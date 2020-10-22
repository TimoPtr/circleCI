/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import timber.log.Timber

/**
 * Given a [String]?, return a [File] if the string represents a valid filesystem path and the file
 * exists
 *
 * @return [File]? if string represents a path to an existing file. null otherwise
 */
@Keep
fun String?.fileOrNull(): File? {
    if (this.isNullOrBlank()) return null

    @Suppress("LiftReturnOrAssignment")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return fileByNio()
    } else {
        val file = File(this)

        return if (file.exists() && file.isFile) {
            file
        } else {
            null
        }
    }
}

/**
 * Given a [String], return a [File] if the string represents a valid filesystem path and the file
 * exists
 *
 * This method can only be used on Android O and greater, since it uses NIO to read the file
 *
 * @return [File]? if string represents a path to an existing file. null otherwise
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun String.fileByNio(): File? {
    try {
        val path = Paths.get(this)

        if (Files.exists(path) && Files.isRegularFile(path)) {
            return path.toFile()
        }
    } catch (e: InvalidPathException) {
        Timber.w(e, "Path is invalid $this")
    } catch (e: UnsupportedOperationException) {
        Timber.w(e, "Path is invalid $this")
    }

    return null
}
