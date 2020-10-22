package com.kolibree.android.sdk.wrapper

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.sdk.connection.parameters.Parameters
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Keep
data class ToothbrushFacadeImpl(
    val connection: KLTBConnection,
    val toothbrush: Toothbrush,
    val user: User,
    val parameters: Parameters,
    val brushing: Brushing,
    val gruware: GruwareRepository,
    val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase
) : ToothbrushFacade {

    override fun state(): ConnectionStateWrapper {
        return ConnectionStateWrapperImpl(connection.state())
    }

    /**
     * @return Observable that will emit true if there's an OTA available, false otherwise. Each
     * invocation can return a new instance
     */
    override fun hasOTAObservable(): Observable<Boolean> {
        return connection.hasOTAObservable()
    }

    override fun setDefaultBrushingDuration(defaultDurationSeconds: Int): Completable {
        return brushing.setDefaultDuration(defaultDurationSeconds)
    }

    /**
     * Get default brushing session duration
     *
     * @return non null [Integer] [Single] seconds
     */
    override fun getDefaultBrushingDuration(): Single<Int> {
        return brushing.defaultDuration
    }

    /**
     * Set auto shutdown timeout
     *
     * @param autoShutdownTimeout auto shutdown timeout in seconds
     * @return non null [Completable]
     */
    override fun setAutoShutdownTimeout(autoShutdownTimeout: Int): Completable {
        return parameters.setAutoShutdownTimeout(autoShutdownTimeout)
    }

    /**
     * Get auto shutdown timeout
     *
     * @return non null [Integer] invoker
     */
    override fun getAutoShutdownTimeout(): Single<Int> {
        return parameters.autoShutdownTimeout
    }

    override fun getProfileId(): Single<Long> = user.profileId()

    override fun enableSharedMode(): Completable = user.enableSharedMode()

    override fun isSharedModeEnabled(): Single<Boolean> = user.isSharedModeEnabled()

    override fun setProfileId(profileId: Long): Completable = user.setProfileId(profileId)
        .andThen(synchronizeBrushingModeUseCase.syncLocalBrushingModeToToothbrush(connection))

    /**
     * Get the toothbrush bluetooth name
     *
     * @return bluetooth device name
     */
    override fun getName(): String {
        return toothbrush.getName()
    }

    /**
     * Set the toothbrush bluetooth name
     *
     * @param name non null name
     * @return non null [Completable]
     */
    override fun setName(name: String): Completable {
        return toothbrush.setAndCacheName(name)
    }

    /**
     * Get toothbrush bluetooth mac address
     *
     * @return non null String encoded mac address
     */
    override fun getMac(): String {
        return toothbrush.mac
    }

    /**
     * Get toothbrush model
     *
     * @return non null [ToothbrushModel]
     */
    override fun getModel(): ToothbrushModel {
        return toothbrush.model
    }

    /**
     * Get the toothbrush hardware version
     *
     * @return non null [HardwareVersion]
     */
    override fun getHardwareVersion(): HardwareVersion {
        return toothbrush.hardwareVersion
    }

    /**
     * Get the toothbrush serial number
     *
     * @return non null serial number
     */
    override fun getSerialNumber(): String {
        return toothbrush.serialNumber
    }

    /**
     * Get the firmware version
     *
     * @return non null [SoftwareVersion]
     */
    override fun getFirmwareVersion(): SoftwareVersion {
        return toothbrush.firmwareVersion
    }

    /**
     * Check if the firmware is running in bootloader mode
     *
     * @return true if running in bootloader mode, false otherwise
     */
    override fun isRunningBootloader(): Boolean {
        return toothbrush.isRunningBootloader
    }

    /**
     * Check if the battery is currently being charged
     *
     * @return non null [Single]
     */
    override fun isCharging(): Single<Boolean> {
        return toothbrush.battery().isCharging
    }

    /**
     * Get the battery level in percents
     *
     * @return non null [Single]
     */
    override fun getBatteryLevel(): Single<Int> =
        toothbrush.battery().batteryLevel

    override fun getBatteryDiscreteLevel(): Single<DiscreteBatteryLevel> =
        toothbrush.battery().discreteBatteryLevel

    override fun batteryUsesDiscreteLevels(): Boolean =
        toothbrush.battery().usesDiscreteLevels

    override fun hasValidGruData(): Boolean {
        val detector = connection.detectors().mostProbableMouthZones()
        return when {
            detector != null -> detector.hasValidGruData()
            else -> false
        }
    }

    /**
     * @return Single that will emit {@link GruwareData} for the specified parameters. It might be
     *     cached or it might imply a remote request
     *     <p>It will emit a {@link java.io.IOException} if there were errors checking or downloading
     *     the files
     */
    @SuppressLint("DefaultLocale")
    override fun checkUpdates(): Single<GruwareData> {
        return gruware.getGruwareInfo(
            toothbrush.model.internalName.toLowerCase(),
            toothbrush.hardwareVersion.toString(),
            toothbrush.serialNumber,
            toothbrush.firmwareVersion.toString()
        )
    }
}
