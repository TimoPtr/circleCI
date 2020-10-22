/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.test

import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.core.notification.ListenerPool

internal class FakeListenerPool<LT : Any> : ListenerPool<LT> {
    val listeners = mutableSetOf<LT>()

    override fun add(listener: LT): Int {
        listeners.add(listener)

        return listeners.size
    }

    override fun remove(listener: LT): Int {
        listeners.remove(listener)

        return listeners.size
    }

    override fun notifyListeners(listenerNotifier: ListenerNotifier<LT>) {
        listeners.forEach { listenerNotifier.notifyListener(it) }
    }

    override fun size(): Int = listeners.size
}
