/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide1

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SlideOneViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: SlideOneViewModel

    override fun setup() {
        super.setup()

        viewModel = SlideOneViewModel(null)
    }

    @Test
    fun `userClickInfo1() updates view state`() {
        viewModel.userClickInfo1()

        val expectedViewState = SlideOneViewState(
            isInfoSelected1 = true,
            isInfoSelected2 = false,
            isInfoSelected3 = false
        )
        assertEquals(expectedViewState, viewModel.getViewState())
    }

    @Test
    fun `userClickInfo2() updates view state`() {
        viewModel.userClickInfo2()

        val expectedViewState = SlideOneViewState(
            isInfoSelected1 = false,
            isInfoSelected2 = true,
            isInfoSelected3 = false
        )
        assertEquals(expectedViewState, viewModel.getViewState())
    }

    @Test
    fun `userClickInfo3() updates view state`() {
        viewModel.userClickInfo3()

        val expectedViewState = SlideOneViewState(
            isInfoSelected1 = false,
            isInfoSelected2 = false,
            isInfoSelected3 = true
        )
        assertEquals(expectedViewState, viewModel.getViewState())
    }

    @Test
    fun `isInfoSelected1 returns the same value as isInfoSelected1 from viewState`() {
        val observable = viewModel.isInfoSelected1.test()

        viewModel.updateViewState { this.copy(isInfoSelected1 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isInfoSelected1 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }

    @Test
    fun `isInfoSelected2 returns the same value as isInfoSelected2 from viewState`() {
        val observable = viewModel.isInfoSelected2.test()

        viewModel.updateViewState { this.copy(isInfoSelected2 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isInfoSelected2 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }

    @Test
    fun `isInfoSelected3 returns the same value as isInfoSelected3 from viewState`() {
        val observable = viewModel.isInfoSelected3.test()

        viewModel.updateViewState { this.copy(isInfoSelected3 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isInfoSelected3 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }
}
