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
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.KLTBConnection
import dagger.Binds
import dagger.Module
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

/** Use case responsible for synchronizing the local database and the toothbrush's brushing mode */
@Keep
interface SynchronizeBrushingModeUseCase {

    /**
     * Synchronize the local database and the toothbrush's brushing mode
     *
     * Only affects toothbrushes that
     * - support brushing mode. See [ToothbrushModel.supportsVibrationSpeedUpdate()]
     * - are not in bootloader
     *
     * This method has to be called after a successful connection attempt
     *
     * @param connection active [KLTBConnection]
     * @return [Completable]
     */
    fun synchronizeBrushingMode(connection: KLTBConnection): Completable

    /**
     * set default brushing mode to the profile
     */
    fun initBrushingModeForProfile(profileId: Long): Completable

    /**
     * initialize the Toothbrush and Profile with default brushing mode
     */
    fun initBrushingModeForToothbrushAndProfile(connection: KLTBConnection): Completable

    /**
     * sync the local brushing mode to the toothbrush
     */
    fun syncLocalBrushingModeToToothbrush(connection: KLTBConnection): Completable
}

@Keep
@SuppressWarnings("TooManyFunctions")
class SynchronizeBrushingModeUseCaseImpl @Inject constructor(
    private val brushingModeRepository: BrushingModeRepository
) : SynchronizeBrushingModeUseCase {
    override fun synchronizeBrushingMode(connection: KLTBConnection): Completable =
        isCandidateForSync(connection)
            .flatMapCompletable { isCandidateForSync ->
                if (isCandidateForSync) {
                    chooseSynchronizationCompletable(connection)
                } else {
                    Completable.complete()
                }
            }

    @VisibleForTesting
    fun chooseSynchronizationCompletable(connection: KLTBConnection): Completable =
        Single.zip(
            fetchDatabaseProfileBrushingMode(connection),
            fetchToothbrushProfileBrushingMode(connection),
            BiFunction<ProfileBrushingMode, ProfileBrushingMode, Completable> { dbState, tbState ->
                return@BiFunction when {
                    // No local data
                    dbState == ProfileBrushingMode.NULL ->
                        syncFromToothbrushToDatabase(tbState)

                    // Already synchronized
                    dbState.brushingMode == tbState.brushingMode ->
                        Completable.complete()

                    // Local data is newer
                    dbState.dateTime.truncatedTo(ChronoUnit.SECONDS).isAfter(tbState.dateTime) &&
                        dbState.isValid() -> syncFromDatabaseToToothbrush(connection, dbState)

                    // Toothbrush data is newer
                    else -> syncFromToothbrushToDatabase(tbState)
                }
            }
        ).flatMapCompletable { it }

    @VisibleForTesting
    fun syncFromToothbrushToDatabase(tbState: ProfileBrushingMode) =
        Completable.fromAction {
            brushingModeRepository.setForProfile(tbState.profileId, tbState.brushingMode)
        }

    @VisibleForTesting
    fun syncFromDatabaseToToothbrush(connection: KLTBConnection, dbState: ProfileBrushingMode) =
        connection
            .brushingMode()
            .set(dbState.brushingMode)

    @VisibleForTesting
    fun fetchDatabaseProfileBrushingMode(connection: KLTBConnection): Single<ProfileBrushingMode> =
        connection
            .userMode()
            .profileId()
            .map { brushingModeRepository.getForProfile(it) ?: ProfileBrushingMode.NULL }

    @VisibleForTesting
    fun fetchToothbrushProfileBrushingMode(connection: KLTBConnection) =
        Single.zip(
            connection.userMode().profileId(),
            connection.brushingMode().getCurrent(),
            connection.brushingMode().lastUpdateDate(),
            Function3<Long, BrushingMode, OffsetDateTime, ProfileBrushingMode> { profileId, mode, lastSync ->
                ProfileBrushingMode(profileId, mode, lastSync)
            }
        )

    @VisibleForTesting
    fun isCandidateForSync(connection: KLTBConnection): Single<Boolean> =
        if (!connection.toothbrush().isRunningBootloader && connection.brushingMode().isAvailable())
            connection.userMode().isSharedModeEnabled().map { it.not() } // Disabled on shared tbs
        else
            Single.just(false)

    override fun syncLocalBrushingModeToToothbrush(connection: KLTBConnection): Completable =
        isCandidateForSync(connection)
            .flatMapCompletable { isCandidateForSync ->
                if (isCandidateForSync) {
                    fetchDatabaseProfileBrushingMode(connection)
                        .map { syncFromDatabaseToToothbrush(connection, it) }
                        .flatMapCompletable { it }
                } else {
                    Completable.complete()
                }
            }

    override fun initBrushingModeForToothbrushAndProfile(connection: KLTBConnection): Completable =
        setDefaultModeForProfile(connection)
            .andThen(setDefaultModeForToothBrush(connection))

    override fun initBrushingModeForProfile(profileId: Long): Completable =
        Completable.fromAction {
            brushingModeRepository.setForProfile(profileId, BrushingMode.defaultMode())
        }

    @VisibleForTesting
    fun setDefaultModeForToothBrush(connection: KLTBConnection): Completable =
        isCandidateForSync(connection).flatMapCompletable { isCandidateForSync ->
            if (isCandidateForSync) {
                connection.brushingMode().set(BrushingMode.defaultMode())
            } else {
                Completable.complete()
            }
        }

    @VisibleForTesting
    fun setDefaultModeForProfile(connection: KLTBConnection): Completable =
        connection.userMode().profileId()
            .flatMapCompletable {
                initBrushingModeForProfile(it)
            }
}

/** Module to be included when depending on [SynchronizeBrushingModeUseCase] */
@Module(includes = [BrushingProgramModule::class])
@Keep
abstract class SynchronizeBrushingModeUseCaseModule {

    @Binds
    abstract fun bindConfirmBrushingModeUseCase(
        impl: SynchronizeBrushingModeUseCaseImpl
    ): SynchronizeBrushingModeUseCase
}
