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
import com.kolibree.android.app.ui.onboarding.signup.SignUpFragment

internal interface SignUpNavigator {
    fun navigateToSignUp(intent: Intent)
}

internal class SignUpNavigatorViewModel : BaseNavigator<SignUpFragment>(), SignUpNavigator {

    private lateinit var googleAuthContract: ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerActivityResultContracts()
    }

    private fun registerActivityResultContracts() = withOwner {
        googleAuthContract = registerForActivityResult(GoogleAuthContract()) { data ->
            data?.let { onGoogleSignUpSucceed(data) } ?: onGoogleLogInFailed()
        }
    }

    override fun navigateToSignUp(intent: Intent) {
        googleAuthContract.launch(intent)
    }
}
