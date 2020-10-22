/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import androidx.annotation.Keep
import timber.log.Timber

@Keep
@Suppress("TooGenericExceptionCaught")
inline fun callSafely(toCall: () -> Unit) {
    try {
        toCall()
    } catch (reason: Exception) {
        Timber.e(reason)
    }
}

@Keep
@Suppress("TooGenericExceptionCaught")
inline fun <T> onErrorReturnNull(toCall: () -> T?): T? {
    return try {
        toCall()
    } catch (reason: Exception) {
        Timber.e(reason)
        null
    }
}

@Keep
@Suppress("TooGenericExceptionCaught")
inline fun <T> onErrorReturn(defaultValue: T, toCall: () -> T): T {
    return try {
        toCall()
    } catch (reason: Exception) {
        Timber.e(reason)
        defaultValue
    }
}
