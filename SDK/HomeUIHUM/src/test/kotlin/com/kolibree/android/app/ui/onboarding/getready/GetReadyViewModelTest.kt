/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.getready

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewState
import com.kolibree.android.app.ui.onboarding.initialSharedState
import com.kolibree.android.test.lifecycleTester
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class GetReadyViewModelTest : BaseUnitTest() {

    private val activityState = MutableLiveData<OnboardingSharedViewState>()
    private val sharedViewModel: OnboardingSharedViewModel = mock()
    private lateinit var viewModel: GetReadyViewModel

    override fun setup() {
        super.setup()
        activityState.value = initialSharedState()

        doReturn(activityState).whenever(sharedViewModel).sharedViewStateLiveData
        whenever(sharedViewModel.getSharedViewState()).thenAnswer { activityState.value }

        viewModel = spy(GetReadyViewModel(sharedViewModel))
    }

    @Test
    fun `enableOnScreenBackNavigation is off while VM is at least in started state`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        verify(sharedViewModel).enableOnScreenBackNavigation(false)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_DESTROY)
        verify(sharedViewModel).enableOnScreenBackNavigation(true)
    }

    @Test
    fun `onStart calls reset of the shared state`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        verify(sharedViewModel).resetState()
    }

    @Test
    fun `connectMyBrushButtonClicked triggers StartToothbrushPairing`() {
        val actionListener = viewModel.actionsObservable.test()

        viewModel.connectMyBrushButtonClicked()

        actionListener.assertValue(GetReadyActions.StartToothbrushPairing)
    }

    @Test
    fun `noBrushButtonClicked triggers CreateNewAccount`() {
        val actionListener = viewModel.actionsObservable.test()

        viewModel.noBrushButtonClicked()

        actionListener.assertValue(GetReadyActions.CreateNewAccount)
    }

    @Test
    fun `signInButtonClicked triggers SignIn`() {
        val actionListener = viewModel.actionsObservable.test()

        viewModel.signInButtonClicked()

        actionListener.assertValue(GetReadyActions.SignIn)
    }
}
