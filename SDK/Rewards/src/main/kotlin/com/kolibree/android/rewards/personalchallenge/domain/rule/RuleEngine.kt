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
import io.reactivex.Observable

/**
 * An abstract interface for an asynchronous rule engine - a black-box system that generates
 * output when new input is provided & internal set of rules/conditions is met.
 *
 * Important thing: not every input generates the output.
 *
 * Can be implemented as:
 * - on-the-fly calculation
 * - decision matrix
 * - decision tree
 *
 * @param I - a type of input that engine accepts
 * @param O - a type of output that engine produces
 */
@Keep
interface RuleEngine<I, O> {

    fun getOutputStream(): Observable<O>

    fun updateInput(input: I)
}
