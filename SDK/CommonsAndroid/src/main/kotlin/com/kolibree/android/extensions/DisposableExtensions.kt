@file:JvmName("DisposableUtils")

package com.kolibree.android.extensions

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

@Keep
fun Disposable?.forceDispose() {
    if (this == null) return

    if (!isDisposed) dispose()
}

@Keep
@SuppressLint("UnsafeCompositeDisposableIssue")
fun CompositeDisposable.addSafely(disposable: Disposable?) {
    when {
        disposable == null -> FailEarly.fail("Trying to add a null disposable to composite!")
        // TODO check if we really want to keep this rule
        // disposable.isDisposed -> FailEarly.fail("Trying to add a disposed disposable to composite!")
        isDisposed -> FailEarly.fail("Trying to add to a disposed composite!")
        else -> add(disposable)
    }
}

@Keep
operator fun CompositeDisposable?.plusAssign(disposable: Disposable?) {
    if (this == null) FailEarly.fail("Composite disposable should not be null!")
    else addSafely(disposable)
}
