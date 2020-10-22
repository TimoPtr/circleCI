/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.extensions

import androidx.annotation.Keep
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertTrue

@Keep
fun CompletableSubject.assertHasObserversAndComplete() {
    assertTrue(hasObservers())

    onComplete()
}

@Keep
fun PublishSubject<*>.assertHasObserversAndComplete() {
    assertTrue(hasObservers())

    onComplete()
}

@Keep
fun <T> SingleSubject<T>.assertHasObserversAndComplete(value: T) {
    assertTrue(hasObservers())

    onSuccess(value)
}
