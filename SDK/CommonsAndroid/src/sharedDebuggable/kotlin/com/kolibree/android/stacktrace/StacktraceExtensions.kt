/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

@file:JvmName(FILE_NAME)

package com.kolibree.android.stacktrace

import android.annotation.SuppressLint
import com.kolibree.android.annotation.VisibleForApp
import timber.log.Timber

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun printRelevantStackTrace(message: String) {
    val throwable = throwableWithoutIrrelevantStackTraces()

    if (throwable.stackTrace.isNotEmpty()) {
        Timber.d(throwable, message)
    } else {
        Timber.w("Couldn't find relevant stacktrace with $message")
    }
}

private fun throwableWithoutIrrelevantStackTraces(): Throwable {
    val throwable = Throwable()

    val stackTrace = throwable.stackTrace.toMutableList()

    stackTrace.removeAll { element ->
        val className = element.className

        className.contains(FILE_NAME)
    }

    throwable.stackTrace = stackTrace.toTypedArray()

    return throwable
}

private const val FILE_NAME = "StacktraceExtensions"
