/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.annotation.Keep
import java.util.Locale

/**
 * Executes [block] with the device's Locale set to [newLocale]. Finally, it resets Locale to the
 * default value
 */
@Keep
fun Context.forceLocale(newLocale: Locale, block: () -> Unit) {
    val defaultLocale = Locale.getDefault()

    try {
        forceLocale(newLocale)

        block.invoke()
    } finally {
        forceLocale(defaultLocale)
    }
}

private fun Context.forceLocale(newLocale: Locale) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        updateResourcesLocale(this, newLocale)
    }

    updateResourcesLocaleLegacy(this, newLocale)

    Locale.setDefault(newLocale)
}

@TargetApi(Build.VERSION_CODES.N)
private fun updateResourcesLocale(
    context: Context,
    locale: Locale
) {
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
}

@Suppress("DEPRECATION")
private fun updateResourcesLocaleLegacy(context: Context, locale: Locale) {
    val resources: Resources = context.resources
    val configuration: Configuration = resources.configuration
    configuration.locale = locale
    resources.updateConfiguration(configuration, resources.displayMetrics)
}
