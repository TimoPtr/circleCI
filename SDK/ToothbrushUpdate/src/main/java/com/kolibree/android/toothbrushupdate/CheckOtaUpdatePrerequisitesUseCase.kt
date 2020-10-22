/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

/** Checks whether an OTA update can be performed on the specified device */
@VisibleForApp
interface CheckOtaUpdatePrerequisitesUseCase {

    /**
     * Subscribe to this to check whether an OTA update can be performed
     *
     * @return [OtaUpdateBlocker] [List] [Single] sorted by enum order
     */
    fun otaUpdateBlockersOnce(connection: KLTBConnection): Single<List<OtaUpdateBlocker>>
}

/** [CheckOtaUpdatePrerequisitesUseCase] implementation */
internal class CheckOtaUpdatePrerequisitesUseCaseImpl @Inject constructor() :
    CheckOtaUpdatePrerequisitesUseCase {

    override fun otaUpdateBlockersOnce(connection: KLTBConnection): Single<List<OtaUpdateBlocker>> =
        Single.just<List<OtaUpdateBlocker>>(emptyList())
            .checkStatus(connection)
            .checkOnCharger(connection)
            .checkForBatteryLevel(connection)
            .checkGruwareData(connection)
            .map(Iterable<OtaUpdateBlocker>::sorted)

    private fun Single<List<OtaUpdateBlocker>>.checkOnCharger(
        connection: KLTBConnection
    ): Single<List<OtaUpdateBlocker>> = flatMap { blockers ->
        if (connection.requiresChargerWhileOta()) {
            checkForCharger(connection).addBlockerIfPresent(blockers)
        } else {
            Single.just(blockers)
        }
    }

    private fun KLTBConnection.requiresChargerWhileOta(): Boolean {
        return toothbrush().model in arrayOf(PLAQLESS, CONNECT_E2)
    }

    private fun checkForCharger(connection: KLTBConnection): Maybe<OtaUpdateBlocker> =
        connection.toothbrush().run {
            if (isRunningBootloader) {
                Maybe.empty()
            } else if (model == PLAQLESS || isE2WithBuggyFirmware()) {
                maybeConnectMustBeCharging(connection)
            } else {
                Maybe.empty()
            }
        }

    private fun Toothbrush.isE2WithBuggyFirmware(): Boolean {
        return model == CONNECT_E2 && CONNECT_E2_STABLE_BL_VERSION.isNewer(bootloaderVersion)
    }

    private fun Single<List<OtaUpdateBlocker>>.checkForBatteryLevel(
        connection: KLTBConnection
    ): Single<List<OtaUpdateBlocker>> = flatMap { blockers ->
        // In bootloader we can't check the battery level
        if (connection.toothbrush().isRunningBootloader) {
            Single.just(blockers)
        } else {
            isBatteryEnoughForOTA(connection)
                .flatMapMaybe { isEnough ->
                    if (isEnough) {
                        Maybe.empty()
                    } else {
                        Maybe.just(OtaUpdateBlocker.NOT_ENOUGH_BATTERY)
                    }
                }.addBlockerIfPresent(blockers)
        }
    }

    private fun Single<List<OtaUpdateBlocker>>.checkStatus(
        connection: KLTBConnection
    ): Single<List<OtaUpdateBlocker>> =
        map { blockers ->
            val result = blockers.toMutableList()
            if (connection.state().current != KLTBConnectionState.ACTIVE) {
                result.add(OtaUpdateBlocker.CONNECTION_NOT_ACTIVE)
            }
            result
        }

    private fun Single<List<OtaUpdateBlocker>>.checkGruwareData(
        connection: KLTBConnection
    ): Single<List<OtaUpdateBlocker>> =
        map { blockers ->
            val result = blockers.toMutableList()
            if (connection.tag !is GruwareData) {
                result.add(OtaUpdateBlocker.NO_GRUWARE_DATA)
            }
            result
        }

    @VisibleForTesting
    internal fun isBatteryEnoughForOTA(connection: KLTBConnection): Single<Boolean> =
        with(connection.toothbrush().battery()) {
            if (usesDiscreteLevels) {
                discreteBatteryLevel.map {
                    isDiscreteBatteryLevelEnoughForOTA(it)
                }
            } else {
                batteryLevel.map {
                    isBatteryLevelEnoughForOTA(it)
                }
            }
        }

    @VisibleForTesting
    internal fun isBatteryLevelEnoughForOTA(level: Int): Boolean =
        level > MIN_BATTERY_LEVEL

    @VisibleForTesting
    internal fun isDiscreteBatteryLevelEnoughForOTA(level: DiscreteBatteryLevel): Boolean =
        level == DiscreteBatteryLevel.BATTERY_6_MONTHS ||
            level == DiscreteBatteryLevel.BATTERY_3_MONTHS ||
            level == DiscreteBatteryLevel.BATTERY_FEW_WEEKS

    private fun maybeConnectMustBeCharging(connection: KLTBConnection): Maybe<OtaUpdateBlocker> =
        connection
            .toothbrush()
            .battery()
            .isCharging
            .flatMapMaybe { isCharging ->
                if (isCharging) {
                    Maybe.empty()
                } else {
                    Maybe.just(OtaUpdateBlocker.NOT_CHARGING)
                }
            }

    companion object {

        // https://kolibree.atlassian.net/browse/KLTB002-10510
        @VisibleForTesting
        val CONNECT_E2_STABLE_BL_VERSION = SoftwareVersion("2.2.5")

        /*
        Plaqless min battery is 30%. And, it doesn't hurt to enforce that on other toothbrushes

        https://kolibree.slack.com/archives/G7J9NDA72/p1600781544023600
         */
        private const val MIN_BATTERY_LEVEL = 30
    }
}

private fun Maybe<OtaUpdateBlocker>.addBlockerIfPresent(
    blockers: List<OtaUpdateBlocker>
): Single<List<OtaUpdateBlocker>> =
    map {
        blockers.toMutableList().apply { add(it) }.toList()
    }.toSingle(blockers)

/** OTA update blocker that prevents the process from being started */
@VisibleForApp // Kept for BtTester
enum class OtaUpdateBlocker {
    CONNECTION_NOT_ACTIVE,

    /**
     * E2 & Plaqless devices must be charging in order to perform an update
     */
    // https://kolibree.atlassian.net/browse/KLTB002-10510
    NOT_CHARGING,
    NOT_ENOUGH_BATTERY,
    NO_GRUWARE_DATA
}
