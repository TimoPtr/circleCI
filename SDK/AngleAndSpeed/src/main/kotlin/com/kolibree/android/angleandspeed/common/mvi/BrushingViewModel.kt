/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.mvi

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.mvi.BaseGameViewModel
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.kml.MouthZone16
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

@Keep
abstract class BrushingViewModel<GVS : BaseGameViewState>(
    initialViewState: GVS,
    macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    private val angleAndSpeedUseCase: AngleAndSpeedUseCase,
    private val prescribedZones: Array<MouthZone16>,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController
) : BaseGameViewModel<GVS>(
    initialViewState,
    macAddress,
    gameInteractor,
    facade,
    lostConnectionHandler,
    keepScreenOnController
) {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        // TODO add Espresso test for scenario when user goes back from incisors to molars
        // in Test Angles - this case has to trigger reset of the expected zones, so molars
        // will be detected instead of incisors.
        angleAndSpeedUseCase.setPrescribedZones(prescribedZones)

        disposeOnStop {
            angleAndSpeedUseCase.angleAndSpeedFlowable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response -> onNewFeedback(response) }, Timber::e)
        }
    }

    final override fun onKolibreeServiceConnected(service: KolibreeService) {
        // no-op
    }

    final override fun onKolibreeServiceDisconnected() {
        // no-op
    }

    /**
     * Processes next feedback chunk obtained from KML.
     * Should result in update of view state.
     *
     * @param response new feedback item obtained from KML
     */
    @VisibleForTesting(otherwise = PROTECTED)
    abstract fun onNewFeedback(response: AngleAndSpeedFeedback)
}
