/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.lowbattery

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.LowBatteryItem
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import timber.log.Timber

internal interface LowBatteryCallback {
    fun onLowBatteryDismissed()
}

@VisibleForApp
class LowBatteryViewModel(
    private val batteryLevelUseCase: BatteryLevelUseCase,
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val connectionProvider: KLTBConnectionProvider,
    private val lowBatteryUseCase: LowBatteryUseCase,
    private val displayPriorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
    private val navigator: HumHomeNavigator,
    private val timeScheduler: Scheduler
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState), LowBatteryCallback {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        disposeOnPause(::monitorLowBattery)
    }

    private fun monitorLowBattery(): Disposable {
        return toothbrushConnectionStateViewModel.viewStateFlowable
            .map { it.state }.ofType(SingleToothbrushConnected::class.java)
            .switchMapSingle { connectionProvider.existingActiveConnection(it.mac) }
            .flatMapMaybe(::isLowBatteryDisplayable)
            .flatMapCompletable(::displayLowBattery)
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    /**
     * Retrieve the battery state and if it is matching the requirement,
     * returns the Toothbrush's [KLTBConnection], otherwise the Maybe does not emits any values
     */
    private fun isLowBatteryDisplayable(connection: KLTBConnection): Maybe<KLTBConnection> =
        Single.timer(DELAY_QUERY_BATTERY_MILLIS, MILLISECONDS, timeScheduler)
            .flatMap { batteryLevelUseCase.batteryLevel(connection) }
            .flatMap(lowBatteryUseCase::isMatchingWarningRequirement)
            .filter { it }
            .map { connection }

    /**
     * Submit the [LowBatteryItem] item to the Priority Queue and show the Dialog once it is ready
     * to be displayed
     */
    private fun displayLowBattery(connection: KLTBConnection): Completable {
        return displayPriorityItemUseCase.submitAndWaitFor(LowBatteryItem)
            .andThen(lowBatteryUseCase.setWarningShown())
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromCallable {
                navigator.showLowBatteryDialog(connection.toothbrush().getName())
            })
    }

    override fun onLowBatteryDismissed() {
        displayPriorityItemUseCase.markAsDisplayed(LowBatteryItem)
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val batteryLevelUseCase: BatteryLevelUseCase,
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val connectionProvider: KLTBConnectionProvider,
        private val lowBatteryUseCase: LowBatteryUseCase,
        private val displayPriorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
        private val navigator: HumHomeNavigator,
        @SingleThreadScheduler private val timeScheduler: Scheduler
    ) : BaseViewModel.Factory<BaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LowBatteryViewModel(
                batteryLevelUseCase,
                toothbrushConnectionStateViewModel,
                connectionProvider,
                lowBatteryUseCase,
                displayPriorityItemUseCase,
                navigator,
                timeScheduler
            ) as T
    }
}

private const val DELAY_QUERY_BATTERY_MILLIS = 2000L
