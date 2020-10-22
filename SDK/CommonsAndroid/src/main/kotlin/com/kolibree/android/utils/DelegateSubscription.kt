/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import androidx.annotation.Keep
import com.kolibree.android.extensions.plusAssign
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * This interface help dealing with the subscribe and forget pattern
 */
@Keep
interface DelegateSubscription {
    val disposables: CompositeDisposable

    fun delegateSubscribe(source: Completable, error: (Throwable) -> Unit = Timber::e) {
        disposables += source.subscribe({}, error)
    }

    fun delegateSubscribe(source: Observable<*>, error: (Throwable) -> Unit = Timber::e) {
        disposables += source.subscribe({}, error)
    }

    fun delegateSubscribe(source: Single<*>, error: (Throwable) -> Unit = Timber::e) {
        disposables += source.subscribe({}, error)
    }
}
