/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import android.content.Intent
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.onboarding.OnboardingActivity

internal interface SecretSettingsNavigator {
    fun navigateToStartScreen()
}

internal class SecretSettingsNavigatorImpl : BaseNavigator<SecretSettingsActivity>(),
    SecretSettingsNavigator {

    override fun navigateToStartScreen() = withOwner {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
