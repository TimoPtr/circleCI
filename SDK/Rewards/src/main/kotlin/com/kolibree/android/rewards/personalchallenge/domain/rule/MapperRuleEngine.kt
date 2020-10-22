/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.rule

import androidx.annotation.Keep
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import com.kolibree.android.failearly.FailEarly
import io.reactivex.Observable

/**
 * Rule engine that implements [I] -> [O]? mapping.
 */
@Keep
abstract class MapperRuleEngine<I, O> : RuleEngine<I, O> {

    private val outputRelay: Relay<O> = BehaviorRelay.create<O>()

    override fun getOutputStream(): Observable<O> = outputRelay.distinctUntilChanged()

    override fun updateInput(input: I) {
        map(input)?.let {
            outputRelay.accept(it)
        } ?: FailEarly.fail(
            "Output for $input was not returned, please check your implementation."
        )
    }

    protected abstract fun map(input: I): O?
}
