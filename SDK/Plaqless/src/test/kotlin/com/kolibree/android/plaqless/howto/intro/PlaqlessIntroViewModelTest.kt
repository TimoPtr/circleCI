/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class PlaqlessIntroViewModelTest : BaseUnitTest() {

    private val navigator = mock<PlaqlessHowToNavigator>()

    private lateinit var viewModel: PlaqlessIntroViewModel

    override fun setup() {
        super.setup()

        viewModel = PlaqlessIntroViewModel(null, navigator)
    }

    @Test
    fun `userClickStart invokes navigator`() {
        viewModel.userClickStart()

        verify(navigator).navigateToSlides()
    }
}
