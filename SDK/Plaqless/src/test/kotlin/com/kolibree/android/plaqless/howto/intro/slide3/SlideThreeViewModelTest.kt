/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide3

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.plaqless.howto.intro.PlaqlessHowToNavigator
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.Mock

internal class SlideThreeViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: SlideThreeViewModel

    @Mock
    private lateinit var navigator: PlaqlessHowToNavigator

    override fun setup() {
        super.setup()

        viewModel = SlideThreeViewModel(null, navigator)
    }

    @Test
    fun `tryNowClick invokes navigatesToTestBrushing`() {
        viewModel.tryNowClick()

        verify(navigator, times(1)).navigateToTestBrushing()
    }

    @Test
    fun `tryLaterClick invokes finish`() {
        viewModel.tryLaterClick()

        verify(navigator, times(1)).finish()
    }
}
