/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.insets

import android.view.View
import android.view.WindowInsets
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarly

/**
 * A mediator class for redistribution of window insets. Can be used for ex. to
 * redistribute insets from activity's window to its fragments.
 *
 * Because insets should be applied only once per window, mediator subscribes itself
 * to view as window insets listener and applies insets to it when they're available.
 * If any other receiver asks for insets before that happen, it is added to pending list.
 * Once insets are available, all pending receivers are notified and the value is cached.
 * Querying insets later will cause immediate notification.
 *
 * @param rootView view which will receive windows insets that
 */
@Keep
class WindowInsetsMediator(
    rootView: View
) : WindowInsetsOwner {

    @VisibleForTesting
    lateinit var windowInsets: WindowInsets

    @VisibleForTesting
    internal val pendingList = mutableSetOf<PendingWindowInsetsReceiver>()

    init {
        rootView.setOnApplyWindowInsetsListener { _, insets ->
            FailEarly.failIfNotExecutedOnMainThread()
            synchronized(this@WindowInsetsMediator) {
                windowInsets = insets
                pendingList.forEach {
                    it.onWindowInsetsAvailable(insets)
                }
                pendingList.clear()
            }
            insets
        }
    }

    override fun withWindowInsets(block: (WindowInsets) -> Unit) {
        FailEarly.failIfNotExecutedOnMainThread()
        synchronized(this@WindowInsetsMediator) {
            if (::windowInsets.isInitialized) {
                block(windowInsets)
            } else {
                addToPending(block)
            }
        }
    }

    private fun addToPending(block: (WindowInsets) -> Unit) {
        pendingList.add(object : PendingWindowInsetsReceiver {
            override fun onWindowInsetsAvailable(insets: WindowInsets) {
                block(insets)
            }
        })
    }
}

internal interface PendingWindowInsetsReceiver {

    fun onWindowInsetsAvailable(insets: WindowInsets)
}
