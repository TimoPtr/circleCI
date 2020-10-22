/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.lifecycle

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.KolibreeExperimental
import io.reactivex.disposables.Disposable

/**
 * Provides a convenient way to associate RX [Disposable]s with particular lifecycle state.
 * Each method associates disposable with dedicated composite, which is then disposed
 * when [Lifecycle] reaches state associated with it.
 *
 * @see Lifecycle
 * @see DisposableScope
 */
@Keep
@KolibreeExperimental
interface LifecycleDisposableScope {

    /**
     * Makes sure that disposable returned by the parameter will be disposed
     * in [onPause] lifecycle method.
     *
     * @param block reactive block that returns [Disposable]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun disposeOnPause(block: () -> Disposable?)

    /**
     * Makes sure that disposable returned by the parameter will be disposed
     * in [onStop] lifecycle method.
     *
     * @param block reactive block that returns [Disposable]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun disposeOnStop(block: () -> Disposable?)

    /**
     * Makes sure that disposable returned by the parameter will be disposed
     * in [onDestroy] lifecycle method.
     *
     * @param block reactive block that returns [Disposable]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun disposeOnDestroy(block: () -> Disposable?)
}
