/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.rewards.ProfileTierUseCase
import com.kolibree.android.rewards.persistence.ProfileTierOptional
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import org.junit.Test

class EarningPointsCardViewModelTest : BaseUnitTest() {

    private val profileTierUseCase: ProfileTierUseCase = mock()

    private val navigator: HumHomeNavigator = mock()

    private lateinit var viewModel: EarningPointsCardViewModel

    override fun setup() {
        super.setup()

        viewModel = EarningPointsCardViewModel(
            initialViewState = EarningPointsCardViewState.initial(DynamicCardPosition.ZERO),
            navigator = navigator,
            profileTierUseCase = profileTierUseCase
        )
    }

    @Test
    fun `onCreate starts listening for profile tier events`() {
        val observer = viewModel.viewStateFlowable.test()

        viewModel.onStart(mock())

        observer.assertLastValueWithPredicate { it.pointsPerBrush == 1 }
    }

    @Test
    fun `calling toggleExpanded() toggles expanded state`() {
        val observer = viewModel.viewStateFlowable.test()
        val profileTierProcessor = PublishProcessor.create<ProfileTierOptional>()

        whenever(profileTierUseCase.currentProfileTier()).thenReturn(profileTierProcessor)

        viewModel.onStart(mock())

        observer.assertLastValueWithPredicate { it.expanded.not() }

        viewModel.toggleExpanded(mock())

        observer.assertLastValueWithPredicate { it.expanded }

        viewModel.toggleExpanded(mock())

        observer.assertLastValueWithPredicate { it.expanded.not() }
    }

    @Test
    fun `toggleExpanded send analytics event close when collapsing`() {
        viewModel.updateViewState { copy(expanded = true) }
        viewModel.toggleExpanded(mock())

        verify(eventTracker).sendEvent(AnalyticsEvent("EarningPoints_Information_close"))
    }

    @Test
    fun `toggleExpanded send analytics event open when expanding`() {
        viewModel.updateViewState { copy(expanded = false) }
        viewModel.toggleExpanded(mock())

        verify(eventTracker).sendEvent(AnalyticsEvent("EarningPoints_Information"))
    }
}
