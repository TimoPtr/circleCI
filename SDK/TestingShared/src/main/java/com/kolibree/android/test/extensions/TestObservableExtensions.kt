/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.extensions

import androidx.annotation.Keep
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber

@Keep
fun <T> TestObserver<T>.assertLastValue(expectedValue: T): TestObserver<T> {
    return assertValueAt(valueCount() - 1, expectedValue)
}

@Keep
fun <T> TestObserver<T>.assertLastValueWithPredicate(
    valuePredicate: (T) -> Boolean
): TestObserver<T> = assertValueAt(valueCount() - 1, valuePredicate)

@Keep
fun <T> TestSubscriber<T>.assertLastValue(expectedValue: T): TestSubscriber<T> {
    return assertValueAt(valueCount() - 1, expectedValue)
}

@Keep
fun <T> TestSubscriber<T>.assertLastValueWithPredicate(
    valuePredicate: (T) -> Boolean
): TestSubscriber<T> = assertValueAt(valueCount() - 1, valuePredicate)
