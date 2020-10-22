/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.rewards.SmilesUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

internal interface SmilesCounterStateProvider {
    val smilesStateObservable: Observable<SmilesCounterState>
}

internal class SmilesCounterStateProviderImpl @Inject constructor(
    smilesCounterVisibilityUseCase: SmilesCounterVisibilityUseCase,
    userExpectsSmilesUseCase: UserExpectsSmilesUseCase,
    smilesUseCase: SmilesUseCase,
    smilesCounterStateMerger: SmilesCounterStateMerger,
    networkChecker: NetworkChecker,
    smileCounterChangedUseCase: SmileCounterChangedUseCase,
    @SingleThreadScheduler debounceScheduler: Scheduler
) : SmilesCounterStateProvider {

    /**
     * Observable that emits updates of Smiles points for all profiles
     *
     * Since there can be a rapid sequence of Smiles awarded, it emits the first value as soon as it
     * is available, and debounces the emission by 500ms windows to avoid too frequent updates
     */
    private val smilesObservableOnceAndStreamDebounced: Observable<Int> =
        Observable.concat(
            smilesUseCase.smilesAmountStream().toObservable().take(1),
            smilesUseCase.smilesAmountStream().toObservable().skip(1)
                .debounce(
                    DEBOUNCE_SMILES_RESULTS.toMillis(),
                    TimeUnit.MILLISECONDS,
                    debounceScheduler
                )
        )

    /**
     * This Observable merges state from multiple sources to compute the [SmilesCounterState]
     *
     * It does not emit duplicate consecutive values.
     *
     * If there's an error, the observable emits [SmilesCounterState.Error]. The stream will proceed
     * with next state as soon as it can calculate it.
     *
     * @return [Observable]<[SmilesCounterState]>
     */
    override val smilesStateObservable: Observable<SmilesCounterState> = Observable.combineLatest(
        smilesCounterVisibilityUseCase.onceAndStream.distinctUntilChanged(),
        userExpectsSmilesUseCase.onceAndStream.distinctUntilChanged(),
        smilesObservableOnceAndStreamDebounced.distinctUntilChanged(),
        networkChecker.connectivityStateObservable().distinctUntilChanged(),
        smilesCounterStateMerger
    )
        .onErrorReturn {
            Timber.e(it)

            Error
        }
        .distinctUntilChanged()
        .doOnNext(smileCounterChangedUseCase::onSmileCounterChanged)
}

private val DEBOUNCE_SMILES_RESULTS = Duration.of(500, ChronoUnit.MILLIS)
