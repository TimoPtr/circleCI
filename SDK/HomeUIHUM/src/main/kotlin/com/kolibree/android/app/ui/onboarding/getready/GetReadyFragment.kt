/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.getready

import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentGetReadyBinding
import com.kolibree.android.tracker.TrackableScreen

internal class GetReadyFragment : AnimatedBottomGroupFragment<
    EmptyBaseViewState,
    GetReadyActions,
    GetReadyViewModel.Factory,
    GetReadyViewModel,
    FragmentGetReadyBinding
    >(), TrackableScreen {

    override fun getViewModelClass(): Class<GetReadyViewModel> = GetReadyViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_get_ready

    override fun execute(action: GetReadyActions) {
        when (action) {
            is GetReadyActions.StartToothbrushPairing ->
                navigate(R.id.action_fragment_get_ready_to_pairingActivity)
            is GetReadyActions.CreateNewAccount ->
                navigate(R.id.action_fragment_get_ready_to_fragment_sign_up)
            is GetReadyActions.SignIn ->
                navigate(R.id.action_fragment_get_ready_to_fragment_login)
            else -> FailEarly.fail("Action not recognized")
        }
    }

    override fun getScreenName() = GetReadyAnalytics.main()

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomButtons
}
