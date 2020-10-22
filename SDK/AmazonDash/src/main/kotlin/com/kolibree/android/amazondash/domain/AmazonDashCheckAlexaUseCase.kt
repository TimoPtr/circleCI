/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

internal interface AmazonDashCheckAlexaUseCase {

    /**
     * Checks if Alexa app is installed and if it supports app links
     */
    fun isAlexaAppAvailable(): Single<Boolean>
}

/**
 * Based on:
 * https://developer.amazon.com/en-US/docs/alexa/account-linking/app-to-app-account-linking-starting-from-your-app.html
 */
internal class AmazonDashCheckAlexaUseCaseImpl constructor(
    private val context: Context,
    private val buildSdkInt: Int
) : AmazonDashCheckAlexaUseCase {

    @Inject
    constructor(context: Context) : this(context, Build.VERSION.SDK_INT)

    @SuppressWarnings("SwallowedException")
    override fun isAlexaAppAvailable(): Single<Boolean> {
        return Single.fromCallable {
            try {
                val packageInfo = context.packageManager.getPackageInfo(ALEXA_PACKAGE_NAME, 0)

                @SuppressLint("NewApi")
                if (packageInfo != null && buildSdkInt >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode > REQUIRED_MINIMUM_VERSION_CODE
                } else {
                    packageInfo != null
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // The Alexa App is not installed
                false
            }
        }.doOnSuccess { available -> Timber.d("Alexa available: $available") }
    }
}

internal const val ALEXA_PACKAGE_NAME = "com.amazon.dee.app"
internal const val REQUIRED_MINIMUM_VERSION_CODE = 866607211L
