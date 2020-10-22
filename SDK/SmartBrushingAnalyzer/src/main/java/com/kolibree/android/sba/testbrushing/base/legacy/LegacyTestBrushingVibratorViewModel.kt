/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.legacy

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import io.reactivex.disposables.CompositeDisposable

@Deprecated("Use TestBrushingVibratorViewModel from MVI package")
internal abstract class LegacyTestBrushingVibratorViewModel<VS : LegacyBaseTestBrushingViewState>(
    open val toothbrushMac: String,
    open val serviceProvider: ServiceProvider,
    override var viewState: VS
) : LegacyBaseTestBrushingViewModel<VS>(viewState) {

    private val serviceDisposables = CompositeDisposable()

    @VisibleForTesting
    var registeredVibrator: Vibrator? = null

    @VisibleForTesting
    var registeredState: ConnectionState? = null

    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    var currentConnection: KLTBConnection? = null

    override fun onStart(owner: LifecycleOwner) {
        serviceDisposables.addSafely(
            serviceProvider.connectOnce()
                .subscribe(this::serviceProvidedSuccess, this::handleException)
        )
    }

    fun serviceProvidedSuccess(service: KolibreeService) {
        val connection = service.getConnection(toothbrushMac)
        if (connection != null) {
            currentConnection = connection
            registeredVibrator = connection.vibrator()
            registeredState = connection.state()
            connection.state().register(connectionStateListener)
            connection.vibrator().register(vibratorListener)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        serviceDisposables.clear()
        unregisterVibrator()
    }

    fun unregisterVibrator() {
        registeredVibrator?.unregister(vibratorListener)
        registeredState?.unregister(connectionStateListener)
    }

    @VisibleForTesting
    val vibratorListener = object : VibratorListener {
        override fun onVibratorStateChanged(connection: KLTBConnection, isVibratorOn: Boolean) {
            currentConnection = connection
            onVibratorStateChanged(isVibratorOn)
        }
    }

    @VisibleForTesting
    val connectionStateListener = object : ConnectionStateListener {
        override fun onConnectionStateChanged(connection: KLTBConnection, newState: KLTBConnectionState) {
            currentConnection = connection
        }
    }

    abstract fun onVibratorStateChanged(isVibratorOn: Boolean)
}
