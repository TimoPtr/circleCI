/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.login

import android.content.Intent
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentLoginBinding
import com.kolibree.android.tracker.TrackableScreen

internal class LoginFragment : AnimatedBottomGroupFragment<
    EmptyBaseViewState,
    OnboardingActions,
    LoginViewModel.Factory,
    LoginViewModel,
    FragmentLoginBinding
    >(), TrackableScreen {

    override fun getViewModelClass(): Class<LoginViewModel> = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_login

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomButtons

    override fun getScreenName() = LoginAnalytics.main()

    override fun execute(action: OnboardingActions) {
        when (action) {
            is OnboardingActivityAction ->
                (activity as? OnboardingActivity)?.execute(action)
                    ?: FailEarly.fail("$activity} is not OnboardingActivity")
            is LoginActions.OpenCheckYourEmail ->
                navigate(R.id.action_fragment_login_to_fragment_check_email)
            else -> FailEarly.fail("Action not recognized")
        }
    }

    fun onGoogleLogInSucceed(data: Intent) {
        viewModel.onGoogleLogInSucceed(data)
    }

    fun onGoogleLogInFailed() {
        viewModel.onGoogleLogInFailed()
    }
}
