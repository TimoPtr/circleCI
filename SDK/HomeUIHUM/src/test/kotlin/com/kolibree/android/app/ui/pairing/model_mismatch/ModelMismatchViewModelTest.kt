/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.model_mismatch

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class ModelMismatchViewModelTest : BaseUnitTest() {

    private val sharedFacade: PairingFlowSharedFacade = mock()
    private val navigator: PairingNavigator = mock()

    private lateinit var viewmodel: ModelMismatchViewModel

    override fun setup() {
        super.setup()

        viewmodel = ModelMismatchViewModel(sharedFacade, navigator)
    }

    /*
    continueAnywayClick
     */
    @Test
    fun `continueAnywayClick invokes navigator navigateFromModelMismatchToSignUp when onboarding flow`() {
        whenever(sharedFacade.isOnboardingFlow()).thenReturn(true)

        viewmodel.continueAnywayClick()

        verify(navigator).navigateFromModelMismatchToSignUp()
    }

    @Test
    fun `continueAnywayClick invokes navigator finishFlow when not onboarding flow`() {
        whenever(sharedFacade.isOnboardingFlow()).thenReturn(false)

        viewmodel.continueAnywayClick()

        verify(navigator).finishFlow()
    }

    @Test
    fun `continueAnywayClick sends continueAnyway analytics`() {
        viewmodel.continueAnywayClick()

        verify(eventTracker).sendEvent(ModelMismatchAnalytics.continueAnyway())
    }

    /*
    changeApp
     */
    @Test
    fun `changeApp invokes navigator navigateToColgateConnectPlayStore`() {
        viewmodel.changeApp()

        verify(navigator).navigateToSecondAppPlayStore()
    }

    @Test
    fun `changeApp sends changeApp analytics`() {
        viewmodel.changeApp()

        verify(eventTracker).sendEvent(ModelMismatchAnalytics.changeApp())
    }
}
