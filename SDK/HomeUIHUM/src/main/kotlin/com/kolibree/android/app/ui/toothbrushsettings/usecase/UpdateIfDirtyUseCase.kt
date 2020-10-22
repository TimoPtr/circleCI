/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.usecase

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Checks if the AccountToothbrush values are different from those in the backend and we need to
 * update
 */
internal class UpdateIfDirtyUseCase @Inject constructor(
    private val toothbrushRepository: ToothbrushRepository,
    private val updateToothbrushUseCase: UpdateToothbrushUseCase
) {
    fun maybeUpdate(connection: KLTBConnection): Completable {
        return toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac)
            .flatMapCompletable { accountToothbrush ->
                if (accountToothbrush.dirty) {
                    updateToothbrushUseCase.updateToothbrush(connection)
                        .ignoreElement()
                        .andThen(toothbrushRepository.cleanDirty(connection.toothbrush().mac))
                } else {
                    Completable.complete()
                }
            }
    }
}
