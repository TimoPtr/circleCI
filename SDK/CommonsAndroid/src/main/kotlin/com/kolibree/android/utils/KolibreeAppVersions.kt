/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import timber.log.Timber

@Keep
data class KolibreeAppVersions @VisibleForTesting constructor(val appVersion: String, val buildVersion: String) {
    constructor(context: Context) : this(getAppVersion(context), getBuildVersion(context))
}

private fun getAppVersion(context: Context): String {
    return try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val version = info.versionName

        if (version.contains("-")) {
            version.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            version
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e(e, "Impossible to get app version")
        ""
    }
}

private fun getBuildVersion(context: Context): String {
    return try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "" + info.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e(e, "Impossible to get build version")
        ""
    }
}
