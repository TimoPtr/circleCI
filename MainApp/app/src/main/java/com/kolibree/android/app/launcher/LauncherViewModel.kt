/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.migration.MigrationUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.app.ui.settings.secret.persistence.AppSessionFlags
import com.kolibree.android.app.update.AppUpdateUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManager
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class LauncherViewModel(
    private val navigator: LauncherNavigator,
    private val connector: IKolibreeConnector,
    private val memoryManager: MemoryManager,
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase,
    sessionFlags: AppSessionFlags,
    private val appUpdateUseCase: AppUpdateUseCase,
    private val migrationUseCase: MigrationUseCase
) : BaseViewModel<EmptyBaseViewState, LauncherActions>(EmptyBaseViewState) {

    init {
        /*
        This ViewModel is only created once per session. We are not interested in onCreate, which
        can potentially be invoked multiple times (configuration change, for example)
         */
        sessionFlags.onSessionStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        disposeOnPause {
            initialTasksSingle()
                .subscribeOn(Schedulers.io())
                .subscribe({ isLoggedIn ->
                    if (isLoggedIn) {
                        navigator.openHomeScreen()
                    } else {
                        navigator.openOnboarding()
                    }
                }, { e ->
                    Timber.e(e)
                    navigator.terminate()
                })
        }
    }

    private fun initialTasksSingle() = Single.zip(
        syncOnce().doOnSuccess { Timber.d("Sync completed") },
        loadJawsOnce().doOnSuccess { Timber.d("Jaws completed") },
        checkForUpdateAndUpdate().doOnSuccess { Timber.d("Update checked") },
        checkForMigration().doOnSuccess { Timber.d("Migration checked") },
        { isLoggedIn: Boolean, _: Unit, _: Unit, _: Unit -> isLoggedIn }
    )

    private fun loadJawsOnce(): Single<Unit> {
        return memoryManager.preloadFromAssets(Kolibree3DModel.HUM_UPPER_JAW)
            .andThen(memoryManager.preloadFromAssets(Kolibree3DModel.HUM_LOWER_JAW))
            .subscribeOn(Schedulers.io())
            .toSingleDefault(Unit)
    }

    private fun syncOnce(): Single<Boolean> {
        return connector.syncAndNotify().subscribeOn(Schedulers.io())
            .doOnSubscribe { userExpectsSmilesUseCase.onUserExpectsPoints(TrustedClock.getNowInstant()) }
    }

    private fun checkForUpdateAndUpdate(): Single<Unit> {
        return appUpdateUseCase.checkForUpdateAndMaybeUpdate()
            .flatMapSingleElement {
                pushAction(LauncherActions.OnUpdateNeeded(it))
                Single.never<Unit>() // block the flow since there is a mandatory update
            }.toSingle(Unit)
            .onErrorReturnItem(Unit) // we don't want to block the flow when an error occur
    }

    private fun checkForMigration(): Single<Unit> {
        return migrationUseCase.getMigrationsCompletable().toSingleDefault(Unit)
    }

    class Factory @Inject constructor(
        private val navigator: LauncherNavigator,
        private val connector: IKolibreeConnector,
        private val memoryManager: MemoryManager,
        private val sessionFlags: AppSessionFlags,
        private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase,
        private val appUpdateUseCase: AppUpdateUseCase,
        private val migrationUseCase: MigrationUseCase
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LauncherViewModel(
                navigator = navigator,
                connector = connector,
                memoryManager = memoryManager,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                sessionFlags = sessionFlags,
                appUpdateUseCase = appUpdateUseCase,
                migrationUseCase = migrationUseCase
            ) as T
    }
}
