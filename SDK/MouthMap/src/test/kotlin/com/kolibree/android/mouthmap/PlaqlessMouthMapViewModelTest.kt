/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.ShowPlaqlessVersionOfViewsFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PlaqlessMouthMapViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: MouthMapViewModel

    private val coverageColorMouthZones = emptyMap<MouthZone16, Float>()
    private val plaqlessColorMouthZones: ColorMouthZones = mock()

    private val brushingResults = BrushingResults(
        missedAreas = MISSED,
        buildUpRemains = REMAINS,
        coverage = 100 - MISSED - REMAINS,
        duration = DURATION,
        coverageColorMouthZones = coverageColorMouthZones,
        plaqlessColorMouthZones = plaqlessColorMouthZones,
        hasPlaqlessData = true
    )

    override fun setup() {
        super.setup()

        viewModel = spy(
            MouthMapViewModel(
                brushingResults,
                ConstantFeatureToggle(ShowPlaqlessVersionOfViewsFeature, false)
            )
        )
    }

    @Test
    fun `userClickJaws emits action ToggleJawsView`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.userClickJaws()

        testObserver.assertValue(ToggleJawsView)
    }

    @Test
    fun `userClickRemainsDetails emits action ShowBuildUpRemainsDialog`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.userClickRemainsDetails()

        testObserver.assertValue(ShowBuildUpRemainsDialog)
    }

    @Test
    fun `userClickMissedDetails emits action ShowMissedAreaDialog`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.userClickMissedDetails()

        testObserver.assertValue(ShowMissedAreaDialog)
    }

    @Test
    fun `userClickCleanScore emits action ShowCoverageDialog`() {
        val expectedScore = 100 - REMAINS - MISSED
        val expectedAction = ShowCoverageDialog(
            brushingResults.hasPlaqlessData,
            REMAINS,
            MISSED,
            expectedScore
        )
        val testObserver = viewModel.actionsObservable.test()

        viewModel.userClickCleanScore()

        testObserver.assertValue(expectedAction)
    }

    @Test
    fun `coverage returns state's coverage`() {
        assertEquals(100 - MISSED - REMAINS, viewModel.coverage)
    }

    @Test
    fun `hasCoverageData returns true for coverage gte 0`() {
        assertTrue(viewModel.hasCoverageData)
    }

    @Test
    fun `duration returns formatted brushing duration`() {
        assertEquals("1:18", viewModel.duration)
    }

    @Test
    fun `colorMouthZones returns plaqlessColorMouthZones`() {
        assertEquals(plaqlessColorMouthZones, viewModel.colorMouthZones)
    }
}

private const val REMAINS = 14
private const val MISSED = 43
private const val DURATION = 78
