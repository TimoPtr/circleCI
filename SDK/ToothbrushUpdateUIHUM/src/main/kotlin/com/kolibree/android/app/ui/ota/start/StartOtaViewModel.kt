/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.ota.OtaUpdateNavigator
import com.kolibree.android.app.ui.ota.OtaUpdateParams
import com.kolibree.android.app.ui.ota.OtaUpdateSharedViewModel
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

internal class StartOtaViewModel(
    private val sharedViewModel: OtaUpdateSharedViewModel,
    private val navigator: OtaUpdateNavigator,
    private val otaUpdateParams: OtaUpdateParams,
    private val checkOtaUpdatePrerequisitesUseCase: CheckOtaUpdatePrerequisitesUseCase,
    private val otaChecker: OtaChecker,
    private val timeScheduler: Scheduler
) : BaseViewModel<EmptyBaseViewState, BaseAction>(EmptyBaseViewState),
    OtaUpdateSharedViewModel by sharedViewModel {

    val isRechargeableBrush: Boolean = otaUpdateParams.toothbrushModel.isRechargeable()

    val isMandatoryUpdate: Boolean = otaUpdateParams.isMandatory

    fun onUpgradeClick() {
        StartOtaAnalytics.startUpgrade()
        hideError()
        disposeOnCleared {
            checkPrerequisites()
        }
    }

    fun onCancelClick() {
        StartOtaAnalytics.cancelUpgrade()
        navigator.finishScreen()
    }

    private fun checkPrerequisites(): Disposable =
        checkPrerequisitesWithMinimumTime()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgress(true) }
            .doFinally { showProgress(false) }
            .subscribe({
                if (it.isEmpty()) {
                    navigator.navigatesToInProgress()
                } else {
                    Timber.e("Got errors = ${it.joinToString(",")}")
                    handleBlocker(it)
                }
            }, ::handleError)

    private fun handleError(exception: Throwable) {
        Timber.e(exception, "error while checking the prerequisites")
        val error: Error = when (exception) {
            is ConnectionNotFoundException -> {
                Error.from(messageId = R.string.ota_blocker_not_active_connection)
            }
            is NetworkNotAvailableException -> {
                Error.from(messageId = R.string.ota_blocker_no_internet)
            }
            else -> Error.from(exception)
        }
        showError(error)
    }

    private fun handleBlocker(blockers: List<OtaUpdateBlocker>) {
        FailEarly.failInConditionMet(blockers.isEmpty(), "There is no blocker to handle")
        showError(mapBlockerToError(blockers.first()))
    }

    /*
    Ensure a minimum delay to show progress dialog
     */
    private fun checkPrerequisitesWithMinimumTime(): Single<List<OtaUpdateBlocker>> = Single.zip(
        getConnection().flatMap(checkOtaUpdatePrerequisitesUseCase::otaUpdateBlockersOnce),
        Single.timer(1, TimeUnit.SECONDS, timeScheduler),
        BiFunction<List<OtaUpdateBlocker>, Long, List<OtaUpdateBlocker>> { blockers, _ ->
            blockers
        }
    )

    /*
    Get the connection by using otaChecker that will also if needed download the firmware
     */
    private fun getConnection(): Single<KLTBConnection> =
        otaChecker.otaForConnectionsOnce().subscribeOn(Schedulers.io())
            .filter { it.connection.mac() == otaUpdateParams.mac }
            .timeout(
                TOOTHBRUSH_NOT_FOUND_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
                timeScheduler,
                Observable.error(ConnectionNotFoundException("impossible to retrieve the connection $otaUpdateParams"))
            )
            .take(1)
            .singleOrError()
            .map(OtaForConnection::connection)

    class Factory @Inject constructor(
        private val sharedViewModel: OtaUpdateSharedViewModel,
        private val navigator: OtaUpdateNavigator,
        private val otaUpdateParams: OtaUpdateParams,
        private val checkOtaUpdatePrerequisitesUseCase: CheckOtaUpdatePrerequisitesUseCase,
        private val otaChecker: OtaChecker,
        @SingleThreadScheduler private val timeScheduler: Scheduler
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StartOtaViewModel(
                sharedViewModel,
                navigator,
                otaUpdateParams,
                checkOtaUpdatePrerequisitesUseCase,
                otaChecker,
                timeScheduler
            ) as T
    }
}

@VisibleForTesting
internal class ConnectionNotFoundException(override val message: String?) : RuntimeException()

private const val TOOTHBRUSH_NOT_FOUND_TIMEOUT_SECONDS = 30L
