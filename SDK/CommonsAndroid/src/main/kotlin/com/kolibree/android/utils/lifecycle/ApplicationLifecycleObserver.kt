/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils.lifecycle

import androidx.lifecycle.ProcessLifecycleOwner
import com.kolibree.android.annotation.VisibleForApp

/**
 * ApplicationLifecycleObserver will be notified about
 * Application lifecycle changes.
 *
 * Because [ApplicationLifecyclePublisherImpl] is based on
 * [ProcessLifecycleOwner] onApplicationDestroy is not supported.
 *
 * All methods are invoked on main thread therefore,
 * they should not perform blocking operations.
 * If you want to do something heavy, please delegate it
 * to some background thread.
 *
 * @see ApplicationLifecyclePublisher
 */
@VisibleForApp
interface ApplicationLifecycleObserver {
    fun onApplicationCreated() = Unit
    fun onApplicationStarted() = Unit
    fun onApplicationResumed() = Unit
    fun onApplicationPaused() = Unit
    fun onApplicationStopped() = Unit
}
