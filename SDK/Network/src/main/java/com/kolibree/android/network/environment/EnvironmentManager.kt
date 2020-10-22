/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.network.environment

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.environment.Environment.Companion.fromUrl
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
interface EnvironmentManager {
    fun setEnvironment(environment: Environment): Boolean
    fun setEndpoint(endpoint: Endpoint): Boolean

    fun environment(): Environment
    fun endpoint(): Endpoint

    fun setCustomEnvironment(customEnvironment: CustomEnvironment): Boolean
    fun endpointUrlAlreadyExists(customEnvironment: CustomEnvironment): Boolean
}

/** Created by miguelaragues on 6/3/18.  */
internal class EnvironmentManagerImpl constructor(
    context: Context,
    private val defaultEnvironment: DefaultEnvironment?,
    private val killAppDelaySeconds: Long,
    private val handler: Handler
) : EnvironmentManager {

    @Inject
    constructor(context: Context, defaultEnvironment: DefaultEnvironment?) : this(
        context,
        defaultEnvironment,
        KILL_APP_DEFAULT_DELAY_SECONDS,
        Handler(Looper.getMainLooper())
    )

    private val context: Context = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(
            SECRET_PROVIDER_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    private val prefsEditor: SharedPreferences.Editor by lazy { prefs.edit() }

    /**
     * Updates the [Environment] the requests go to
     *
     *
     * If we attempt to set the Environment we already use, it returns false.
     *
     *
     * On success, it returns true
     *
     * @return true if Environment changed successfully, false otherwise
     */
    override fun setEnvironment(environment: Environment): Boolean =
        if (endpoint() == environment) {
            false
        } else setEndpoint(environment)

    /**
     * Set a custom endpoint and credentials to use in backend calls.
     *
     * @param customEnvironment
     * @return false if one of the enums in Environment already points to that url. True otherwise
     */
    override fun setCustomEnvironment(customEnvironment: CustomEnvironment): Boolean =
        if (endpointUrlAlreadyExists(customEnvironment)) {
            false
        } else setEndpoint(customEnvironment)

    override fun endpointUrlAlreadyExists(customEnvironment: CustomEnvironment): Boolean =
        fromUrl(customEnvironment.url()) != Environment.CUSTOM

    override fun setEndpoint(endpoint: Endpoint): Boolean {
        prefsEditor
            .putString(ENVIRONMENT_KEY, endpoint.url())
            .putLong(
                SWAP_ENVIRONMENT_TIMESTAMP_KEY,
                System.currentTimeMillis()
            )
            .apply()
        return true
    }

    override fun environment(): Environment = fromUrl(endpoint().url())

    override fun endpoint(): Endpoint {
        return try {
            val defaultEnvironment = defaultEndpoint()
            val storedEnvironment = storedEndpoint()
            if (storedEnvironment != defaultEnvironment) {
                if (appHasBeenUpdatedSinceEnvironmentSwapped()) {
                    showLogoutMessageAndKillApp(defaultEnvironment, storedEnvironment)
                    defaultEnvironment
                } else {
                    storedEnvironment
                }
            } else {
                defaultEnvironment
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            Environment.PRODUCTION
        }
    }

    @VisibleForTesting
    fun showLogoutMessageAndKillApp(
        defaultEndpoint: Endpoint,
        storedEndpoint: Endpoint
    ) {
        showLogoutMessage(defaultEndpoint, storedEndpoint)
        handler.postDelayed(
            { clearUserDataAndKillApp() },
            killAppDelaySeconds
        )
    }

    @VisibleForTesting
    fun showLogoutMessage(
        defaultEndpoint: Endpoint,
        storedEndpoint: Endpoint
    ) {
        /*
    This could be improved by showing a system alert dialog. See https://stackoverflow.com/a/31221646/218473

    We could add that to debug and beta manifest, since we don't use it in release
     */
        Toast.makeText(
            context,
            """
                You changed the Endpoint to $storedEndpoint. After updating the app, we've reset it to $defaultEndpoint

                Please login again
                """.trimIndent(),
            Toast.LENGTH_LONG
        )
            .show()
    }

    @VisibleForTesting
    fun clearUserDataAndKillApp() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
    }

    @VisibleForTesting
    fun appHasBeenUpdatedSinceEnvironmentSwapped(): Boolean {
        val packageManager = context.packageManager
        try {
            val packageInfo =
                packageManager.getPackageInfo(context.packageName, 0)
            return (packageInfo.lastUpdateTime
                > prefs.getLong(
                SWAP_ENVIRONMENT_TIMESTAMP_KEY,
                Long.MIN_VALUE
            ))
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
        return false
    }

    @VisibleForTesting
    @Throws(PackageManager.NameNotFoundException::class)
    fun defaultEndpoint(): Endpoint = when {
        defaultEnvironment != null -> defaultEnvironment.environment
        isStagingBuild() -> Environment.STAGING
        else -> Environment.PRODUCTION
    }

    @VisibleForTesting
    @Throws(PackageManager.NameNotFoundException::class)
    fun storedEndpoint(): Endpoint {
        val storedUrl = prefs.getString(
            ENVIRONMENT_KEY,
            defaultEndpoint().url()
        ) ?: defaultEndpoint().url()
        val environment =
            fromUrl(storedUrl)
        return if (environment === Environment.CUSTOM) CustomEnvironment(storedUrl) else environment
    }

    /*
  Reads Manifest metadata. Quite ugly, this should be provided by Dagger, but it's how it's done for
  now
   */
    @Throws(PackageManager.NameNotFoundException::class)
    @VisibleForTesting
    fun isStagingBuild(): Boolean {
        val pm = context.packageManager
        val ai = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return ai.metaData.getBoolean(METADATA_DEBUG)
    }

    companion object {
        private const val KILL_APP_DEFAULT_DELAY_SECONDS: Long = 5
        private const val ENVIRONMENT_KEY = "endpoint"
        private const val SWAP_ENVIRONMENT_TIMESTAMP_KEY = "swap_environment_timestamp"
        private const val METADATA_DEBUG = "com.kolibree.debug"
    }
}

// don't change 'secret_' prefix. See BaseClearUserContentJobService
internal const val SECRET_PROVIDER_PREFS_NAME = "secret_provider_preferences"
