/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.extention

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.failearly.FailEarly

@Dimension
@Keep
fun Context.dimenFloat(@DimenRes dimenRes: Int): Float = resources.getDimension(dimenRes)

@Dimension
@Keep
fun Context.dimenInt(@DimenRes dimenRes: Int): Int = dimenFloat(dimenRes).toInt()

/*
 * Be careful while using this method, if the url can open an app,
 * you should put the app package into <query> in the manifest otherwise
 * nothing will happen and it will crash on debug build due to a FailEarly and
 * won't do anything in release.
 */
@Keep
fun Context.showInBrowser(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (canHandleIntent(browserIntent)) {
        startActivity(browserIntent)
    } else {
        FailEarly.fail("Intent not supported! $browserIntent")
    }
}

@Keep
fun Context.showInBrowser(@StringRes urlRes: Int) {
    showInBrowser(getString(urlRes))
}

@Keep
@SuppressLint("QueryPermissionsNeeded")
fun Context.canHandleIntent(intent: Intent): Boolean {
    return intent.resolveActivity(packageManager) != null
}

@Keep
@Suppress("LongMethod")
fun Context.openAppNotificationSettings() {
    val notificationsSettingsIntent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            else -> {
                action = APP_NOTIFICATION_SETTINGS_ACTION
                putExtra(APP_PACKAGE, packageName)
                putExtra(APP_UID, applicationInfo.uid)
            }
        }
    }
    if (canHandleIntent(notificationsSettingsIntent)) {
        startActivity(notificationsSettingsIntent)
    } else {
        FailEarly.fail("Intent not supported! $notificationsSettingsIntent")
    }
}

private const val APP_NOTIFICATION_SETTINGS_ACTION = "android.settings.APP_NOTIFICATION_SETTINGS"
private const val APP_PACKAGE = "app_package"
private const val APP_UID = "app_uid"
