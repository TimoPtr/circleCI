/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.is_brush_ready

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class IsBrushReadyViewModelTest : BaseUnitTest() {

    private val sharedFacade: PairingFlowSharedFacade = mock()
    private val navigator: PairingNavigator = mock()

    private lateinit var viewModel: IsBrushReadyViewModel

    override fun setup() {
        super.setup()
        viewModel = IsBrushReadyViewModel(sharedFacade, navigator)
    }

    /*
    needHelpClick
     */
    @Test
    fun `needHelpClick invokes navigator navigateToNeedMoreHelp`() {
        viewModel.needHelpClick()

        verify(navigator).navigateToNeedMoreHelp()
    }

    @Test
    fun `needHelpClick send moreHelp analytics`() {
        viewModel.needHelpClick()

        verify(eventTracker).sendEvent(IsBrushReadyAnalytics.moreHelp())
    }

    /*
    connectMyBrushClick
    */
    @Test
    fun `connectMyBrushClick invokes navigator navigateFromIsBrushReadyToWakeYourBrush`() {
        viewModel.connectMyBrushClick()

        verify(navigator).navigateFromIsBrushReadyToWakeYourBrush()
    }

    @Test
    fun `connectMyBrushClick send connect analytics`() {
        viewModel.connectMyBrushClick()

        verify(eventTracker).sendEvent(IsBrushReadyAnalytics.connect()) }
}
