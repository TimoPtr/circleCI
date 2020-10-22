/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.legacy

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

@SuppressLint("DeobfuscatedPublicSdkClass")
@Deprecated("Use BaseTestBrushingFragment from MVI package")
abstract class LegacyBaseTestBrushingViewModel<VS : LegacyBaseTestBrushingViewState>(open var viewState: VS) :
    ViewModel(), DefaultLifecycleObserver {

    private val stateBehaviorRelay = BehaviorRelay.create<VS>()
    private val viewStateObservable: Observable<VS> by lazy {
        stateBehaviorRelay
            .startWith(viewState)
            .hide()
    }

    val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.dispose()
    }

    override fun onResume(owner: LifecycleOwner) {
        emitState(viewState)
    }

    open fun initViewState(): VS {
        return viewState
    }

    fun viewStateObservable(): Observable<VS> {
        viewState = initViewState()
        return viewStateObservable
    }

    fun emitState(viewState: VS) {
        this.viewState = viewState
        stateBehaviorRelay.accept(viewState)
    }

    fun resetAction() {
        emitState(resetActionViewState())
    }

    fun handleException(throwable: Throwable) {
        throwable.printStackTrace()
    }

    internal abstract fun resetActionViewState(): VS
}
