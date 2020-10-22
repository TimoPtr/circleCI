/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.ConfirmConnectionUseCase
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Associates the confirmed connection to the active profile depending on the PairingFlowHost
 */
internal class BrushFoundConfirmConnectionUseCase @Inject constructor(
    private val pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val confirmConnectionUseCase: ConfirmConnectionUseCase
) {
    fun maybeConfirmConnection(): Completable {
        return turnOffVibration()
            .andThen(confirmConnection())
    }

    private fun turnOffVibration(): Completable {
        return Completable.defer {
            pairingFlowSharedFacade.blinkingConnection()?.vibrator()?.off()
                ?: Completable.complete()
        }
    }

    private fun confirmConnection(): Completable {
        return Completable.defer {
            if (pairingFlowSharedFacade.isOnboardingFlow()) {
                Completable.complete()
            } else {
                confirmConnectionUseCase.confirm(failOnMissingConnection = true)
            }
        }
    }
}
