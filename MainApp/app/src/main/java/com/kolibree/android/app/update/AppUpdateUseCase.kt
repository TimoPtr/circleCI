/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.update

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.crashlogger.CrashLogger
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import javax.inject.Inject
import timber.log.Timber

/**
 *
 * In order to test the In-App update library with the Play Store, the easiest way is to
 * publish two internal AAB (it can be a debug one) https://play.google.com/console/internal-app-sharing
 * with two different version codes, install the one with lower version code, using the generated link, and then
 * open the link to the second one - do not perform the update, it's just here to warm up the cache of the Play Store -
 * and finally, open back the application (you might have to kill the app first and open it again).
 *
 * While releasing a version to the Play Store please be aware of this priority classification it
 * will change the behavior of this app.
 *
 * Priority [0-5]:
 * 0 ->     We don't show anything, the update is considered as minor
 * [1-4] -> We add new cool feature or perf improvement to the app but we don't need the update to
 *              be mandatory. We will show a dialog to do the update ([AppUpdateType.FLEXIBLE])
 * 5 ->     This update is critical, without it the user cannot use the brush, or we found the security
 *          issue, or a crash. We will show a new screen (the app will be close) to do the
 *              mandatory update ([AppUpdateType.IMMEDIATE])
 *
 * TODO : Handle Flexible update https://kolibree.atlassian.net/browse/KLTB002-12707
 */
@VisibleForApp
interface AppUpdateUseCase {
    fun checkForUpdateAndMaybeUpdate(): Maybe<AppUpdateRequested>
}

internal class AppUpdateUseCaseImpl @VisibleForTesting constructor(
    private val appUpdateManager: AppUpdateManager
) : AppUpdateUseCase {

    @Inject
    constructor(context: Context) : this(AppUpdateManagerFactory.create(context.applicationContext))

    override fun checkForUpdateAndMaybeUpdate(): Maybe<AppUpdateRequested> =
        Maybe.create<AppUpdateRequested> { emitter ->
            runCatching {
                appUpdateManager.appUpdateInfo.setup(emitter)
            }.getOrElse {
                Timber.e(it, "Impossible to check for update availability")
                emitter.onComplete()
            }
        }

    private fun Task<AppUpdateInfo>.setup(emitter: MaybeEmitter<AppUpdateRequested>) {
        addOnSuccessListener { appUpdateInfo ->
            onUpdateAvailabilitySuccess(emitter, appUpdateInfo)
        }
        addOnFailureListener {
            onUpdateAvailabilityFailure(emitter, it)
        }
    }

    private fun onUpdateAvailabilitySuccess(
        emitter: MaybeEmitter<AppUpdateRequested>,
        appUpdateInfo: AppUpdateInfo
    ) {
        when {
            appUpdateInfo.isMandatoryUpdate() -> startImmediateUpdate(
                emitter,
                appUpdateInfo
            )
            appUpdateInfo.isOptionalUpdate() -> {
                // TODO Handle Flexible update https://kolibree.atlassian.net/browse/KLTB002-12707
                Timber.w("Optional update available but Flexible update is not implemented yet")
                emitter.onComplete()
            }
            else -> {
                onNoUpdateNeeded(appUpdateInfo)
                emitter.onComplete()
            }
        }
    }

    private fun AppUpdateInfo.isMandatoryUpdate(): Boolean {
        val isUpdateInProgress =
            updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

        val isUpdateMandatoryAvailable =
            updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                updatePriority() >= MIN_PRIORITY_FOR_MANDATORY_UPDATE &&
                isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

        return isUpdateMandatoryAvailable || isUpdateInProgress
    }

    private fun onNoUpdateNeeded(appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            Timber.i("An update is available but because of priority we don't do anything")
        } else {
            Timber.i("No update available")
        }
    }

    private fun AppUpdateInfo.isOptionalUpdate(): Boolean {
        // TODO check for instance if the update has been download but not installed
        // https://developer.android.com/guide/playcore/in-app-updates?authuser=2#install_flexible

        return updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            updatePriority() in RANGE_PRIORITY_FOR_OPTIONAL_UPDATE &&
            isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
    }

    private fun onUpdateAvailabilityFailure(
        emitter: MaybeEmitter<AppUpdateRequested>,
        error: Throwable
    ) {
        Timber.e(error, "Error while trying to check for updates")
        CrashLogger.logException(error)
        emitter.tryOnError(error)
    }

    private fun startImmediateUpdate(
        emitter: MaybeEmitter<AppUpdateRequested>,
        appUpdateInfo: AppUpdateInfo
    ) {
        Timber.i("Update available starting immediate update")
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                getIntentSenderForResultStarter(emitter),
                APP_UPDATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Timber.e(e, "Impossible to request intent to get the update stats")
            CrashLogger.logException(e)
            emitter.tryOnError(e)
        }
    }

    private fun getIntentSenderForResultStarter(
        emitter: MaybeEmitter<AppUpdateRequested>
    ): IntentSenderForResultStarter = IntentSenderForResultStarter { intentSender: IntentSender,
        requestCode: Int, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: Bundle? ->
        emitter.onSuccess(
            AppUpdateRequested(
                intentSender,
                requestCode,
                fillInIntent,
                flagsMask,
                flagsValues,
                extraFlags,
                options
            )
        )
    }
}

private const val APP_UPDATE_REQUEST_CODE = 911119

private const val MIN_PRIORITY_FOR_OPTIONAL_UPDATE = 1
private const val MAX_PRIORITY_FOR_OPTIONAL_UPDATE = 4

private val RANGE_PRIORITY_FOR_OPTIONAL_UPDATE =
    MIN_PRIORITY_FOR_OPTIONAL_UPDATE..MAX_PRIORITY_FOR_OPTIONAL_UPDATE
private const val MIN_PRIORITY_FOR_MANDATORY_UPDATE = 5
