/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityHumAboutBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class AboutActivity :
    BaseMVIActivity<AboutViewState,
        AboutActions,
        AboutViewModel.Factory,
        AboutViewModel,
        ActivityHumAboutBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<AboutViewModel> = AboutViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_hum_about

    override fun execute(action: AboutActions) {
        when (action) {
            is AboutActions.ShowSecretSettingsEnabled -> showSecretSettingsEnabledSnackbar()
            is AboutActions.AccountIdCopied -> showAccountIdCopied()
        }
    }

    private fun showSecretSettingsEnabledSnackbar() {
        snackbar(binding.aboutRootLayout) {
            duration(Snackbar.LENGTH_LONG)
            icon(R.drawable.ic_icon_settings_24)
            message("Secret settings enabled!")
            action(R.string.settings_item_about)
        }.show()
    }

    private fun showAccountIdCopied() {
        snackbar(binding.aboutRootLayout) {
            duration(Snackbar.LENGTH_SHORT)
            message(R.string.settings_about_screen_account_id_copied)
        }.show()
    }

    override fun getScreenName(): AnalyticsEvent = AboutAnalytics.main()
}

@Keep
fun startAboutIntent(context: Context) {
    context.startActivity(Intent(context, AboutActivity::class.java))
}
