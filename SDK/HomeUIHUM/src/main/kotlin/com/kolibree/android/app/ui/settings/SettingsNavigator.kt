/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.amazondash.ui.connect.startAmazonDashConnectActivity
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.settings.about.startAboutIntent
import com.kolibree.android.app.ui.settings.help.startHelpActivity
import com.kolibree.android.app.ui.settings.notifications.startNotificationsActivity
import com.kolibree.android.app.ui.settings.secret.SecretSettingsFactory
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.brushingquiz.presentation.createBrushingProgramIntent
import com.kolibree.android.guidedbrushing.settings.startGuidedBrushingSettingsIntent
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject

internal class SettingsNavigator(
    private val secretSettingsFactory: SecretSettingsFactory
) : BaseNavigator<SettingsActivity>() {

    fun closeScreen() = withOwner {
        finish()
    }

    fun showAboutScreen() = withOwner {
        startAboutIntent(this)
    }

    fun showTermsAndConditions() = withOwner {
        showInBrowser(R.string.terms_url)
    }

    fun showPrivacyPolicy() = withOwner {
        showInBrowser(R.string.privacy_url)
    }

    fun showOnboardingScreen() = withOwner {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun showSecretSettings() = withOwner {
        val secretSettings = secretSettingsFactory.secretSettingsIntent(this)
        startActivity(secretSettings)
    }

    fun showBrushingProgram() = withOwner {
        startActivity(createBrushingProgramIntent(this))
    }

    fun showNotificationsScreen() = withOwner {
        startNotificationsActivity(this)
    }

    fun showGuidedBrushingSettings() = withOwner {
        startGuidedBrushingSettingsIntent(this)
    }

    fun showHelpScreen() = withOwner {
        startHelpActivity(this)
    }

    fun showAmazonDashConnect() = withOwner {
        startAmazonDashConnectActivity(this)
    }

    fun rateOurApp() = withOwner {
        showInBrowser(getString(R.string.playstore_url, packageName))
        /*
        Google play library to show Rating Dialog is not reliable.

        See comments in https://kolibree.atlassian.net/browse/KLTB002-12090

        I'm keeping the code because it'll be reused in a future task

        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { response ->
            if (response.isSuccessful) {
                val reviewInfo = response.result
                manager.launchReviewFlow(this, reviewInfo)
            } else {
                showError(R.string.something_went_wrong)
            }
        }*/
    }

    private fun showError(@StringRes messageResId: Int) = withOwner {
        val rootView: View = this.window.decorView.findViewById(android.R.id.content)

        snackbar(rootView) {
            message(messageResId)
            duration(Snackbar.LENGTH_LONG)
        }.show()
    }

    class Factory @Inject constructor(
        private val secretSettingsFactory: SecretSettingsFactory
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsNavigator(secretSettingsFactory) as T
    }
}
