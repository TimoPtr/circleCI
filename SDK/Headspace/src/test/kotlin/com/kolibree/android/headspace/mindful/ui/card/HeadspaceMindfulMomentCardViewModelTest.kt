/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui.card

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.headspace.mindful.HeadspaceMindfulMomentNavigator
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentUseCase
import com.kolibree.android.headspace.mindful.ui.shared.SAMPLE_MINDFUL_MOMENT
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HeadspaceMindfulMomentCardViewModelTest : BaseUnitTest() {
    private val statusSubject = PublishProcessor.create<HeadspaceMindfulMomentStatus>()

    private val navigator = mock<HeadspaceMindfulMomentNavigator>()
    private val mindfulMomentUseCase = mock<HeadspaceMindfulMomentUseCase> {
        on { getHeadspaceMindfulMomentStatus() } doReturn statusSubject
    }

    private lateinit var viewModel: HeadspaceMindfulMomentCardViewModel

    override fun setup() {
        super.setup()
        initWithViewState()
    }

    @Test
    fun `onResume subscribes to mindfulMomentUseCaseStream`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(statusSubject.hasSubscribers())
    }

    @Test
    fun `when statusSubject emits NotAvailable state expected ViewState is emitted`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val viewStateObserver = viewModel.viewStateFlowable.test()

        val status = HeadspaceMindfulMomentStatus.NotAvailable
        statusSubject.onNext(status)

        val expectedViewState = initialViewState().withNotAvailableStatus(status)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `when statusSubject emits Available state expected ViewState is emitted`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val viewStateObserver = viewModel.viewStateFlowable.test()

        val status = HeadspaceMindfulMomentStatus.Available(
            SAMPLE_MINDFUL_MOMENT
        )
        statusSubject.onNext(status)

        val expectedViewState = initialViewState().withAvailableStatus(status)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `card onClick calls navigator and sends analytics event`() {
        initWithViewState(viewState = initialViewState().copy(mindfulMoment = SAMPLE_MINDFUL_MOMENT))

        viewModel.onClick()

        verify(navigator).showMindfulMomentScreen(SAMPLE_MINDFUL_MOMENT)
        verify(eventTracker).sendEvent(AnalyticsEvent("HeadSpace_MM_Open"))
    }

    private fun initWithViewState(viewState: HeadspaceMindfulMomentCardViewState = initialViewState()) {
        viewModel = HeadspaceMindfulMomentCardViewModel(
            initialViewState = viewState,
            mindfulMomentUseCase = mindfulMomentUseCase,
            mindfulMomentNavigator = navigator
        )
    }

    private fun initialViewState() = HeadspaceMindfulMomentCardViewState.initial(
        position = DynamicCardPosition.EIGHT
    )
}
