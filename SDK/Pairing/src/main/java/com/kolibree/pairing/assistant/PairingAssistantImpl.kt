/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.pairing.assistant

import android.bluetooth.BluetoothDevice
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.scan.AnyToothbrushScanCallback
import com.kolibree.android.sdk.scan.ToothbrushApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.pairing.session.PairingSession
import com.kolibree.pairing.session.PairingSessionCreator
import com.kolibree.sdkws.core.InternalKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * [PairingAssistant] implementation
 */
class PairingAssistantImpl @Inject constructor(
    toothbrushScannerFactory: ToothbrushScannerFactory,
    private val accountToothbrushRepository: AccountToothbrushRepository,
    private val serviceProvider: ServiceProvider,
    private val kolibreeConnector: InternalKolibreeConnector,
    private val pairingSessionCreator: PairingSessionCreator
) : PairingAssistant, AnyToothbrushScanCallback {

    /**
     * [ToothbrushScanResult] publisher
     */
    private val scanResultSubject = PublishSubject.create<ToothbrushScanResult>()

    /**
     * Real time [ToothbrushScanResult] [List] publisher
     */
    private val rtScanResultRelay = PublishRelay.create<List<ToothbrushScanResult>>()

    /**
     * The publisher is hiding behind this observable so we prevent the users from publishing fake
     * results
     */
    private val scanResultObservable by lazy {
        scanResultSubject
            .hide()
            .doOnSubscribe { onScannerObservableSubscription() }
            .doOnDispose { onScannerObservableDisposition() }
    }

    private val rtScanResultObservable by lazy {
        rtScanResultRelay
            .hide()
            .doOnSubscribe { onRealTimeScannerObservableSubscription() }
            .doOnDispose { onRealTimeScannerObservableDisposition() }
    }

    private val tickerObservable = Observable.interval(TICKER_PERIOD, MILLISECONDS)

    @VisibleForTesting
    val rtResultsDisposable = CompositeDisposable()

    /**
     * Core SDK toothbrush scanner
     */
    private val scanner: ToothbrushScanner = toothbrushScannerFactory
        .getCompatibleBleScanner() ?: throw IllegalStateException(
        "Custom roms are not supported. The device has BLE flag but no BLE scanner"
    )

    /**
     * We keep a track of the observer count so we can stop the internal scanner when it is not used
     * anymore
     */
    @VisibleForTesting
    val scanResultObservableObserverCount = AtomicInteger()

    @VisibleForTesting
    internal val availableDeviceList: HashMap<String, AvailableDevice> = HashMap()

    override fun scannerObservable(): Observable<ToothbrushScanResult> {
        return scanResultObservable
    }

    override fun realTimeScannerObservable(): Observable<List<ToothbrushScanResult>> {
        return rtScanResultObservable
    }

    override fun pair(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<PairingSession> {
        return pairingSessionCreator
            .create(mac, model, name)
            .doOnSubscribe { stopScan() }
            .doOnError { restartScanIfAnySubscriber() }
            .onErrorResumeNext {
                unpair(mac).andThen(Single.error(it))
            }
    }

    override fun pair(result: ToothbrushScanResult): Single<PairingSession> {
        return pair(getRealMacAddress(result), result.model, result.name)
    }

    override fun createPairingSession(connection: KLTBConnection): PairingSession {
        return pairingSessionCreator.create(connection)
    }

    /*
    M1 and CE2 toothbrushes have a +1-on-the-last-byte mac address trick, here we want to store the
    main app mac address for future reusing
     */
    @VisibleForTesting
    fun getRealMacAddress(scanResult: ToothbrushScanResult): String {
        return if (scanResult.toothbrushApp == DFU_BOOTLOADER) {
            DfuUtils.getMainAppMac(scanResult.mac)
        } else {
            scanResult.mac
        }
    }

    override fun getPairedToothbrushes(): Single<List<AccountToothbrush>> {
        return accountToothbrushRepository.getAccountToothbrushes(kolibreeConnector.accountId)
    }

    override fun onToothbrushFound(result: ToothbrushScanResult) {
        scanResultSubject.onNext(result)
    }

    override fun onError(errorCode: Int) {
        scanResultSubject.onError(Exception("Scan failed with critical error: $errorCode"))
    }

    @VisibleForTesting
    fun addNewScanResult(scanResult: ToothbrushScanResult) {
        synchronized(availableDeviceList) {
            // Add or replace new result
            availableDeviceList[scanResult.mac] = AvailableDevice(scanResult)
        }
    }

    // Clear outdated results
    @VisibleForTesting
    fun cleanList(): Boolean {
        var dataSetChanged = false
        synchronized(availableDeviceList) {
            availableDeviceList
                .filter { entry -> isOutdated(System.currentTimeMillis(), entry.value) }
                .forEach {
                    availableDeviceList.remove(it.key)
                    dataSetChanged = true
                }
        }

        return dataSetChanged
    }

    @VisibleForTesting
    fun emitScanResultList() {
        rtScanResultRelay
            .accept(availableDeviceList.values
                .map { availableDevice -> availableDevice.toothbrushScanResult }
                .toList())
    }

    // Called when an observer subscribes to the scan results observable
    private fun onScannerObservableSubscription() {
        if (scanResultObservableObserverCount.getAndIncrement() == 0) {
            startScan()
        }
    }

    // Called when an observer disposes the scan results observable
    private fun onScannerObservableDisposition() {
        if (scanResultObservableObserverCount.decrementAndGet() == 0) {
            stopScan()
        }
    }

    // Called when an observer subscribes to the real time scan results observable
    @VisibleForTesting
    fun onRealTimeScannerObservableSubscription() {
        subscribeToScannerObservable()
        subscribeToTickerObservable()
    }

    @VisibleForTesting
    fun onRealTimeScannerObservableDisposition() {
        rtResultsDisposable.clear()
    }

    @VisibleForTesting
    fun restartScanIfAnySubscriber() {
        if (scanResultObservableObserverCount.get() > 0) {
            startScan()
        }
    }

    @VisibleForTesting
    fun startScan() {
        synchronized(scanner) {
            scanner.startScan(this, true)
        }
    }

    @VisibleForTesting
    fun stopScan() {
        synchronized(scanner) {
            scanner.stopScan(this)
        }
    }

    @VisibleForTesting
    fun subscribeToTickerObservable() {
        rtResultsDisposable.addSafely(
            tickerObservable
                .subscribe({
                    if (cleanList()) {
                        emitScanResultList()
                    }
                }, Throwable::printStackTrace)
        )
    }

    @VisibleForTesting
    fun subscribeToScannerObservable() {
        rtResultsDisposable.addSafely(
            scanResultObservable
                .subscribeOn(Schedulers.io())
                .subscribe({
                    addNewScanResult(it)
                    cleanList()
                    emitScanResultList()
                }, Throwable::printStackTrace)
        )
    }

    @VisibleForTesting
    internal fun isOutdated(now: Long, availableDevice: AvailableDevice): Boolean {
        return availableDevice.lastSeen < now - SCAN_RESULT_LIFE_TIME
    }

    override fun unpair(mac: String): Completable {
        return serviceProvider.connectOnce()
            .flatMapCompletable {
                it.forget(mac)

                accountToothbrushRepository.remove(mac)
            }
    }

    override fun connectAndBlinkBlue(scanResult: ToothbrushScanResult): Single<KLTBConnection> {
        return pairingSessionCreator.connectAndBlinkBlue(
            getRealMacAddress(scanResult),
            scanResult.model,
            scanResult.name
        )
            .onErrorResumeNext {
                unpair(scanResult.mac).andThen(Single.error(it))
            }
    }

    override fun blinkBlue(connection: KLTBConnection): Single<KLTBConnection> =
        pairingSessionCreator.blinkBlue(connection)

    @VisibleForTesting
    internal data class AvailableDevice(
        val toothbrushScanResult: ToothbrushScanResult,
        val lastSeen: Long = System.currentTimeMillis()
    )

    /*
    This method is overriding AnyToothbrushScanCallback's one.
    As we do in the CallbackWrapper class, we return null since we are looking for all devices
     */
    override fun bluetoothDevice(): BluetoothDevice? = null

    internal companion object {
        /**
         * After SCAN_RESULT_LIFE_TIME millis without seeing a scan result, we'll consider it as out
         * of reach and remove it from the scan result list.
         *
         * Setting this value too low causes the results to flicker, even if our toothbrush is
         * right next to the devices.
         *
         * Scan results seem to be batched in 5 second windows, so this allows a scan result to skip
         * one window.
         *
         * According to FW team, Toothbrushes advertise at least every 1300ms, but apparently that's
         * not always picked up by Android.
         */
        @VisibleForTesting
        const val SCAN_RESULT_LIFE_TIME = 11000L // Millis

        @VisibleForTesting
        const val TICKER_PERIOD = 400L
    }
}
