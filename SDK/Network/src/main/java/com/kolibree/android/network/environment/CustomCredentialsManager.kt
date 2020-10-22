/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.network.environment

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.annotation.VisibleForApp
import javax.inject.Inject

@VisibleForApp
class CustomCredentialsManager @Inject constructor(context: Context) {
    private val appContext = context.applicationContext

    // don't change 'secret_' prefix. See BaseClearUserContentJobService
    @VisibleForApp
    companion object {
        private const val CLIENT_ID_KEY = "custom_client_id"
        private const val CLIENT_SECRET_KEY = "custom_client_secret"
    }

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(SECRET_PROVIDER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val prefsEditor: SharedPreferences.Editor by lazy { prefs.edit() }

    fun setCustomCredentials(clientId: String, clientSecret: String) {
        prefsEditor
            .putString(CLIENT_ID_KEY, clientId)
            .putString(CLIENT_SECRET_KEY, clientSecret)
            .apply()
    }

    fun getCustomCredentials(): Credentials = Credentials(getClientId(), getClientSecret())

    private fun getClientId() = prefs.getString(CLIENT_ID_KEY, "") ?: ""
    private fun getClientSecret() = prefs.getString(CLIENT_SECRET_KEY, "") ?: ""
}
