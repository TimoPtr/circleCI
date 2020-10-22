/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.settings.about.social.FacebookIntentProvider
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject
import timber.log.Timber

internal class AboutNavigator : BaseNavigator<AboutActivity>() {

    fun closeScreen() = withOwner { finish() }

    fun showFacebookPage() = withOwner {
        try {
            startActivity(FacebookIntentProvider.intent(this))
        } catch (ex: ActivityNotFoundException) {
            Timber.e(ex)
            showInBrowser(getString(R.string.facebook_social_link))
        }
    }

    fun showTwitterPage() = withOwner {
        showInBrowser(getString(R.string.twitter_social_link))
    }

    fun showInstagramPage() = withOwner {
        showInBrowser(getString(R.string.instagram_social_link))
    }

    fun showColgateWebsite() = withOwner {
        showInBrowser(getString(R.string.website_link))
    }

    fun showLicensesPage() = withOwner {
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AboutNavigator() as T
    }
}
