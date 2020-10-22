/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject
import zendesk.support.Support
import zendesk.support.guide.HelpCenterActivity

internal class HelpNavigator : BaseNavigator<HelpActivity>() {

    fun closeScreen() = withOwner {
        finish()
    }

    fun showHelpCenter() = withOwner {
        FailEarly.failInConditionMet(!Support.INSTANCE.isInitialized, "Zendesk is not initialized")
        HelpCenterActivity.builder().withContactUsButtonVisible(false)
            .show(this)
    }

    fun showContactUs() = withOwner {
        showInBrowser(R.string.contact_us_url)
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HelpNavigator() as T
    }
}
