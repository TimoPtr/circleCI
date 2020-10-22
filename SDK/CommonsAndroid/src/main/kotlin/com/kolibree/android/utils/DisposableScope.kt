package com.kolibree.android.utils

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.failearly.FailEarly
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This object allow us to provide a compositeDisposable associated to a scopeName and add some
 * safety check
 *
 * To use it give a proper name to the scope and call the ready method when the scope is ready to be used, before
 * that, FailEarly will be triggered.
 *
 * Once you call clear or dispose you have to call the ready method again, otherwise addSafely
 * will trigger FailEarly.
 *
 *
 * With that in mind you should keep this object has a private field inside your class and only expose
 * an add method to the user of your class. In that case you will be responsible for calling clear and
 * dispose at the right place.
 *
 * Please give a very meaningful name to the scope and your add method and some documentation
 * so that the user will now exactly at which moment you will call clear/dispose
 */
@Keep
class DisposableScope(private val scopeName: String) {

    @VisibleForTesting
    var isReady = AtomicBoolean(false)

    @VisibleForTesting
    val compositeDisposable = CompositeDisposable()

    /**
     * Initialize the scope and allow further calls to addSafely and clear
     * sets isReady flag to true
     */
    fun ready() {
        isReady.set(true)
    }

    /**
     * Add disposable to the inner compositeDisposable
     */
    fun addSafely(disposable: Disposable?) {
        FailEarly.failInConditionMet(!isReady.get(), "scope $scopeName not ready")
        compositeDisposable += disposable
    }

    /**
     * Add disposable to the inner compositeDisposable
     */
    operator fun plusAssign(disposable: Disposable?) {
        addSafely(disposable)
    }

    /**
     * Clear the inner compositeDisposable and sets the isReady flag to false
     */
    fun clear() {
        isReady.set(false)
        compositeDisposable.clear()
    }

    /**
     * Dispose the inner compositeDisposable and sets the isReady flag to false
     */
    fun dispose() {
        isReady.set(false)
        compositeDisposable.dispose()
    }
}
