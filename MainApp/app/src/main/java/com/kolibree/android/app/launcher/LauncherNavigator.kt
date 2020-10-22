/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.home.startHomeScreenIntent
import com.kolibree.android.app.ui.onboarding.startHumOnboardingIntent
import javax.inject.Inject

internal class LauncherNavigator : BaseNavigator<LauncherActivity>() {

    fun openOnboarding() {
        withOwner {
            startHumOnboardingIntent(this)
            finish()
        }
    }

    fun openHomeScreen() {
        withOwner {
            startHomeScreenIntent(this)
            finish()
        }
    }

    fun terminate() {
        withOwner { finish() }
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LauncherNavigator() as T
    }
}
