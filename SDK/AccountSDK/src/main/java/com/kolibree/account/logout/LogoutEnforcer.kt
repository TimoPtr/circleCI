/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.kolibree.account.eraser.UserSessionManager
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.extensions.forceDispose
import com.kolibree.sdkws.core.OnUserLoggedInCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import javax.inject.Inject
import timber.log.Timber

/**
 * Detects force logout scenarios and reacts wiping user stored content
 *
 * If the injected IntentAfterForcedLogout is not null, it enforces navigation to it
 */
internal interface LogoutEnforcer : OnUserLoggedInCallback {
    @MainThread
    fun initWatch()

    @MainThread
    fun stopWatch()
}

@AppScope
internal class LogoutEnforcerImpl @Inject constructor(
    context: Context,
    private val shouldLogoutUseCase: ShouldLogoutUseCase,
    private val userSessionManager: UserSessionManager,
    private val createdActivitiesWatcher: CreatedActivitiesWatcher,
    private val intentAfterForcedLogout: IntentAfterForcedLogout?
) : LogoutEnforcer {

    override fun onUserLoggedIn() = initWatch()

    override fun onUserLoggedOut() = stopWatch()

    private val appContext = context.applicationContext

    @VisibleForTesting
    var forceLogoutDisposable: Disposable? = null

    @MainThread
    override fun initWatch() {
        Timber.d("Enforcer starting watch with disposable %s", forceLogoutDisposable)
        if (canStartWatch()) {
            forceLogoutDisposable = shouldLogoutUseCase.shouldLogoutStream
                .doOnSubscribe { startWatchingActivities() }
                .subscribeOn(Schedulers.io())
                /*
                We only care about 1 logout event per watch

                Once the event is consumed, we stop the watch until a new login invokes initWatch
                 */
                .take(1)
                .doOnNext { Timber.w("Enforcer Logout stream emitting") }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.w("Enforcer Logout stream emitting on main thread") }
                .doAfterTerminate { stopWatch() }
                .subscribe(
                    this::onForcedLogout,
                    Timber::e
                ) { Timber.d("Enforcer on complete") }
        }
    }

    @VisibleForTesting
    fun startWatchingActivities() {
        if (shouldManageNavigation()) {
            Timber.w("Enforcer started watching activities")
            createdActivitiesWatcher.clear()

            application().registerActivityLifecycleCallbacks(createdActivitiesWatcher)
        }
    }

    private fun application() = (appContext as Application)

    @VisibleForTesting
    fun canStartWatch() = forceLogoutDisposable?.isDisposed ?: true

    @MainThread
    override fun stopWatch() {
        stopWatchingActivities()
        Timber.d("Enforcer stopping watch with disposable %s", forceLogoutDisposable)

        forceLogoutDisposable.forceDispose()

        forceLogoutDisposable = null
    }

    @VisibleForTesting
    fun onForcedLogout(reason: ForceLogoutReason) {
        if (shouldManageNavigation()) {
            forceNavigation(reason)
        }

        userSessionManager.reset()
    }

    @VisibleForTesting
    fun forceNavigation(reason: ForceLogoutReason) {
        stopWatchingActivities()

        navigateToAfterLogoutScreen(reason)

        createdActivitiesWatcher.finishActivitiesReverseOrder()
    }

    @VisibleForTesting
    fun stopWatchingActivities() {
        if (shouldManageNavigation()) {
            application().unregisterActivityLifecycleCallbacks(createdActivitiesWatcher)
        }
    }

    @VisibleForTesting
    fun navigateToAfterLogoutScreen(reason: ForceLogoutReason) {
        intentAfterForcedLogout?.apply {
            putExtra(EXTRA_FORCED_LOGOUT, reason)
            appContext.startActivity(this)
        }
    }

    @VisibleForTesting
    fun shouldManageNavigation(): Boolean = intentAfterForcedLogout != null
}

/**
 * If present, it contains the [ForceLogoutReason] why we forced a logout
 */
@Keep
const val EXTRA_FORCED_LOGOUT: String = "extra_forced_logout"

@Keep
sealed class ForceLogoutReason : Serializable

@Keep
object AccountDoesNotExist : ForceLogoutReason()

@Keep
object RefreshTokenFailed : ForceLogoutReason()
