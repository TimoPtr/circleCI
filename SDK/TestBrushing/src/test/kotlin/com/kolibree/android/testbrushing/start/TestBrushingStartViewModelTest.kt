/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.start

import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.mvi.brushstart.BrushStartViewState
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.testbrushing.TestBrushingNavigator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class TestBrushingStartViewModelTest : BaseUnitTest() {

    private val state = BrushStartViewState(
        ToothbrushModel.CONNECT_E1,
        "com.kolibree.under.text",
        "00:00:00:00:00:00"
    )

    private val gameInteractor: GameInteractor = mock()

    private val navigator: TestBrushingNavigator = mock()

    private lateinit var viewModel: TestBrushingStartViewModel

    override fun setup() {
        super.setup()
        viewModel =
            spy(TestBrushingStartViewModel(state, gameInteractor, navigator))
    }

    @Test
    fun `onBrushStarted invokes navigator`() {
        viewModel.onBrushStarted(state)
        verify(navigator).startOngoingBrushing()
    }
}
