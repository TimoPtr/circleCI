# What is it?
This module is responsible for all Kolibree toothbrush Bluetooth connections, commands and hardware-related functionnality.

# Dependencies

- Dagger

## Usage

### Dagger

-- includes ToothbrushModule.class_

Once that's set up, you can use _ToothbrushWrapper_.

## Tools

### ToothbrushWrapper interface

#### Methods

```
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
     * Get toothbrush's current user ID
     *
     * @return non null Single
     */
    fun getUserId(): Single<Long>

    /**
     * Set the toothbrush in single user mode and set the user ID
     *
     * @param userId long user ID
     * @return non null [Completable]
     */
    fun setUserId(userId: Long): Completable

    /**
     * Enable multi user mode
     * Offline brushing will not be recorded anymore
     *
     * @param enable true to enable multi user mode, false to disable it
     * @return non null [Completable]
     */
    fun setMultiMode(enable: Boolean): Completable

    /**
     * Get toothbrush's multi users mode state
     *
     * @return a Single that will emit true if the toothbrush is in multi users mode, false otherwise
     */
    fun isMultiModeEnabled(): Single<Boolean>

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
    fun setAndCacheName(name: String): Completable

    /**
     * Set the toothbrush name in the App
     *
     * @param name non null name
     */
    fun cacheName(name: String)

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
     * Check if the toothbrush firmware has valid GRU data for RNN detector
     *
     * @return true if the GRU data is valid, false otherwise
     */
    fun hasValidGruData(): Boolean


    /**
     * Check if the updates are available (GRU or firmware)
     *
     * @return non null [Single]
     */
    fun checkUpdates(): Single<GruwareData>
```



### ConnectionStateWrapper interface

#### Methods

```
interface ConnectionStateWrapper {

    /**
     * Get the current state of the connection
     *
     * @return non null [KLTBConnectionState]
     */
    fun getCurrent(): KLTBConnectionState

    /**
     * Return true if connected to toothbrush connection, false otherwise
     */
    fun isActive(): Boolean

    /**
     * check if the connection is registered or not
     */
    fun isRegistered(): Observable<Boolean>
}

```

This snippet of code is written in kotlin.
