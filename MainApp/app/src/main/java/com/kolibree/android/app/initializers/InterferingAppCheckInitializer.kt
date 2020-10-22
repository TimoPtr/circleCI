/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import com.kolibree.BuildConfig
import com.kolibree.android.app.initializers.base.AppInitializer
import javax.inject.Inject
import timber.log.Timber

/**
 * Goal of this initializer is to check if any of
 * the installed apps can interfere with our app.
 *
 * It checks all installed packages and if any [canInterfereWith] our [Application]
 * it will log it and show toast if [BuildConfig.DEBUG].
 */
internal class InterferingAppCheckInitializer @Inject constructor() : AppInitializer {

    /*
    Package names need to be added to the manifest in order to work properly on Android 11+
     */
    @SuppressLint("QueryPermissionsNeeded")
    override fun initialize(application: Application) {
        application.packageManager
            .getInstalledApplications(0)
            .map { appInfo -> appInfo.packageName }
            .filter { appPackage -> application.canInterfereWith(appPackage) }
            .takeIf { appPackages -> appPackages.isNotEmpty() }
            ?.let { application.logWarning(it) }
    }

    private fun Application.canInterfereWith(appPackage: String): Boolean {
        return packageName != appPackage &&
            appPackage.contains(Regex("com.colgate|com.kolibree")) &&
            !appPackage.contains("test")
    }

    private fun Application.logWarning(packagesThatCanInterfere: List<String>) {
        val message = "Detected apps that may interfere: ${packagesThatCanInterfere.joinToString()}"
        Timber.w(message)

        if (!BuildConfig.RELEASE) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
