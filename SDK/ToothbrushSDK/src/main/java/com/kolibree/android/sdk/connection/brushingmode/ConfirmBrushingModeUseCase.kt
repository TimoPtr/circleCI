/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import dagger.Binds
import dagger.Module
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import timber.log.Timber

@Keep
interface ConfirmBrushingModeUseCase {
    fun confirmBrushingModeCompletable(
        profileId: Long,
        selectedBrushingMode: BrushingMode
    ): Completable
}

internal class ConfirmBrushingModeUseCaseImpl @Inject constructor(
    private val brushingModeRepository: BrushingModeRepository,
    private val brushingProgramToothbrushesUseCase: BrushingProgramToothbrushesUseCase
) : ConfirmBrushingModeUseCase {
    /**
     * Stores [selectedBrushingMode] for the profile associated to [profileId] and attempts to set
     * [selectedBrushingMode] to all active [KLTBConnection] associated to the [Profile]
     */
    override fun confirmBrushingModeCompletable(
        profileId: Long,
        selectedBrushingMode: BrushingMode
    ): Completable {
        return Completable.mergeArrayDelayError(
            storeProfileBrushingModeCompletable(profileId, selectedBrushingMode),
            brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(profileId)
                .flatMapCompletable { connections ->
                    setBrushingModeOnActiveConnectionsCompletable(connections, selectedBrushingMode)
                }
                .doOnError(Timber::e)
                .onErrorComplete()
        )
    }

    private fun storeProfileBrushingModeCompletable(
        profileId: Long,
        selectedBrushingMode: BrushingMode
    ): Completable {
        return Completable.fromAction {
            brushingModeRepository.setForProfile(
                profileId,
                selectedBrushingMode
            )
        }
    }

    private fun setBrushingModeOnActiveConnectionsCompletable(
        connections: List<KLTBConnection>,
        selectedBrushingMode: BrushingMode
    ): Completable {
        return Observable.fromIterable(connections.filter { it.isActive() })
            .flatMapCompletable { connection ->
                connection
                    .brushingMode()
                    .set(selectedBrushingMode)
                    .doOnError(Timber::e)
                    .onErrorComplete()
            }
    }
}

@Keep
@Module(includes = [BrushingProgramModule::class])
abstract class ConfirmBrushingModeModule {
    @Binds
    internal abstract fun bindsConfirmBrushingModeUseCase(impl: ConfirmBrushingModeUseCaseImpl):
        ConfirmBrushingModeUseCase
}
