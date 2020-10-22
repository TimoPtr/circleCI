/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.enteremail

import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentEnterEmailBinding
import com.kolibree.android.tracker.TrackableScreen

internal class EnterEmailFragment : AnimatedBottomGroupFragment<
    EmptyBaseViewState,
    OnboardingActions,
    EnterEmailViewModel.Factory,
    EnterEmailViewModel,
    FragmentEnterEmailBinding
    >(), TrackableScreen {

    override fun getViewModelClass(): Class<EnterEmailViewModel> = EnterEmailViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_enter_email

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomButtons

    override fun getScreenName() = EnterEmailAnalytics.main()

    override fun execute(action: OnboardingActions) {
        when (action) {
            is OnboardingActivityAction ->
                (activity as? OnboardingActivity)?.execute(action)
                    ?: FailEarly.fail("$activity} is not OnboardingActivity")
            else -> FailEarly.fail("Action not recognized")
        }
    }
}
