package com.kolibree.android.sdk.core

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS
import com.kolibree.android.sdk.scan.ScanBeforeConnectFilter
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by aurelien on 12/09/17.
 *
 * SDK internal [KLTBConnection]
 */
@VisibleForApp
interface InternalKLTBConnection : KLTBConnection {

    /**
     * Establish a connection to the toothbrush and emit a complete when it succeeds, or emit an
     * error if it fails.
     *
     * On Android >= 8, the toothbrush must have been scanned before invoking this method. Otherwise,
     * the result attempt will always timeout. See [ScanBeforeConnectFilter]
     *
     * @return Completable that will complete on success, error on failure
     */
    fun establishCompletable(): Completable

    /**
     * Finishes and then establish a connection to the toothbrush and emit a complete when it succeeds,
     * or emit an error if it fails. If brush was already disconnected, it will proceed to connection immediately.
     *
     * @return Completable that will complete on success, error on failure
     */
    fun reconnectCompletable(): Completable

    /**
     * Establish a connection to a 3rd generation toothbrush that is running bootloader and emit a
     * complete when it succeeds, or emit an error if it fails.
     *
     * On Android >= 8, the toothbrush must have been scanned before invoking this method. Otherwise,
     * the result attempt will always timeout. See [ScanBeforeConnectFilter]
     *
     * @return Completable that will complete on success, error on failure
     */
    fun establishDfuBootloaderCompletable(): Completable

    /**
     * Internally set the state of the connection
     *
     * @param state non null KLTBConnectionState
     */
    fun setState(state: KLTBConnectionState)

    /**
     * Set the updated version of the weights file
     *
     * @param gruDataUpdatedVersion non null SoftwareVersion
     */
    fun setGruDataUpdatedVersion(gruDataUpdatedVersion: SoftwareVersion)

    /**
     * @return a Flowable that will emit all data received on [DEVICE_PARAMETERS]
     */
    fun deviceParametersCharacteristicChangedStream(): Flowable<ByteArray>

    /**
     * @return true if the instance will emit the state of vibration right after connection has
     * been established
     */
    fun emitsVibrationStateAfterLostConnection(): Boolean

    /**
     * @see [Toothbrush.isConnectionAllowed]
     */
    fun isConnectionAllowed(): Boolean
}
