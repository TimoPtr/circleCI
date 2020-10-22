/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.widget.OnShadeViewExpandedListener
import com.kolibree.android.app.ui.widget.ShadeView
import io.reactivex.Observable
import javax.inject.Inject
import timber.log.Timber

@FragmentScope
internal class SmilesCounterVisibilityUseCase @VisibleForTesting constructor(
    private val handler: Handler,
    lifecycle: Lifecycle
) : OnShadeViewExpandedListener,
    DefaultLifecycleObserver {

    @Inject
    constructor(lifecycle: Lifecycle) : this(
        lifecycle = lifecycle,
        handler = Handler(Looper.getMainLooper())
    )

    private val isVisibleRelay = BehaviorRelay.createDefault(false)

    private val emitIsVisibleRunnable = Runnable {
        isVisibleRelay.accept(isVisible())
    }

    init {
        lifecycle.addObserver(this)
    }

    private var isForeground: Boolean = false

    private var isViewExpanded: Boolean = false

    /**
     * Observable that will emit true when Smiles counter is visible, false when it's not
     *
     * It considers the Smiles Counter to be visible when user is in Home tab and Smiles counter
     * header is fully expanded
     */
    val onceAndStream: Observable<Boolean> = isVisibleRelay.distinctUntilChanged()

    private fun isVisible(): Boolean {
        return isForeground && isViewExpanded
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        isForeground = true

        emitIsVisible()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        isForeground = false

        emitIsVisible()
    }

    fun setView(view: ShadeView?) {
        view?.setOnExpandedListener(this)
            ?: Timber.w("Can't set OnShadeViewExpandedListener, view is null")
    }

    private fun emitIsVisible() {
        handler.post(emitIsVisibleRunnable)
    }

    @MainThread
    override fun onFullyExpanded() {
        isViewExpanded = true

        emitIsVisible()
    }

    @MainThread
    override fun onNotFullyExpanded() {
        isViewExpanded = false

        emitIsVisible()
    }
}
