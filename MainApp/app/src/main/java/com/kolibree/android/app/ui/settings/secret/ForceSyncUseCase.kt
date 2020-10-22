/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronization.SynchronizationState
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.Instant

internal class ForceSyncUseCase @Inject constructor(
    private val synchronizationStateUseCase: SynchronizationStateUseCase,
    private val connector: IKolibreeConnector
) {
    fun force(): Single<SynchronizationState> {
        return startSyncOnce()
            .reportSuccessOrFailureSync()
    }

    private fun startSyncOnce(): Single<Instant> {
        return Single.defer {
            val preSyncInstant = TrustedClock.getNowInstant()

            connector.syncAndNotify()
                .map { preSyncInstant }
        }
    }

    private fun Single<Instant>.reportSuccessOrFailureSync(): Single<SynchronizationState> =
        flatMap { preSyncInstant ->
            synchronizationStateUseCase.onceAndStream
                .filter { syncState ->
                    when (syncState) {
                        is SynchronizationState.Success, is SynchronizationState.Failure -> syncState.timestamp.isAfter(
                            preSyncInstant
                        )
                        else -> false
                    }
                }
                .take(1)
                .singleOrError()
        }
}
