/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.persistence

import com.kolibree.android.annotation.VisibleForApp

/**
 * Manages flags that will be reset on each session start
 */
@VisibleForApp
interface SessionFlags {
    fun setSessionFlag(key: String, value: Boolean)

    /**
     * Returns the previously set session value for [key], or null if no value was set
     */
    fun readSessionFlag(key: String): Boolean?

    @VisibleForApp
    companion object {
        const val SHOULD_NOTIFY_LOCATION_NEEDED = "should_notify_location_needed"
        const val SHOULD_NOTIFY_BLUETOOTH_NEEDED = "should_notify_bluetooth_needed"
    }
}
