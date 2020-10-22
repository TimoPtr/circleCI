/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.intro

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class TestBrushIntroViewModelTest : BaseUnitTest() {

    private val navigator = mock<TestBrushingNavigator>()

    private lateinit var viewModel: TestBrushIntroViewModel

    override fun setup() {
        super.setup()

        viewModel = spy(TestBrushIntroViewModel(null, navigator))
    }

    @Test
    fun `userClickNext ivokes navigator navigateToSessionScreen`() {
        viewModel.userClickNext()

        verify(navigator).navigateToSessionScreen()
    }

    @Test
    fun `userClickDoLater ivokes navigator finishScreen`() {
        viewModel.userClickDoLater()

        verify(navigator).finishScreen()
    }
}
