/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.pairing.assistant

import androidx.annotation.Keep
import androidx.annotation.NonNull
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.pairing.session.PairingSession
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Pairing assistant
 *
 * Use this interface to  discover nearby toothbrushes,  pair with a toothbrush and get the list of paired
 * toothbrushes for the current account
 */
@Keep
interface PairingAssistant {

    /**
     * Get the toothbrush scanner [Observable]
     *
     * This observable will make the assistant start scanning for toothbrushes as soon as an
     * observer subscribes to it.
     * The BLE scan will stop when the last observer leaves
     *
     * Don't forget to dispose your subscribers or the scanner will keep on scanning
     *
     * The observable emits [ToothbrushScanResult] objects that you can then pair with the
     * pair() method
     *
     * This observable will never complete but can return an error in case of non recoverable hardware
     * failure
     *
     * @return non null [ToothbrushScanResult] [Observable]
     */
    @NonNull
    fun scannerObservable(): Observable<ToothbrushScanResult>

    /**
     * Get the real time toothbrush scanner [Observable]
     *
     * This observable will make the assistant start scanning for toothbrushes as soon as an
     * observer subscribes to it.
     * The BLE scan will stop when the last observer leaves
     *
     * Don't forget to dispose your subscribers or the scanner will keep on scanning
     *
     * The observable emits [ToothbrushScanResult] [List] of available devices
     *
     * This observable will never complete but can return an error in case of non recoverable hardware
     * failure
     *
     * @return non null [ToothbrushScanResult] [Observable]
     */
    @NonNull
    fun realTimeScannerObservable(): Observable<List<ToothbrushScanResult>>

    /**
     * Pair a scanned toothbrush given the toothbrush information
     *
     * This toothbrush will be added to the database and the reconnection process will be automated
     *
     * @param mac mac addr of the toothbrush
     * @param model model of the toothbrush
     * @param name name of the toothbrush
     * @return non null [Single] The created Pairing session
     */
    @NonNull
    fun pair(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<PairingSession>

    /**
     * Pair a scanned toothbrush given the toothbrush information
     *
     * This toothbrush will be added to the database and the reconnection process will be automated
     *
     * @param result non null [ToothbrushScanResult] result of the Scan
     * @return non null [Single] The created Pairing session
     */

    @NonNull
    fun pair(@NonNull result: ToothbrushScanResult): Single<PairingSession>

    /**
     * Unpair toothbrush given a mac addr
     *
     * This method will remove toothbrush from database and forget current connection
     *
     * @param mac toothbrush mac which will be removed [String]
     * @return non null [Completable]
     */
    @NonNull
    fun unpair(mac: String): Completable

    /**
     * Get the list of paired toothbrushes for the current account, associated by its accountId
     * @return non null [Single]  [List]  [AccountToothbrush] list of AccountToothbrush object
     */
    fun getPairedToothbrushes(): Single<List<AccountToothbrush>>

    /**
     * This method will send blink command to given toothbrush
     * @return completable object [Completable]
     */
    fun connectAndBlinkBlue(scanResult: ToothbrushScanResult): Single<KLTBConnection>

    fun blinkBlue(connection: KLTBConnection): Single<KLTBConnection>

    /**
     * Create a PairingSession by specific connection
     * @param connection non null [KLTBConnection] The connection of toothbrush
     * @return [PairingSession] The created Pairing session
     */
    fun createPairingSession(@NonNull connection: KLTBConnection): PairingSession
}
