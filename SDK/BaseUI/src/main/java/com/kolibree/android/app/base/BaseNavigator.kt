/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly

@VisibleForApp
abstract class BaseNavigator<T : LifecycleOwner> : ViewModel(), DefaultLifecycleObserver {
    private var owner: T? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        synchronized(this) {
            @Suppress("UNCHECKED_CAST")
            this.owner = owner as T
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        synchronized(this) {
            this.owner = null
        }
    }

    @Synchronized
    protected fun withOwner(execute: T.() -> Unit) {
        owner?.let { execute(it) } ?: FailEarly.fail("Expected T, was null")
    }
}
