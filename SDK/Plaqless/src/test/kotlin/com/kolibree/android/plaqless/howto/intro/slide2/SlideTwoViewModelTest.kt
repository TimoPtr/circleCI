/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide2

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class SlideTwoViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: SlideTwoViewModel

    override fun setup() {
        super.setup()

        viewModel = SlideTwoViewModel(null)
    }

    @Test
    fun `Description1 is selected in initial State`() {
        viewModel.isDescriptionSelected1.test().assertValue(true)
    }

    @Test
    fun `userClickDescription1() updates view state`() {
        viewModel.userClickDescription1()

        val expectedViewState = SlideTwoViewState(
            isDescriptionSelected1 = true,
            isDescriptionSelected2 = false,
            isDescriptionSelected3 = false
        )
        assertEquals(expectedViewState, viewModel.getViewState())
        viewModel.isDescriptionSelected1.test().assertValue(true)
        viewModel.isDescriptionSelected2.test().assertValue(false)
        viewModel.isDescriptionSelected3.test().assertValue(false)
    }

    @Test
    fun `userClickDescription2() updates view state`() {
        viewModel.userClickDescription2()

        val expectedViewState = SlideTwoViewState(
            isDescriptionSelected1 = false,
            isDescriptionSelected2 = true,
            isDescriptionSelected3 = false
        )
        assertEquals(expectedViewState, viewModel.getViewState())
        viewModel.isDescriptionSelected1.test().assertValue(false)
        viewModel.isDescriptionSelected2.test().assertValue(true)
        viewModel.isDescriptionSelected3.test().assertValue(false)
    }

    @Test
    fun `userClickDescription3() updates view state`() {
        viewModel.userClickDescription3()

        val expectedViewState = SlideTwoViewState(
            isDescriptionSelected1 = false,
            isDescriptionSelected2 = false,
            isDescriptionSelected3 = true
        )
        assertEquals(expectedViewState, viewModel.getViewState())
        viewModel.isDescriptionSelected1.test().assertValue(false)
        viewModel.isDescriptionSelected2.test().assertValue(false)
        viewModel.isDescriptionSelected3.test().assertValue(true)
    }

    @Test
    fun `isDescriptionSelected1 returns the same value as isDescriptionSelected1 from viewState`() {
        val observable = viewModel.isDescriptionSelected1.test()

        viewModel.updateViewState { this.copy(isDescriptionSelected1 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isDescriptionSelected1 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }

    @Test
    fun `isDescriptionSelected2 returns the same value as isDescriptionSelected2 from viewState`() {
        val observable = viewModel.isDescriptionSelected2.test()

        viewModel.updateViewState { this.copy(isDescriptionSelected2 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isDescriptionSelected2 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }

    @Test
    fun `isDescriptionSelected3 returns the same value as isDescriptionSelected3 from viewState`() {
        val observable = viewModel.isDescriptionSelected3.test()

        viewModel.updateViewState { this.copy(isDescriptionSelected3 = true) }

        observable.assertHasValue()
        observable.assertValue(true)

        viewModel.updateViewState { this.copy(isDescriptionSelected3 = false) }

        observable.assertHasValue()
        observable.assertValue(false)
    }
}
