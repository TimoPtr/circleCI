/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.lowbattery

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.LowBatteryItem
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelPercentage
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit.MILLISECONDS
import org.junit.Assert.fail
import org.junit.Test

class LowBatteryViewModelTest : BaseUnitTest() {

    lateinit var viewModel: LowBatteryViewModel

    private val batteryLevelUseCase: BatteryLevelUseCase = mock()
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()
    private val connectionProvider: KLTBConnectionProvider = mock()
    private val lowBatteryUseCase: LowBatteryUseCase = mock()
    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority> = mock()
    private val navigator: HumHomeNavigator = mock()
    private val timeScheduler = TestScheduler()

    override fun setup() {
        super.setup()

        viewModel = LowBatteryViewModel(
            batteryLevelUseCase,
            toothbrushConnectionStateViewModel,
            connectionProvider,
            lowBatteryUseCase,
            priorityItemUseCase,
            navigator,
            timeScheduler
        )
    }

    @Test
    fun `monitorLowBattery display the popup if the TB battery is below 15% and the warning has not been shown before`() {
        val toothbrushMac = "123"
        val toothbrushName = "Toto"
        val batteryLevel = LevelPercentage(10)

        mockHappyPath(toothbrushMac, toothbrushName, batteryLevel)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        timeScheduler.advanceTimeBy(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS)

        verify(priorityItemUseCase).submitAndWaitFor(LowBatteryItem)
        verify(lowBatteryUseCase).setWarningShown()
        verify(navigator).showLowBatteryDialog(toothbrushName)
    }

    @Test
    fun `monitorLowBattery does not call any method if TB state is other than SingleToothbrushConnected`() {
        val toothbrushMac = "123"
        val toothbrushName = "Toto"
        val batteryLevel = LevelPercentage(10)

        mockHappyPath(toothbrushMac, toothbrushName, batteryLevel)

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(state = SingleToothbrushDisconnected("123"))
            )
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        timeScheduler.advanceTimeBy(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS)

        verifyZeroInteractions(
            batteryLevelUseCase, connectionProvider, lowBatteryUseCase,
            priorityItemUseCase, navigator
        )
    }

    @Test
    fun `monitorLowBattery does not call any method if an active connection send an error fails`() {
        val toothbrushMac = "123"
        val toothbrushName = "Toto"
        val batteryLevel = LevelPercentage(10)

        mockHappyPath(toothbrushMac, toothbrushName, batteryLevel)

        whenever(connectionProvider.existingActiveConnection(toothbrushMac))
            .thenReturn(Single.error(Exception()))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        timeScheduler.advanceTimeBy(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS)

        verifyZeroInteractions(
            batteryLevelUseCase, lowBatteryUseCase, priorityItemUseCase, navigator
        )
    }

    @Test
    fun `if isMatchingWarningRequirement returns false the dialog should be displayed`() {
        val toothbrushMac = "123"
        val toothbrushName = "Toto"
        val batteryLevel = LevelPercentage(10)

        mockHappyPath(toothbrushMac, toothbrushName, batteryLevel)

        whenever(lowBatteryUseCase.isMatchingWarningRequirement(batteryLevel))
            .thenReturn(Single.just(false))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        timeScheduler.advanceTimeBy(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS)

        verifyZeroInteractions(priorityItemUseCase, navigator)
    }

    @Test
    fun `if LowBatteryItem is not consumed, then the dialog should not have been displayed`() {
        val toothbrushMac = "123"
        val toothbrushName = "Toto"
        val batteryLevel = LevelPercentage(10)

        mockHappyPath(toothbrushMac, toothbrushName, batteryLevel)

        whenever(priorityItemUseCase.submitAndWaitFor(LowBatteryItem)).thenReturn(Completable.never())
        whenever(lowBatteryUseCase.setWarningShown()).thenReturn(Completable.fromCallable {
            fail("setWarningShown should not be called")
        })

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        timeScheduler.advanceTimeBy(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS)

        verifyZeroInteractions(navigator)
    }

    @Test
    fun `onLowBatteryDismissed should mark as displayed the Item`() {
        viewModel.onLowBatteryDismissed()

        verify(priorityItemUseCase).markAsDisplayed(LowBatteryItem)
    }

    private fun mockHappyPath(
        toothbrushMac: String,
        toothbrushName: String,
        batteryLevel: LevelPercentage
    ) {
        val connection = getHappyPathConnection(toothbrushName)

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(state = SingleToothbrushConnected(toothbrushMac))
            )
        )
        whenever(connectionProvider.existingActiveConnection(toothbrushMac))
            .thenReturn(Single.just(connection))
        whenever(batteryLevelUseCase.batteryLevel(connection))
            .thenReturn(Single.just(batteryLevel))
        whenever(lowBatteryUseCase.isMatchingWarningRequirement(batteryLevel))
            .thenReturn(Single.just(true))
        whenever(priorityItemUseCase.submitAndWaitFor(LowBatteryItem)).thenReturn(Completable.complete())
        whenever(lowBatteryUseCase.setWarningShown()).thenReturn(Completable.complete())
    }

    private fun getHappyPathConnection(toothbrushName: String): InternalKLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess()
            .withName(toothbrushName)
            .withState(KLTBConnectionState.ACTIVE).build()
    }
}

private const val DELAY_QUERY_BATTERY_MILLIS = 2000L
