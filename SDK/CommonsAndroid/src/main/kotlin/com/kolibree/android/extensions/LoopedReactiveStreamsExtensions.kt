/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import androidx.annotation.Keep
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

/**
 * Converts a collection of elements to looped [Observable].
 *
 * @param period the period size in time units (see below)
 * @param unit time units to use for the interval size
 * @param scheduler the Scheduler to use for scheduling the items
 * @return an Observable that emits an item from the collection every [period][unit] (for ex. 1s)
 * starting from the first one and then loops around the collection until it gets terminated.
 */
@Keep
fun <T> List<T>.toLoopedObservable(
    period: Long,
    unit: TimeUnit,
    scheduler: Scheduler
): Observable<T> = Observable.interval(period, unit, scheduler).map { this[it.toInt() % size] }

/**
 * Converts a collection of elements to looped [Flowable].
 *
 * @param period the period size in time units (see below)
 * @param unit time units to use for the interval size
 * @param scheduler the Scheduler to use for scheduling the items
 * @return a [Flowable] that emits an item from the collection every [period][unit] (for ex. 1s)
 * starting from the first one and then loops around the collection until it gets terminated.
 */
@Keep
fun <T> List<T>.toLoopedFlowable(
    period: Long,
    unit: TimeUnit,
    scheduler: Scheduler
): Flowable<T> = Flowable.interval(period, unit, scheduler).map { this[it.toInt() % size] }
