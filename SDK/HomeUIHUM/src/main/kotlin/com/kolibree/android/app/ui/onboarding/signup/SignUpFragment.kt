/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.input.hideSoftInput
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.text.TextPaintModifiers
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentSignUpBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class SignUpFragment : AnimatedBottomGroupFragment<
    SignUpViewState,
    OnboardingActions,
    SignUpViewModel.Factory,
    SignUpViewModel,
    FragmentSignUpBinding
    >(), TrackableScreen {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.linkStyle = TextPaintModifiers.Builder()
            .withBoldText(true)
            .withUnderlineText(true)
            .build()
        return view
    }

    override fun getViewModelClass(): Class<SignUpViewModel> = SignUpViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_sign_up

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomButtons

    override fun getScreenName(): AnalyticsEvent = SignUpAnalytics.main()

    override fun execute(action: OnboardingActions) {
        when (action) {
            is SignUpActions.HideSoftInput -> binding.nameInputField.hideSoftInput()
            is SignUpActions.OpenTermsAndConditions -> activity?.showInBrowser(R.string.terms_url)
            is SignUpActions.OpenPrivacyPolicy -> activity?.showInBrowser(R.string.privacy_url)
            is SignUpActions.OpenEnterEmail ->
                navigate(R.id.action_fragment_sign_up_to_fragment_enter_email)
            is OnboardingActivityAction ->
                (activity as? OnboardingActivity)?.execute(action)
                    ?: FailEarly.fail("$activity} is not OnboardingActivity")
            else -> FailEarly.fail("Action not recognized")
        }
    }

    fun onGoogleSignUpSucceed(data: Intent) {
        viewModel.onGoogleSignUpSucceed(data)
    }

    fun onGoogleLogInFailed() {
        viewModel.onGoogleLogInFailed()
    }
}
