/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.navigator

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.onboarding.login.LoginFragment

internal interface LoginNavigator {
    fun navigateToLogIn(intent: Intent)
}

internal class LoginNavigatorViewModel : BaseNavigator<LoginFragment>(), LoginNavigator {

    private lateinit var googleAuthContract: ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerActivityResultContracts()
    }

    private fun registerActivityResultContracts() = withOwner {
        googleAuthContract = registerForActivityResult(GoogleAuthContract()) { data ->
            data?.let { onGoogleLogInSucceed(data) } ?: onGoogleLogInFailed()
        }
    }

    override fun navigateToLogIn(intent: Intent) = withOwner {
        googleAuthContract.launch(intent)
    }
}
