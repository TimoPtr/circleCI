/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_3_MONTHS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_6_MONTHS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_CHANGE
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_CUT_OFF
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_FEW_DAYS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_FEW_WEEKS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_UNKNOWN
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCaseImpl.Companion.CONNECT_E2_STABLE_BL_VERSION
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker.CONNECTION_NOT_ACTIVE
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker.NOT_CHARGING
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker.NOT_ENOUGH_BATTERY
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker.NO_GRUWARE_DATA
import junit.framework.Assert.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

/** [CheckOtaUpdatePrerequisitesUseCaseImpl] unit tests */
class CheckOtaUpdatePrerequisitesUseCaseImplTest : BaseUnitTest() {

    /*
    ERROR_E2_NOT_CHARGING
     */

    @Test
    fun `check E2 need to be put on charger blocker is never send for other brush than E2 or Plaqless`() {
        ToothbrushModel
            .values()
            .filter { it != CONNECT_E2 && it != PLAQLESS }
            .forEach { model ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withModel(model)
                    .withOTAAvailable()
                    .withBattery(100, false)
                    .build()

                CheckOtaUpdatePrerequisitesUseCaseImpl()
                    .otaUpdateBlockersOnce(connection)
                    .test()
                    .assertValue {
                        it.isEmpty()
                    }
            }
    }

    @Test
    fun `check PLAQLESS need to be put on charger blocker present when PLAQLESS not on the charger`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(PLAQLESS)
            .withOTAAvailable()
            .withBattery(100, false)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.size == 1 }
            .assertValue { it.first() == NOT_CHARGING }
    }

    @Test
    fun `check PLAQLESS need to be put on charger blocker not present when PLAQLESS is on the charger`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(PLAQLESS)
            .withOTAAvailable()
            .withBattery(100, true)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `check E2 need to be put on charger blocker present when E2 not on the charger`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E2)
            .withBootloaderVersion("1.0.0")
            .withOTAAvailable()
            .withBattery(100, false)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.size == 1 }
            .assertValue { it.first() == NOT_CHARGING }
    }

    @Test
    fun `check E2 need to be put on charger blocker not present when E2 is on the charger`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E2)
            .withOTAAvailable()
            .withBattery(100, true)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `check E2 need to be put on charger blocker not present when E2 not on the charger but newer version`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E2)
            .withBootloaderVersion(CONNECT_E2_STABLE_BL_VERSION)
            .withOTAAvailable()
            .withBattery(100, false)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `check E2 need to be put on charger blocker not present when E2 not on the charger but on bootloader`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E2)
            .withBattery(100, false)
            .withOTAAvailable()
            .withBootloaderVersion("4.4.4")
            .withBootloader(true)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `check PLAQLESS need to be put on charger blocker not present when PLAQLESS not on the charger but on bootloader`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(PLAQLESS)
            .withBattery(100, false)
            .withOTAAvailable()
            .withBootloaderVersion("4.4.4")
            .withBootloader(true)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    /*
    CONNECTION_NOT_ACTIVE
     */

    @Test
    fun `check connection status blocker when connection is not active`() {
        KLTBConnectionState.values()
            .filter { it != ACTIVE }
            .forEach { state ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withModel(CONNECT_E1)
                    .withBattery(100, false)
                    .withOTAAvailable()
                    .withState(state)
                    .build()

                CheckOtaUpdatePrerequisitesUseCaseImpl()
                    .otaUpdateBlockersOnce(connection)
                    .test()
                    .assertValue { it.size == 1 }
                    .assertValue { it.first() == CONNECTION_NOT_ACTIVE }
            }
    }

    @Test
    fun `check connection status blocker when connection active`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E1)
            .withBattery(100, false)
            .withOTAAvailable()
            .withState(ACTIVE)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    /*
    NOT_ENOUGH_BATTERY
     */
    @Test
    fun `check not enough battery with all brush models`() {
        ToothbrushModel
            .values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withModel(model)
                    .withBattery(0, true)
                    .withOTAAvailable()
                    .build()

                CheckOtaUpdatePrerequisitesUseCaseImpl()
                    .otaUpdateBlockersOnce(connection)
                    .test()
                    .assertValue { it.size == 1 }
                    .assertValue { it.first() == NOT_ENOUGH_BATTERY }
            }
    }

    @Test
    fun `check not enough battery is ignored when brush is in bootloader`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withBootloader(true)
            .withBattery(0, false)
            .withOTAAvailable()
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue { it.isEmpty() }
    }

    /*
    percentageLevelIsEnoughForOta()
    */
    @Test
    fun `check battery level with percentage detection threshold`() {
        val useCase = CheckOtaUpdatePrerequisitesUseCaseImpl()
        assertFalse(useCase.isBatteryLevelEnoughForOTA(7))
        assertFalse(useCase.isBatteryLevelEnoughForOTA(10))
        assertFalse(useCase.isBatteryLevelEnoughForOTA(25))
        assertFalse(useCase.isBatteryLevelEnoughForOTA(30))

        assertTrue(useCase.isBatteryLevelEnoughForOTA(31))
    }

    /*
  discreteLevelIsEnoughForOta()
   */
    @Test
    @Throws(Exception::class)
    fun `check battery level with discrete value detection threshold`() {
        val useCase = CheckOtaUpdatePrerequisitesUseCaseImpl()
        assertFalse(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_UNKNOWN))
        assertFalse(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_CUT_OFF))
        assertFalse(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_CHANGE))
        assertFalse(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_FEW_DAYS))
        assertTrue(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_FEW_WEEKS))
        assertTrue(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_3_MONTHS))
        assertTrue(useCase.isDiscreteBatteryLevelEnoughForOTA(BATTERY_6_MONTHS))
    }

    /*
    NO_GRUWARE_DATA
     */

    @Test
    fun `check no gruware data with all model`() {
        ToothbrushModel
            .values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withModel(model)
                    .withBattery(100, true)
                    .build()

                CheckOtaUpdatePrerequisitesUseCaseImpl()
                    .otaUpdateBlockersOnce(connection)
                    .test()
                    .assertValue { it.size == 1 }
                    .assertValue { it.first() == NO_GRUWARE_DATA }
            }
    }

    /*
    COMBINED
     */

    @Test
    fun `check all blockers for one connection can be combined`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withModel(CONNECT_E2)
            .withBattery(0, false)
            .withState(KLTBConnectionState.TERMINATED)
            .build()

        CheckOtaUpdatePrerequisitesUseCaseImpl()
            .otaUpdateBlockersOnce(connection)
            .test()
            .assertValue(OtaUpdateBlocker.values().toList())
    }
}
