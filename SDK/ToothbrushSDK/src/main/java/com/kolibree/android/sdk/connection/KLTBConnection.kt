package com.kolibree.android.sdk.connection

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushing.BrushingSessionMonitor
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeManager
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.parameters.Parameters
import com.kolibree.android.sdk.connection.root.Root
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.utils.DelegateSubscription
import io.reactivex.Observable

/**
 * Created by aurelien on 07/03/17.
 *
 *
 * Kolibree connection root interface
 */
@Keep
@Suppress("TooManyFunctions")
interface KLTBConnection : DelegateSubscription {

    /**
     * Can be useful to add some custom member to a connection (use it as Android's View's one)
     */
    var tag: Any?

    /**
     * Get the connected toothbrush interface
     *
     * @return non null Toothbrush
     */
    fun toothbrush(): Toothbrush

    /**
     * Get the toothbrush vibrator interface
     *
     * @return non null Vibrator
     */
    fun vibrator(): Vibrator

    /**
     * Get the brushing session monitor interface
     *
     * @return non null BrushingSessionMonitor
     */
    fun brushingSessionMonitor(): BrushingSessionMonitor

    /**
     * Get the toothbrush connection state interface
     *
     * @return non null ConnectionState
     */
    fun state(): ConnectionState

    /**
     * Get toothbrush movement detectors interface
     *
     * @return non null DetectorsManager
     */
    fun detectors(): DetectorsManager

    /**
     * Get the brushing interface
     *
     * @return non null Brushing
     */
    fun brushing(): Brushing

    /**
     * Get the toothbrush parameters interface
     *
     * @return non null Parameters
     */
    fun parameters(): Parameters

    /**
     * Get the toothbrush's root interface
     *
     *
     * Make sure you know what you do when accessing this part of the SDK Kolibree V1 toothbrushes
     * don't have root access (all methods will raise an error)
     *
     * @return non null Root
     */
    fun root(): Root

    /**
     * Get the Vibration Speed (Brushing Mode) manager
     *
     * Not available on every devices
     *
     * @see [BrushingModeManager]
     */
    fun brushingMode(): BrushingModeManager

    /** Disconnect toothbrush  */
    fun disconnect()

    /**
     * Get toothbrush users settings interface
     *
     * @return non null User
     */
    fun userMode(): User

    /**
     * @return Observable that will emit true if there's an OTA available, false otherwise. Each
     * invocation can return a new instance
     */
    fun hasOTAObservable(): Observable<Boolean>

    /**
     * Check if the toothbrushes uses some detectors using GRU weights, and if they have to be updated
     * alongside the firmware
     *
     * @return true if the GRU weights have to be updated OTA, false otherwise
     */
    fun supportsGRUUpdates(): Boolean
}

internal typealias Mac = String
