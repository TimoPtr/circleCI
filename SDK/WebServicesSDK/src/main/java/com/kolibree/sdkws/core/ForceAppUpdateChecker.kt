package com.kolibree.sdkws.core

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.network.api.ApiErrorCode.INVALID_APP_VERSION
import com.kolibree.sdkws.networking.Response
import io.reactivex.Observable
import javax.inject.Inject

interface ForceAppUpdateChecker {
    fun isAppUpdateNeeded(): Observable<Boolean>
}

internal interface InternalForceAppUpdater : ForceAppUpdateChecker {
    fun maybeNotifyForcedAppUpdate(response: Response)
}

internal class ForceAppUpdateCheckerImpl @Inject constructor() : InternalForceAppUpdater {
    private val updateAppRelay = BehaviorRelay.create<Boolean>()

    override fun isAppUpdateNeeded(): Observable<Boolean> = updateAppRelay.hide()

    override fun maybeNotifyForcedAppUpdate(response: Response) {
        response.error?.let {
            updateAppRelay.accept(
                it.internalErrorCode == INVALID_APP_VERSION
            )
        }
    }
}
