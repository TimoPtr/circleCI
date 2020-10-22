/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeUseCase
import com.kolibree.android.sdk.connection.isActive
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

internal class QuizConfirmationUseCase @Inject constructor(
    private val selectedBrushingMode: BrushingMode,
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingProgramToothbrushesUseCase: BrushingProgramToothbrushesUseCase,
    private val confirmUserModeUseCase: ConfirmBrushingModeUseCase,
    @SingleThreadScheduler private val timeScheduler: Scheduler
) {

    @VisibleForTesting
    val oldBrushingMode = AtomicReference<BrushingMode>(null)

    /**
     * Allows the user to try out the selected [BrushingMode]
     *
     * @return Completable that will emit [NoToothbrushWithBrushingProgramException] if there's
     * no paired toothbrush to set the [BrushingMode].
     * The completable will be subscribed on Schedulers.io by default
     */
    inline fun tryOutBrushingModeCompletable(
        crossinline doOnSubscribeBlock: () -> Unit,
        crossinline doFinallyBlock: () -> Unit
    ): Completable {
        return currentProfileProvider
            .currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { doOnSubscribeBlock.invoke() }
            .flatMap { brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(it.id) }
            .map { it.filter { connection -> connection.isActive() } }
            .flatMapCompletable {
                if (it.isEmpty()) Completable.error(NoToothbrushWithBrushingProgramException)
                else tryOutBrushingModeCompletable(it.first())
            }
            .doFinally { doFinallyBlock.invoke() }
            .timeout(TRY_BRUSHING_MODE_TIMEOUT_SEC, TimeUnit.SECONDS, timeScheduler)
    }

    /**
     * Confirms the selected [BrushingMode]
     *
     * If there's supported brushing program toothbrush paired and active, it'll set the BrushingMode.
     *
     * @return a Completable that will complete after storing the [BrushingMode] for the active
     * profile.
     * The completable will be subscribed on Schedulers.io by default
     */
    inline fun confirmBrushingModeCompletable(
        crossinline doOnSubscribeBlock: () -> Unit,
        crossinline doFinallyBlock: () -> Unit
    ): Completable {
        return currentProfileProvider
            .currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { doOnSubscribeBlock.invoke() }
            .flatMapCompletable { profile ->
                confirmUserModeUseCase.confirmBrushingModeCompletable(
                    profile.id,
                    selectedBrushingMode
                )
            }
            .doFinally { doFinallyBlock.invoke() }
    }

    /**
     * Reverts the previously selected [BrushingMode]
     *
     * @return a Completable that will complete after reverting the [BrushingMode] for the active
     * profile.
     * The completable will be subscribed on Schedulers.io by default
     */
    inline fun maybeRevertBrushingModeCompletable(
        crossinline doOnSubscribeBlock: () -> Unit,
        crossinline doFinallyBlock: () -> Unit
    ): Completable =
        oldBrushingMode.get()
            ?.let {
                currentProfileProvider
                    .currentProfileSingle()
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { doOnSubscribeBlock.invoke() }
                    .flatMapCompletable { profile ->
                        confirmUserModeUseCase.confirmBrushingModeCompletable(profile.id, it)
                    }
                    .doFinally { doFinallyBlock.invoke() }
            }
            ?: Completable.complete()

    @VisibleForTesting
    fun tryOutBrushingModeCompletable(connection: KLTBConnection): Completable {
        val brushingModeManager = connection.brushingMode()

        return brushingModeManager.getCurrent()
            .flatMapCompletable { oldMode ->
                oldBrushingMode.compareAndSet(null, oldMode)
                brushingModeManager.set(selectedBrushingMode)
                    .concatWith(connection.vibrator().on())
            }
    }
}

internal object NoToothbrushWithBrushingProgramException : Exception()

/**
 * Bluetooth commands timeout after 5 seconds, after which they will throw an exception
 *
 * Let's set 6 seconds here in case things go bad
 */
private const val TRY_BRUSHING_MODE_TIMEOUT_SEC = 6L
