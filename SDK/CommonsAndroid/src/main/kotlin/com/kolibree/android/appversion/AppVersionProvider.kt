/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.appversion

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.pm.PackageInfoCompat.getLongVersionCode
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

@VisibleForApp
interface AppVersionProvider {

    /**
     * Update the last version code if needed when the application has been updated.
     */
    fun updateLastVersionCode()

    /**
     * Get the previous version code of the App before it has been updated.
     *
     * For example if the user has updated the App from version `9` to `10`,
     * then it returns `9`.
     * If the App has never been updated before, the version code returned is `-1`
     *
     * @return the last version code before update, or `-1` if the app has not been updated before.
     */
    fun getLastVersionCode(): Long

    /**
     * Get the current App version code
     */
    val currentVersionCode: Long
}

internal class AppVersionProviderImpl @Inject constructor(
    context: Context
) : AppVersionProvider, BasePreferencesImpl(context) {

    private val preferences: SharedPreferences = prefs

    override val currentVersionCode: Long by lazy {
        getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
    }

    override fun updateLastVersionCode() {
        val lastVersion = preferences.getLong(KEY_CURRENT_VERSION_CODE, DEFAULT_VERSION_CODE)

        // If the last version is different, then the values are updated
        if (lastVersion != currentVersionCode) {
            preferences.edit {
                this.putLong(KEY_CURRENT_VERSION_CODE, currentVersionCode)
                this.putLong(KEY_LAST_VERSION_CODE, lastVersion)
            }
        }
    }

    override fun getLastVersionCode(): Long {
        return preferences.getLong(KEY_LAST_VERSION_CODE, DEFAULT_VERSION_CODE)
    }

    companion object {
        /** Only used in internal to figure out if the App has been updated */
        const val KEY_CURRENT_VERSION_CODE = "current_version_code"
        const val KEY_LAST_VERSION_CODE = "last_version_code"
        const val DEFAULT_VERSION_CODE = -1L
    }
}
