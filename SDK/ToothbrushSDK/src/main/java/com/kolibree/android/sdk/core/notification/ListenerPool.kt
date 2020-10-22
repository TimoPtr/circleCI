/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.notification

import androidx.annotation.Keep

@Keep
interface ListenerPool<LT : Any> {
    fun add(listener: LT): Int
    fun remove(listener: LT): Int
    fun notifyListeners(listenerNotifier: ListenerNotifier<LT>)
    fun size(): Int
}
