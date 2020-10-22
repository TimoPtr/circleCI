/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.freebrushing

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.bttester.singleconnection.SingleConnectionViewModel
import com.kolibree.bttester.tester.FreeBrushingReport
import com.kolibree.bttester.tester.FreeBrushingTester
import com.kolibree.bttester.utils.AvroWriter
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

internal class FreeBrushingViewModel(
    private val pairingAssistant: PairingAssistant,
    private val serviceProvider: ServiceProvider,
    private val appVersions: KolibreeAppVersions,
    private val avroWriter: AvroWriter,
    initialViewState: FreeBrushingViewState?
) :
    BaseViewModel<FreeBrushingViewState, FreeBrushingActions>(
        initialViewState ?: FreeBrushingViewState.initial()
    ) {

    private var connectionDisposable: Disposable? = null

    // LiveData to get if we can start a new connection
    val canStartConnection: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isReadyToScan() == true
    }

    val result: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.result ?: ""
    }

    val isResultAvailable: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isResultAvailable ?: false
    }

    val avroPath: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.avroPath ?: ""
    }

    val isAvroAvailable: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isAvroAvailable() ?: false
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
    fun startBrushing(tbName: String) {
        connectionDisposable = startScanFor(tbName)
            .doOnSubscribe {
                updateViewState {
                    copy(isBrushing = true, isResultAvailable = false)
                }
            }
            .flatMap { onToothbrushFound(it) }
            .flatMap(::onReportSaveAvro)
            .doFinally {
                updateViewState {
                    copy(isBrushing = false, isResultAvailable = true)
                }
            }
            .subscribe(::onAvroSaved, ::onBrushingError)

        disposeOnCleared {
            connectionDisposable
        }
    }

    private fun onReportSaveAvro(report: FreeBrushingReport): Single<AvroSavedResult> =
        avroWriter.saveAvro(report.avroData).map { AvroSavedResult(report.processedBrushing, it) }

    private fun onAvroSaved(result: AvroSavedResult) {
        Timber.d("result from test ${result.processedBrushing} and avro saved here ${result.avroPath}")
        updateViewState {
            copy(result = result.processedBrushing, avroPath = result.avroPath)
        }
    }

    private fun onBrushingError(error: Throwable) {
        Timber.e(error, "Failure")
        updateViewState {
            copy(result = "failure")
        }
    }

    private fun onToothbrushFound(scanResult: ToothbrushScanResult): Single<FreeBrushingReport> {
        val tester = FreeBrushingTester.create(appVersions = appVersions)
        return serviceProvider.connectOnce().flatMap { service ->
            tester.testFor(service, pairingAssistant, scanResult)
        }
    }

    private fun startScanFor(tbName: String): Single<ToothbrushScanResult> = pairingAssistant.scannerObservable()
        .subscribeOn(Schedulers.io())
        .doOnSubscribe { Timber.d("start scan for $tbName") }
        .filter { it.name == tbName }
        .take(1)
        .timeout(
            SingleConnectionViewModel.SCAN_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            Schedulers.computation(),
            Observable.error(ToothbrushNotFoundException())
        )
        .singleOrError()

    class Factory @Inject constructor(
        private val pairingAssistant: PairingAssistant,
        private val serviceProvider: ServiceProvider,
        private val appVersions: KolibreeAppVersions,
        private val avroWriter: AvroWriter
    ) : BaseViewModel.Factory<FreeBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FreeBrushingViewModel(pairingAssistant, serviceProvider, appVersions, avroWriter, viewState) as T
    }
}

private class AvroSavedResult(val processedBrushing: String, val avroPath: String)
