/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import javax.inject.Inject

/**
 * UseCase to get the next navigation action after the user confirmed the toothbrush either in
 * BrushFound or ScanList screen
 */
internal class NextNavigationActionUseCase @Inject constructor(
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    @SupportedToothbrushModels
    private val supportedToothbrushModels: Set<@JvmSuppressWildcards ToothbrushModel>
) : PairingFlowSharedFacade by pairingFlowSharedFacade {
    fun nextNavitationStep(): ConnectionConfirmedNavigationAction {
        return blinkingConnection()?.let { connection ->
            if (isModelMatched(connection)) {
                onValidModelMatched()
            } else {
                MODEL_MISMATCH
            }
        } ?: NO_BLINKING_CONNECTION
    }

    private fun isModelMatched(connection: KLTBConnection): Boolean {
        val model = connection.toothbrush().model
        return supportedToothbrushModels.contains(model)
    }

    private fun onValidModelMatched(): ConnectionConfirmedNavigationAction {
        return if (isOnboardingFlow()) {
            SIGN_UP
        } else {
            FINISH
        }
    }
}

internal enum class ConnectionConfirmedNavigationAction {
    MODEL_MISMATCH,
    SIGN_UP,
    FINISH,
    NO_BLINKING_CONNECTION
}
