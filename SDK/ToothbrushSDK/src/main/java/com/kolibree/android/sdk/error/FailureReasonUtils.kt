package com.kolibree.android.sdk.error

import androidx.annotation.Keep
import timber.log.Timber

@Keep
inline fun callSafely(toCall: () -> Unit) {
    try {
        toCall()
    } catch (reason: FailureReason) {
        Timber.e(reason)
    }
}
