/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.ongoing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.withWindowInsetsOwner
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.mvi.ConnectionHandlerStateChanged
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.testbrushing.R
import com.kolibree.android.testbrushing.TestBrushingAnalytics
import com.kolibree.android.testbrushing.databinding.FragmentOngoingBrushingBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

@VisibleForApp
class OngoingBrushingFragment : BaseMVIFragment<
    OngoingBrushingViewState,
    BaseGameAction,
    OngoingBrushingViewModel.Factory,
    OngoingBrushingViewModel,
    FragmentOngoingBrushingBinding>(),
    TrackableScreen {

    @Inject
    lateinit var lostConnectionDialogController: LostConnectionDialogController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        withWindowInsetsOwner { insets ->
            with(binding.continueButton) {
                (layoutParams as? ConstraintLayout.LayoutParams)?.also { layoutParams ->
                    layoutParams.bottomMargin += insets.bottomNavigationBarInset()
                } ?: FailEarly.fail("LayoutParams type is not ConstraintLayout.LayoutParams")
            }
        }
        return view
    }

    override fun getViewModelClass(): Class<OngoingBrushingViewModel> =
        OngoingBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_ongoing_brushing

    override fun execute(action: BaseGameAction) {
        when (action) {
            is ConnectionHandlerStateChanged -> updateLostConnectionDialog(action.state)
        }
    }

    private fun updateLostConnectionDialog(state: LostConnectionHandler.State) {
        lostConnectionDialogController.update(state) {
            activity?.finish()
        }
    }

    override fun getScreenName(): AnalyticsEvent = TestBrushingAnalytics.ongoingBrushingScreen()
}
