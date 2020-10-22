/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about.social

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.kolibree.android.homeui.hum.R
import timber.log.Timber

internal object FacebookIntentProvider {
    fun intent(context: Context): Intent {
        val facebookIntent = Intent(Intent.ACTION_VIEW)
        val facebookUrl = getFacebookPageURL(context)
        facebookIntent.data = Uri.parse(facebookUrl)
        return facebookIntent
    }

    private fun getFacebookPageURL(context: Context): String {
        val packageManager: PackageManager = context.packageManager
        val facebookUrl = context.getString(R.string.facebook_social_link)
        return try {
            val versionCode = packageManager.getPackageInfo(FACEBOOK_PACKAGE, 0).versionCode
            if (versionCode >= NEW_SCHEMA_VERSION) { // newer versions of fb app
                "$NEW_SCHEMA$facebookUrl"
            } else { // older versions of fb app
                "$OLD_SCHEMA$FACEBOOK_PAGE_ID"
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            Timber.e(ex)
            facebookUrl
        }
    }
}

private const val NEW_SCHEMA_VERSION = 3002850
private const val FACEBOOK_PAGE_ID = "Colgate"
private const val FACEBOOK_PACKAGE = "com.facebook.katana"
private const val NEW_SCHEMA = "fb://facewebmodal/f?href="
private const val OLD_SCHEMA = "fb://page/"
