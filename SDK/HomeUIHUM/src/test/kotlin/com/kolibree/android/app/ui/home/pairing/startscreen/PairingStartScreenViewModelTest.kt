/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsAnalytics
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class PairingStartScreenViewModelTest : BaseUnitTest() {

    private val navigator: PairingStartScreenNavigator = mock()

    private lateinit var viewModel: PairingStartScreenViewModel

    override fun setup() {
        super.setup()
        viewModel = PairingStartScreenViewModel(navigator)
    }

    @Test
    fun `shopClicked sends shopClicked analytics`() {
        viewModel.shopClicked()

        verify(eventTracker).sendEvent(ToothbrushSettingsAnalytics.main() + "PopUpShop")
    }

    @Test
    fun `connectMyBrushClicked sends connect my brush analytics`() {
        viewModel.connectMyBrushClicked()

        verify(eventTracker).sendEvent(ToothbrushSettingsAnalytics.main() + "PopUpConnectBrush")
    }

    @Test
    fun `shopClicked invokes navigator navigateToShopAndFinish`() {
        viewModel.shopClicked()

        verify(navigator).navigateToShopAndFinish()
    }

    @Test
    fun `connectMyBrushClicked invokes navigator navigateToPairingFlowAndFinish`() {
        viewModel.connectMyBrushClicked()

        verify(navigator).navigateToPairingFlowAndFinish()
    }
}
