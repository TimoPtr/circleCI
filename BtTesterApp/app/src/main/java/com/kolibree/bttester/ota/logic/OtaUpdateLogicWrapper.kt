/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.logic

import android.content.res.Resources
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.ui.ota.OtaUpdateViewModel
import com.kolibree.android.app.ui.ota.OtaUpdateViewState
import com.kolibree.android.app.ui.ota.OtaUpdater
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

internal class OtaUpdateLogicWrapper @Inject constructor(
    private val resources: Resources,
    private val lifecycle: Lifecycle,
    private val otaChecker: OtaChecker,
    private val featureToggleSet: FeatureToggleSet,
    private val serviceProvider: ServiceProvider,
    private val checkOtaUpdatePrerequisitesUseCase: CheckOtaUpdatePrerequisitesUseCase
) {

    lateinit var otaUpdateViewModel: OtaUpdateViewModel

    internal fun performOtaObservable(
        macAddress: String,
        toothBrushModel: ToothbrushModel
    ): Observable<OtaUpdateViewState> {
        return checkActiveConnectionCompletable(macAddress, toothBrushModel)
            .andThen(waitForOtaForConnectionObservable(macAddress))
            .flatMapObservable { performOta(macAddress, toothBrushModel) }
            .doOnError(Timber::e)
    }

    internal fun cleanup() {
        if (::otaUpdateViewModel.isInitialized) {
            otaUpdateViewModel.onUserConfirmedExit()
            lifecycle.removeObserver(otaUpdateViewModel)
        }
    }

    private fun checkActiveConnectionCompletable(macAddress: String, toothBrushModel: ToothbrushModel): Completable {
        return serviceProvider.connectOnce()
            .flatMapCompletable { service ->
                Completable.fromAction {
                    service.createAndEstablishConnection(macAddress, toothBrushModel, "")
                }
            }
    }

    private fun waitForOtaForConnectionObservable(macAddress: String): Single<OtaForConnection> {
        return Single.defer {
            otaChecker.otaForConnectionsOnce()
                .filter { otaConnection ->
                    otaConnection.connection.toothbrush().mac == macAddress
                }
                .firstOrError()
        }
    }

    private fun performOta(macAddress: String, toothBrushModel: ToothbrushModel): Observable<OtaUpdateViewState>? {
        val alwaysOfferOtaUpdateFeatureToggle =
            featureToggleSet.toggleForFeature(AlwaysOfferOtaUpdateFeature)
        alwaysOfferOtaUpdateFeatureToggle.value = true
        otaUpdateViewModel = OtaUpdateViewModel(
            resources,
            serviceProvider,
            macAddress,
            toothBrushModel,
            false,
            checkOtaUpdatePrerequisitesUseCase,
            OtaUpdater()
        )
        lifecycle.addObserver(otaUpdateViewModel)
        return otaUpdateViewModel.viewStateObservable()
            .doOnSubscribe { otaUpdateViewModel.onUserClickedActionButton() }
    }
}
