/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.scopes.ActivityScope
import io.reactivex.Observable
import javax.inject.Inject

@VisibleForApp
interface SmileCounterChangedUseCase {
    fun onSmileCounterChanged(smilesCounterState: SmilesCounterState)

    val counterStateObservable: Observable<SmilesCounterState>
}

/**
 * The purpose of this use case is to expose the state change of [SmilesCounterState].
 * On the contrary to its sibling [SmilesCounterStateProvider] which is highly coupled to
 * its Fragment, this class can be used across the Activity to let the components knows
 * when the counter animation has changed.
 */
@ActivityScope
internal class SmileCounterChangedUseCaseImpl @Inject constructor() : SmileCounterChangedUseCase {

    private val counterStateRelay = PublishRelay.create<SmilesCounterState>()

    override val counterStateObservable: Observable<SmilesCounterState>
        get() = counterStateRelay.hide()

    override fun onSmileCounterChanged(smilesCounterState: SmilesCounterState) {
        counterStateRelay.accept(smilesCounterState)
    }
}
