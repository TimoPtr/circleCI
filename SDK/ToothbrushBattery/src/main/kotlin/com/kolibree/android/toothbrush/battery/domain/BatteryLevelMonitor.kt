/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.toothbrush.battery.data.model.ToothbrushBatteryLevel
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

/**
 * Battery monitor that will check toothbrush battery level when
 * the application is in the foreground.
 *
 * Each time there is a new toothbrush connection
 * monitor will fetch its battery level and send it to the backend.
 *
 * Monitor will never fail. In case of error it will log it and continue.
 */
@AppScope
internal class BatteryLevelMonitor @Inject constructor(
    private val activeConnectionUseCase: ActiveConnectionUseCase,
    private val batteryLevelUseCase: BatteryLevelUseCase,
    private val sendBatteryLevelUseCase: SendBatteryLevelUseCase,
    @SingleThreadScheduler private val timeScheduler: Scheduler
) : ApplicationLifecycleObserver {

    private var monitorDisposable: Disposable? = null
    private var stopDisposable: Disposable? = null

    override fun onApplicationStarted() {
        stopDisposable.forceDispose()
        if (monitorDisposable != null) {
            return
        }

        monitorDisposable = observeActiveConnections()
            .concatMapMaybe(::getBatteryLevel)
            .concatMapCompletable(sendBatteryLevelUseCase::sendBatteryLevel)
            .doOnError(Timber::e)
            .doOnSubscribe { Timber.d("Battery monitoring enabled") }
            .doFinally { Timber.d("Battery monitoring disabled") }
            .doOnError { FailEarly.fail("Monitor should be error proof!") }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    override fun onApplicationStopped() {
        stopDisposable = Completable.complete()
            .delay(DELAY_FOREGROUND_CHANGES, TimeUnit.SECONDS, timeScheduler)
            .doOnComplete {
                monitorDisposable?.dispose()
                monitorDisposable = null
            }
            .subscribe({}, Timber::e)
    }

    private fun observeActiveConnections(): Flowable<KLTBConnection> =
        activeConnectionUseCase.onConnectionsUpdatedStream()
            .filterSupportedBrushes()

    private fun getBatteryLevel(connection: KLTBConnection): Maybe<ToothbrushBatteryLevel> {
        return Single.timer(DELAY_QUERY_BATTERY_SECONDS, TimeUnit.SECONDS, timeScheduler)
            .flatMap {
                batteryLevelUseCase
                    .batteryLevel(connection)
                    .map { batteryLevel ->
                        ToothbrushBatteryLevel(
                            macAddress = connection.toothbrush().mac,
                            serialNumber = connection.toothbrush().serialNumber,
                            batteryLevel = batteryLevel
                        )
                    }
            }
            .doOnSubscribe { Timber.d("Checking battery level for $connection") }
            .doOnSuccess { Timber.d("Detected battery level $it for $connection") }
            .doOnError { Timber.e(it, "Unable to get battery level!") }
            .toMaybe()
            .onErrorComplete()
    }

    private fun Flowable<KLTBConnection>.filterSupportedBrushes(): Flowable<KLTBConnection> {
        return filter { connection ->
            val model = connection.toothbrush().model
            SUPPORTED_MODELS.contains(model).also { supported ->
                if (!supported) Timber.d("Checking battery not supported for $model")
            }
        }
    }

    internal companion object {
        private const val DELAY_QUERY_BATTERY_SECONDS = 2L
        private const val DELAY_FOREGROUND_CHANGES = 3L
        val SUPPORTED_MODELS = ToothbrushModel.values().filter { !it.isRechargeable() }
    }
}
