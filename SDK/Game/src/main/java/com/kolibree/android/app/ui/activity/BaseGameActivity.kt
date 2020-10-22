/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.translationssupport.TranslationContext
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Legacy base game class, kept for compatibility purposes.
 *
 * Created by miguelaragues on 16/11/17.
 * Modified by lookashc on 28/03/19
 */

@Deprecated(
    message = "This class is kept only for compatibility purposes. New games should not inherit from it.",
    replaceWith = ReplaceWith("GameInteractor")
)
@Suppress("TooManyFunctions")
abstract class BaseGameActivity : KolibreeServiceActivity(), GameInteractor.Listener {

    @VisibleForTesting
    internal val disposables = CompositeDisposable()

    protected val connection: KLTBConnection?
        get() = gameInteractor.connection

    val toothbrushMac: String?
        get() = gameInteractor.toothbrushMac

    val toothbrushMacSingle: Single<String>
        get() = gameInteractor.getToothbrushMacSingle()

    // unused but an unused inject is required to  make dagger generate the MembersInjector
    // in the base module, and it won't be generated in other modules any more.
    //
    // bug associated :  https://github.com/google/dagger/issues/955
    @Inject
    override lateinit var serviceProvider: ServiceProvider

    @Inject
    lateinit var gameInteractor: GameInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameInteractor.setLifecycleOwner(this)
        gameInteractor.toothbrushMacGetter = { readMacFromIntent() }
        gameInteractor.shouldProceedWithVibrationDelegate = { shouldProceedWithVibration() }
        gameInteractor.addListener(this)
    }

    override fun onDestroy() {
        gameInteractor.removeListener(this)
        disposables.dispose()
        super.onDestroy()
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        super.onKolibreeServiceConnected(service)
        setupLayout()
    }

    protected fun addToDisposables(disposable: Disposable) {
        disposables.addSafely(disposable)
    }

    @Deprecated(
        message = "Lifecycle of connection listeners is managed by the base class, " +
            "please avoid calling this directly. This method will be removed in the future.",
        replaceWith = ReplaceWith("nothing")
    )
    protected fun registerListenersToMainConnection() {
        gameInteractor.maybeRegisterToMainConnection()
    }

    @Deprecated(
        message = "Lifecycle of connection listeners is managed by the base class, " +
            "please avoid calling this directly. This method will be removed in the future.",
        replaceWith = ReplaceWith("nothing")
    )
    protected fun unregisterListenersFromMainConnection() {
        gameInteractor.unregisterFromMainConnection()
    }

    @Deprecated(
        message = "This is kept for temporary compatibility and will be removed.",
        replaceWith = ReplaceWith("nothing")
    )
    protected fun resetToothbrushConnection() {
        gameInteractor.resetToothbrushConnection()
    }

    protected abstract fun setupLayout()

    @CallSuper
    override fun onConnectionEstablished() {
        // Kept for compatibility
    }

    @CallSuper
    override fun onConnectionStateChanged(connection: KLTBConnection, newState: KLTBConnectionState) {
        // Kept for compatibility
    }

    @CallSuper
    override fun onVibratorOn(connection: KLTBConnection) {
        // Kept for compatibility
    }

    @CallSuper
    override fun onVibratorOff(connection: KLTBConnection) {
        // Kept for compatibility
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TranslationContext(newBase))
    }
}
