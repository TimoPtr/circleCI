package com.kolibree.android.extensions

import androidx.annotation.Keep
import io.reactivex.Completable

@Keep
@SuppressWarnings("TooGenericExceptionCaught")
fun createCompletable(action: () -> Unit) = Completable.create { emitter ->
    try {
        action.invoke()
        emitter.onComplete()
    } catch (ex: Exception) {
        emitter.onError(ex)
    }
}
