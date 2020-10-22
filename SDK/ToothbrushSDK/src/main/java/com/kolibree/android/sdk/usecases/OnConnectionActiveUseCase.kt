/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.usecases

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * UseCase for operations that should happen after a connection is active
 */
internal class OnConnectionActiveUseCase @Inject constructor(
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase,
    private val persistedToothbrushRefreshUseCase: PersistedToothbrushRefreshUseCase
) {
    fun apply(connection: KLTBConnection): Completable {
        return Completable.mergeDelayError(
            listOf(
                synchronizeBrushingMode(connection),
                updatePersistedVersions(connection)
            )
        )
    }

    private fun synchronizeBrushingMode(connection: KLTBConnection): Completable =
        synchronizeBrushingModeUseCase.synchronizeBrushingMode(connection)
            .subscribeOn(Schedulers.io())

    private fun updatePersistedVersions(connection: KLTBConnection): Completable =
        persistedToothbrushRefreshUseCase.maybeUpdateVersions(connection)
}
