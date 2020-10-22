/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.ui.settings.secret.persistence

import android.content.Context
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.persistence.SessionFlags.Companion.SHOULD_NOTIFY_BLUETOOTH_NEEDED
import com.kolibree.android.persistence.SessionFlags.Companion.SHOULD_NOTIFY_LOCATION_NEEDED
import javax.inject.Inject

/**
 * Manages flags that will be reset on each session start in our application
 *
 * For now, we consider that a session starts when WelcomeActivity is launched for the first time
 */
class AppSessionFlags @Inject constructor(context: Context) : BasePreferencesImpl(context),
    SessionFlags {

    private companion object {
        const val DEFAULT_SHOULD_REQUEST_ENABLE_LOCATION = true
        const val SHOULD_REQUEST_ENABLE_LOCATION_KEY = "should_request_enable_location"
    }

    fun onSessionStart() {
        setShouldRequestEnableLocation(DEFAULT_SHOULD_REQUEST_ENABLE_LOCATION)

        setSessionFlag(SHOULD_NOTIFY_BLUETOOTH_NEEDED, true)
        setSessionFlag(SHOULD_NOTIFY_LOCATION_NEEDED, true)
    }

    fun setShouldRequestEnableLocation(shouldRequest: Boolean) =
        setSessionFlag(SHOULD_REQUEST_ENABLE_LOCATION_KEY, shouldRequest)

    fun shouldRequestEnableLocation() =
        internalReadSessionFlag(
            SHOULD_REQUEST_ENABLE_LOCATION_KEY,
            DEFAULT_SHOULD_REQUEST_ENABLE_LOCATION
        )

    override fun setSessionFlag(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun readSessionFlag(key: String): Boolean? {
        return if (prefs.contains(key)) {
            internalReadSessionFlag(key, false)
        } else {
            null
        }
    }

    private fun internalReadSessionFlag(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
}
