/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.getready

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

internal class GetReadyViewModel(
    private val sharedViewModel: OnboardingSharedViewModel
) : BaseViewModel<EmptyBaseViewState, GetReadyActions>(EmptyBaseViewState) {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        sharedViewModel.resetState()
        sharedViewModel.enableOnScreenBackNavigation(false)
    }

    override fun onStop(owner: LifecycleOwner) {
        sharedViewModel.enableOnScreenBackNavigation(true)
        super.onStop(owner)
    }

    fun connectMyBrushButtonClicked() {
        Analytics.send(GetReadyAnalytics.connectMyBrushButtonClicked())
        pushAction(GetReadyActions.StartToothbrushPairing)
    }

    fun noBrushButtonClicked() {
        Analytics.send(GetReadyAnalytics.noBrushButtonClicked())
        pushAction(GetReadyActions.CreateNewAccount)
    }

    fun signInButtonClicked() {
        Analytics.send(GetReadyAnalytics.signInButtonClicked())
        pushAction(GetReadyActions.SignIn)
    }

    class Factory @Inject constructor(
        private val sharedViewModel: OnboardingSharedViewModel
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GetReadyViewModel(
                sharedViewModel
            ) as T
    }
}
