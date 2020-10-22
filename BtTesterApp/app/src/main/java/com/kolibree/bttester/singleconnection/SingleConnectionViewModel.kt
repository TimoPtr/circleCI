/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.singleconnection

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.bttester.R
import com.kolibree.bttester.tester.SingleConnectionTester
import com.kolibree.bttester.utils.ToothbrushNotFoundException
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

class SingleConnectionViewModel private constructor(
    private val pairingAssistant: PairingAssistant,
    private val serviceProvider: ServiceProvider,
    baseViewState: SingleConnectionViewState?
) : BaseViewModel<SingleConnectionViewState, SingleConnectionActions>(
    baseViewState ?: SingleConnectionViewState.initial()
) {

    private var connectionDisposable: Disposable? = null

    // LiveData to get if we can start a new connection
    val canStartConnection: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isReadyToScan() == true
    }

    val isConnectionSuccess: LiveData<Int> = map(viewStateLiveData) {
        when {
            it?.isConnectionSuccess == true -> R.string.bt_status_success
            it?.isConnectionSuccess == false -> R.string.bt_status_failure
            else -> R.string.bt_status_unavailable
        }
    }

    val isStatusAvailable: LiveData<Boolean> = map(viewStateLiveData) {
        it?.isReadyToScan() == true && it.isConnectionSuccess != null
    }

    fun onServiceAvailable() {
        updateViewState {
            copy(isServiceAvailable = true)
        }
    }

    fun onServiceNotAvailable() {
        updateViewState {
            copy(isServiceAvailable = false)
        }
    }

    @SuppressLint("DefaultLocale")
    fun startConnection(tbName: String) {
        connectionDisposable = startScanFor(tbName)
            .flatMapObservable { onToothbrushFound(it) }
            .subscribe(
                {
                    if (it.contains(KLTBConnectionState.ACTIVE.name, ignoreCase = true)) {
                        updateViewState {
                            copy(isConnectionSuccess = true, isConnecting = false)
                        }
                    }
                    Timber.i("status received = $it")
                },
                {
                    updateViewState {
                        copy(isConnectionSuccess = false)
                    }
                    Timber.e(it, "Failed to connect to $tbName")
                },
                {
                    updateViewState { copy(isConnecting = false) }

                    connectionDisposable?.dispose()
                }
            )

        disposeOnCleared {
            connectionDisposable
        }
    }

    private fun startScanFor(tbName: String): Single<ToothbrushScanResult> = pairingAssistant.scannerObservable()
        .subscribeOn(Schedulers.io())
        .doOnSubscribe { Timber.d("start scan for $tbName") }
        .filter { it.name == tbName }
        .take(1)
        .timeout(
            SCAN_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            Schedulers.computation(),
            Observable.error(ToothbrushNotFoundException())
        )
        .doOnSubscribe {
            updateViewState {
                copy(isScanning = true)
            }
        }
        .doFinally {
            updateViewState {
                copy(isScanning = false)
            }
        }
        .singleOrError()

    private fun onToothbrushFound(toothbrushScanResult: ToothbrushScanResult): Observable<String> {
        Timber.i("Toothbrush found ! ${toothbrushScanResult.name} / ${toothbrushScanResult.mac}")
        val connectionTester = SingleConnectionTester.create()
        return serviceProvider.connectOnce()
            .subscribeOn(Schedulers.io())
            .flatMapObservable { kolibreeService ->
                connectionTester.testFor(kolibreeService, pairingAssistant, toothbrushScanResult)
            }
            .doOnSubscribe {
                updateViewState {
                    copy(isConnecting = true, isConnectionSuccess = false)
                }
            }
            .doFinally {
                updateViewState {
                    copy(isConnecting = false)
                }
            }
    }

    companion object {
        const val SCAN_TIMEOUT_SECONDS = 20L
    }

    class Factory @Inject constructor(
        private val pairingAssistant: PairingAssistant,
        private val serviceProvider: ServiceProvider
    ) : BaseViewModel.Factory<SingleConnectionViewState>() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SingleConnectionViewModel(pairingAssistant, serviceProvider, viewState) as T
        }
    }
}
