/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.tab.activities.ActivitiesViewState.Companion.initial
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ActivitiesViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ActivitiesViewModel

    private val toolbarViewModel: HomeToolbarViewModel = mock()
    private val gamesCardViewModel: GamesCardViewModel = mock()

    private val startNonUnityGameUseCase = mock<StartNonUnityGameUseCase>()

    override fun setup() {
        super.setup()

        whenever(startNonUnityGameUseCase.start(any(), any()))
            .thenReturn(Completable.complete())

        viewModel = spy(
            ActivitiesViewModel(
                initialViewState = initial(true),
                startNonUnityGameUseCase = startNonUnityGameUseCase,
                toolbarViewModel = toolbarViewModel,
                gamesCardViewModel = gamesCardViewModel
            )
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
    }

    @Test
    fun `Mind Your Speed is not visible upon request`() {
        assertFalse(
            ActivitiesViewModel(
                initialViewState = initial(false),
                startNonUnityGameUseCase = startNonUnityGameUseCase,
                toolbarViewModel = toolbarViewModel,
                gamesCardViewModel = gamesCardViewModel
            ).testSpeedTask.value!!.visible
        )
    }

    @Test
    fun `testAngleClick starts TestAngles`() {
        viewModel.testAngleClick()

        verify(viewModel).startActivityGame(ActivityGame.TestAngles)
    }

    @Test
    fun `guidedBrushingClick starts CoachPlus`() {
        viewModel.guidedBrushingClick()

        verify(viewModel).startActivityGame(ActivityGame.CoachPlus)
    }

    @Test
    fun `testBrushingClick starts TestBrushing`() {
        viewModel.testBrushingClick()

        verify(viewModel).startActivityGame(ActivityGame.TestBrushing)
    }

    @Test
    fun `testSpeedClick start SpeedControl`() {
        viewModel.testSpeedClick()

        verify(viewModel).startActivityGame(ActivityGame.SpeedControl)
    }

    @Test
    fun `testAngleClick send event`() {
        viewModel.testAngleClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("ShortTasks_AdjustAngle"))
    }

    @Test
    fun `testBrushingClick send event`() {
        viewModel.testBrushingClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("ShortTasks_TestBrushing"))
    }

    @Test
    fun `testSpeedClick send event`() {
        viewModel.testSpeedClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("ShortTasks_MindYourSpeed"))
    }

    @Test
    fun `guidedBrushingClick send event`() {
        viewModel.guidedBrushingClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Activities_GuidedBrushing"))
    }

    @Test
    fun `onResume should send screen name`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker).sendEvent(AnalyticsEvent("Activities-Home"))
    }

    @Test
    fun `isNonUnityGame returns true for non Unity games`() {
        assertTrue(viewModel.isNonUnityGame(ActivityGame.Coach))
        assertTrue(viewModel.isNonUnityGame(ActivityGame.CoachPlus))
        assertTrue(viewModel.isNonUnityGame(ActivityGame.TestAngles))
        assertTrue(viewModel.isNonUnityGame(ActivityGame.TestBrushing))
        assertTrue(viewModel.isNonUnityGame(ActivityGame.SpeedControl))

        assertFalse(viewModel.isNonUnityGame(ActivityGame.Pirate))
        assertFalse(viewModel.isNonUnityGame(ActivityGame.Rabbids))
    }
}
