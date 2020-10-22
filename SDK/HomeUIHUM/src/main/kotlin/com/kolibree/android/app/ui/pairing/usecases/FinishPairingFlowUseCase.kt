/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.ui.pairing.PairingSharedViewModel
import io.reactivex.Completable
import javax.inject.Inject

internal class FinishPairingFlowUseCase @Inject constructor(
    private val sharedFacade: PairingSharedViewModel,
    private val confirmConnectionUseCase: ConfirmConnectionUseCase
) {
    fun finish(failOnMissingConnection: Boolean): Completable {
        return confirmConnectionUseCase.confirm(failOnMissingConnection = failOnMissingConnection)
            .andThen(Completable.fromAction { sharedFacade.onPairingFlowSuccess() })
    }
}
