/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot.FREQUENCY_CHART
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxConfiguration
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.atStartOfKolibreeDay
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.YearMonth

internal class FrequencyCardViewModelTest : BaseUnitTest() {

    private val brushingUseCase = mock<FrequencyBrushingUseCase>()
    private val homeNavigator = mock<HumHomeNavigator>()
    private val pulsingDotUseCase = mock<PulsingDotUseCase>()
    private val toolboxViewModel = mock<ToolboxViewModel>()

    lateinit var viewModel: FrequencyCardViewModel

    override fun setup() {
        super.setup()

        whenever(brushingUseCase.prepareDataBeforeMonth(any()))
            .thenReturn(Completable.complete())

        whenever(pulsingDotUseCase.shouldShowPulsingDot(any()))
            .thenReturn(Flowable.empty())

        viewModel = FrequencyCardViewModel(
            initialViewState = FrequencyCardViewState.initial(DynamicCardPosition.ZERO),
            frequencyBrushingUseCase = brushingUseCase,
            homeNavigator = homeNavigator,
            pulsingDotUseCase = pulsingDotUseCase,
            toolboxViewModel = toolboxViewModel
        )
    }

    @Test
    fun `onPageBackClick updates currentMonth to previous one`() {
        viewModel.updateViewState {
            copy(
                currentMonthFromNow = 1,
                monthsData = listOf(
                    FrequencyChartViewState(),
                    FrequencyChartViewState(),
                    FrequencyChartViewState()
                )
            )
        }

        viewModel.onPageBackClick()
        assertEquals(2, viewModel.getViewState()?.currentMonthFromNow)
    }

    @Test
    fun `onPageBackClick sends previousMonth event`() {
        viewModel.onPageBackClick()
        verify(eventTracker).sendEvent(AnalyticsEvent("FrequencyChart_PassedMonth"))
    }

    @Test
    fun `onPageBackClick invokes prepareDataBeforeMonth on brushing use case`() {
        viewModel.updateViewState {
            copy(
                currentMonthFromNow = 0,
                monthsData = listOf(
                    FrequencyChartViewState(),
                    FrequencyChartViewState()
                )
            )
        }
        viewModel.onPageBackClick()
        verify(brushingUseCase).prepareDataBeforeMonth(YearMonth.now().minusMonths(1L))
    }

    @Test
    fun `onPageForwardClick updates currentMonth to next one`() {
        viewModel.updateViewState {
            copy(
                currentMonthFromNow = 1,
                monthsData = listOf(
                    FrequencyChartViewState(),
                    FrequencyChartViewState(),
                    FrequencyChartViewState()
                )
            )
        }

        viewModel.onPageForwardClick()
        assertEquals(0, viewModel.getViewState()?.currentMonthFromNow)
    }

    @Test
    fun `onPageForwardClick invokes prepareDataBeforeMonth on brushing use case`() {
        viewModel.updateViewState {
            copy(
                currentMonthFromNow = 1,
                monthsData = listOf(
                    FrequencyChartViewState(),
                    FrequencyChartViewState()
                )
            )
        }
        viewModel.onPageForwardClick()
        verify(brushingUseCase).prepareDataBeforeMonth(YearMonth.now())
    }

    @Test
    fun `onPageForwardClick sends nextMonth event`() {
        viewModel.onPageForwardClick()
        verify(eventTracker).sendEvent(AnalyticsEvent("FrequencyChart_NextMonth"))
    }

    @Test
    fun `onDayClick navigates to Day Checkup screen`() {
        val day = TrustedClock.getNowLocalDate()
        viewModel.onDayClick(day)

        verify(homeNavigator).navigateToDayCheckup(day.atStartOfKolibreeDay())
    }

    @Test
    fun `onDayClick sends day event`() {
        viewModel.onDayClick(TrustedClock.getNowLocalDate())
        verify(eventTracker).sendEvent(AnalyticsEvent("FrequencyChart_Day"))
    }

    @Test
    fun `update view state after pulsing dot state changed`() {
        val shouldShowStream = PublishProcessor.create<Boolean>()
        whenever(pulsingDotUseCase.shouldShowPulsingDot(FREQUENCY_CHART))
            .thenReturn(shouldShowStream)
        whenever(toolboxViewModel.factory()).thenReturn(mock())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        assertFalse(viewModel.getViewState()!!.pulsingDotVisible)

        shouldShowStream.offer(true)
        assertTrue(viewModel.getViewState()!!.pulsingDotVisible)

        shouldShowStream.offer(false)
        assertFalse(viewModel.getViewState()!!.pulsingDotVisible)
    }

    @Test
    fun `show proper toolbox and call use case after click on pulsing dot`() {
        val mockConfiguration: ToolboxConfiguration = mock()
        val mockFactory: ToolboxConfiguration.Factory = mock()

        whenever(pulsingDotUseCase.shouldShowPulsingDot(FREQUENCY_CHART))
            .thenReturn(Flowable.just(true))
        whenever(toolboxViewModel.factory()).thenReturn(mockFactory)
        whenever(mockFactory.frequencyChart()).thenReturn(mockConfiguration)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onPulsingDotClick()

        verify(pulsingDotUseCase).onPulsingDotClicked(eq(FREQUENCY_CHART))
        verify(toolboxViewModel).show(eq(mockConfiguration))
    }

    @Test
    fun `profile change resets current month`() {
        val testPublisher = PublishProcessor.create<Pair<Profile, List<FrequencyChartViewState>>>()

        whenever(brushingUseCase.getBrushingStateForCurrentProfile())
            .thenReturn(testPublisher)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        testPublisher.offer(mockProfile(profileId = 1) to mockChartViewState())
        assertEquals(0, viewModel.getViewState()!!.currentMonthFromNow)

        viewModel.onPageBackClick()
        viewModel.onPageBackClick()
        assertEquals(2, viewModel.getViewState()!!.currentMonthFromNow)

        // Emitting the same profile should not reset current month
        testPublisher.offer(mockProfile(profileId = 1) to mockChartViewState())
        assertEquals(2, viewModel.getViewState()!!.currentMonthFromNow)

        // Emitting new profile should reset current month
        testPublisher.offer(mockProfile(profileId = 2) to mockChartViewState())
        assertEquals(0, viewModel.getViewState()!!.currentMonthFromNow)
    }

    private fun mockProfile(profileId: Long): Profile {
        return mock<Profile>().apply {
            whenever(id).thenReturn(profileId)
        }
    }

    private fun mockChartViewState(): List<FrequencyChartViewState> {
        return (0..50).map { FrequencyChartViewState(emptyList()) }
    }
}
