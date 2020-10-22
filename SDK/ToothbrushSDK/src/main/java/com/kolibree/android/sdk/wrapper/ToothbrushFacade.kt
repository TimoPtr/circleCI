package com.kolibree.android.sdk.wrapper

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Keep
interface ToothbrushFacade {

    /**
     * Get the toothbrush connection state interface
     *
     * @return non null [ConnectionStateWrapper]
     */
    fun state(): ConnectionStateWrapper

    /**
     * @return Observable that will emit true if there's an OTA available, false otherwise. Each
     * invocation can return a new instance
     */
    fun hasOTAObservable(): Observable<Boolean>

    /**
     * Set default brushing duration in seconds
     *
     * @param defaultDurationSeconds int seconds
     * @return non null [Completable]
     */
    fun setDefaultBrushingDuration(defaultDurationSeconds: Int): Completable

    /**
     * Get default brushing session duration
     *
     * @return non null [Integer] [Single] seconds
     */
    fun getDefaultBrushingDuration(): Single<Int>

    /**
     * Set auto shutdown timeout
     *
     * @param autoShutdownTimeout auto shutdown timeout in seconds
     * @return non null [Completable]
     */
    fun setAutoShutdownTimeout(autoShutdownTimeout: Int): Completable

    /**
     * Get auto shutdown timeout
     *
     * @return non null [Integer] invoker
     */
    fun getAutoShutdownTimeout(): Single<Int>

    /**
     * Get toothbrush's current profile ID
     *
     * @return non null Single
     */
    fun getProfileId(): Single<Long>

    /**
     * Set the toothbrush profile ID
     *
     * This will disable the shared mode, if it was enabled
     *
     * @param profileId long profile ID
     * @return non null [Completable]
     */
    fun setProfileId(profileId: Long): Completable

    /**
     * Enable shared mode for this toothbrush
     *
     * The profile ID will be erased so the toothbrush can be used by multiple profiles
     *
     * @return non null [Completable]
     */
    fun enableSharedMode(): Completable

    /**
     * Get toothbrush's shared mode state
     *
     * @return a Single that will emit true if the toothbrush is in shared mode, false otherwise
     */
    fun isSharedModeEnabled(): Single<Boolean>

    /**
     * Get the toothbrush bluetooth name
     *
     * @return bluetooth device name
     */
    fun getName(): String

    /**
     * Set the toothbrush bluetooth name
     *
     * @param name non null name
     * @return non null [Completable]
     */
    fun setName(name: String): Completable

    /**
     * Get toothbrush bluetooth mac address
     *
     * @return non null String encoded mac address
     */
    fun getMac(): String

    /**
     * Get toothbrush model
     *
     * @return non null [ToothbrushModel]
     */
    fun getModel(): ToothbrushModel

    /**
     * Get the toothbrush hardware version
     *
     * @return non null [HardwareVersion]
     */
    fun getHardwareVersion(): HardwareVersion

    /**
     * Get the toothbrush serial number
     *
     * @return non null serial number
     */
    fun getSerialNumber(): String

    /**
     * Get the firmware version
     *
     * @return non null [SoftwareVersion]
     */
    fun getFirmwareVersion(): SoftwareVersion

    /**
     * Check if the firmware is running in bootloader mode
     *
     * @return true if running in bootloader mode, false otherwise
     */
    fun isRunningBootloader(): Boolean

    /**
     * Check if the battery is currently being charged
     *
     * @return non null [Single]
     */
    fun isCharging(): Single<Boolean>

    /**
     * Get the battery level in percents
     *
     * @return non null [Single]
     */
    fun getBatteryLevel(): Single<Int>

    /**
     * Get the battery level in discrete levels
     *
     * @return non null [Single]
     */
    fun getBatteryDiscreteLevel(): Single<DiscreteBatteryLevel>

    /**
     * Check if the battery outputs discrete levels or percentage
     *
     * @return true if getDiscreteBatteryLevel should be used, false for getBatteryLevel
     */
    fun batteryUsesDiscreteLevels(): Boolean

    /**
     * Check if the toothbrush firmware has valid GRU data for RNN detector
     *
     * @return true if the GRU data is valid, false otherwise
     */
    fun hasValidGruData(): Boolean

    /**
     * Check if the updates are available (GRU or firmware)
     *
     * @return Single that will emit [GruwareData] for the specified parameters. It might be cached
     * or it might imply a remote request
     *
     * It will emit a [IOException] if there were errors checking or downloading the files
     */
    fun checkUpdates(): Single<GruwareData>
}
