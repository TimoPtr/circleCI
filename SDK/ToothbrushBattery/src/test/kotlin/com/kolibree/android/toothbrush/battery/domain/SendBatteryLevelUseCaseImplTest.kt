/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import com.kolibree.android.toothbrush.battery.data.model.ToothbrushBatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelDiscrete
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Test

class SendBatteryLevelUseCaseImplTest : BaseUnitTest() {

    private val accountDatastore: AccountDatastore = mock()
    private val workerConfigurator: SendBatteryLevelWorker.Configurator = mock()

    private val testAccount = AccountInternal().apply {
        id = 1
        currentProfileId = 1
    }

    private lateinit var useCase: SendBatteryLevelUseCase

    override fun setup() {
        super.setup()
        useCase = SendBatteryLevelUseCaseImpl(
            accountDatastore, workerConfigurator
        )

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(testAccount))

        whenever(workerConfigurator.sendBatteryLevel(any(), any(), any()))
            .thenReturn(Completable.complete())
    }

    @Test
    fun `formats all request fields correctly`() {
        val testBrushBatteryLevel = ToothbrushBatteryLevel(
            macAddress = "AA:10:30:B7:II",
            serialNumber = "SERIAL123",
            batteryLevel = BatteryLevel.LevelUnknown
        )

        val testObserver = useCase.sendBatteryLevel(testBrushBatteryLevel).test()
        testObserver.assertComplete()

        verify(workerConfigurator).sendBatteryLevel(
            accountId = testAccount.id,
            profileId = testAccount.currentProfileId!!,
            request = SendBatteryLevelRequest(
                macAddress = StrippedMac.fromMac(testBrushBatteryLevel.macAddress),
                serialNumber = testBrushBatteryLevel.serialNumber,
                discreteLevel = DISCRETE_LEVEL_BATTERY_UNKNOWN
            )
        )
    }

    @Test
    fun `does not send request when account is not available`() {
        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.empty())

        val testObserver = useCase.sendBatteryLevel(mock()).test()
        testObserver.assertComplete()

        verifyNoMoreInteractions(workerConfigurator)
    }

    @Test
    fun `properly maps discrete battery levels`() {
        val levels: List<Pair<BatteryLevel, Int>> = listOf(
            LevelDiscrete(Discrete.Level6Month) to DISCRETE_LEVEL_BATTERY_6_MONTHS,
            LevelDiscrete(Discrete.Level3Month) to DISCRETE_LEVEL_BATTERY_3_MONTHS,
            LevelDiscrete(Discrete.LevelFewWeeks) to DISCRETE_LEVEL_BATTERY_FEW_WEEKS,
            LevelDiscrete(Discrete.LevelFewDays) to DISCRETE_LEVEL_BATTERY_FEW_DAYS
        )

        for ((level, expectedValue) in levels) {
            val testBrushBatteryLevel = ToothbrushBatteryLevel(
                macAddress = "mac",
                serialNumber = "serial",
                batteryLevel = level
            )

            val testObserver = useCase.sendBatteryLevel(testBrushBatteryLevel).test()
            testObserver.assertComplete()

            verify(workerConfigurator).sendBatteryLevel(
                any(), any(), eq(SendBatteryLevelRequest(StrippedMac.fromMac("mac"), "serial", expectedValue))
            )
        }
    }
}
