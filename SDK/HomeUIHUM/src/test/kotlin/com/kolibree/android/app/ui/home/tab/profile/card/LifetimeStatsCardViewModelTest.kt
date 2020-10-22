/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card

import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.LifetimeSmilesUseCase
import com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.LifetimeStatsCardViewModel
import com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.LifetimeStatsCardViewState
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.charts.inoff.domain.InOffBrushingsCountProvider
import com.kolibree.charts.inoff.domain.model.InOffBrushingsCount
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class LifetimeStatsCardViewModelTest : BaseUnitTest() {
    private val smilesUseCase: SmilesUseCase = mock()
    private val lifetimeSmilesUseCase: LifetimeSmilesUseCase = mock()
    private val inOffBrushingsCountProvider: InOffBrushingsCountProvider = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private lateinit var viewModel: LifetimeStatsCardViewModel

    override fun setup() {
        super.setup()

        viewModel =
            LifetimeStatsCardViewModel(
                initialViewState = LifetimeStatsCardViewState.initial(DynamicCardPosition.ZERO),
                smilesUseCase = smilesUseCase,
                lifetimeSmilesUseCase = lifetimeSmilesUseCase,
                inOffBrushingsCountProvider = inOffBrushingsCountProvider,
                currentProfileProvider = currentProfileProvider
            )
    }

    /*
    onStart
     */
    @Test
    fun `onStart subscribes to smilesUseCase`() {
        val smilesSubject = prepareSmilesUseCase()
        prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        viewModel.pushLifecycleTo(ON_START)

        assertTrue(smilesSubject.hasSubscribers())
    }

    @Test
    fun `onStart subscribes to lifetimeSmilesSubject`() {
        prepareSmilesUseCase()
        val lifetimeSmilesSubject = prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        viewModel.pushLifecycleTo(ON_START)

        assertTrue(lifetimeSmilesSubject.hasSubscribers())
    }

    @Test
    fun `viewState is updated when smilesUseCase emits new value`() {
        val subject = prepareSmilesUseCase()
        prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(LifetimeStatsCardViewState.initial(DynamicCardPosition.ZERO))

        viewModel.pushLifecycleTo(ON_START)

        val expectedSmiles = 4354
        subject.onNext(expectedSmiles)

        viewStateObserver.assertLastValue(
            LifetimeStatsCardViewState
                .initial(DynamicCardPosition.ZERO)
                .copy(
                    isLoading = false,
                    currentPoints = expectedSmiles
                )
        )
    }

    @Test
    fun `viewState is updated when lifetimeSmilesUseCase emits new value`() {
        prepareSmilesUseCase()
        val lifetimeSmilesSubject = prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(LifetimeStatsCardViewState.initial(DynamicCardPosition.ZERO))

        viewModel.pushLifecycleTo(ON_START)

        val expectedSmiles = 4354
        lifetimeSmilesSubject.onNext(expectedSmiles)

        viewStateObserver.assertLastValue(
            LifetimeStatsCardViewState
                .initial(DynamicCardPosition.ZERO)
                .copy(
                    isLoading = false,
                    lifetimePoints = expectedSmiles
                )
        )
    }

    @Test
    fun `viewState is updated when inOffBrushingsCountProvider emits new value`() {
        prepareSmilesUseCase()
        prepareLifetimeSmilesUseCase()
        val inOffBrushingsCountProvider = prepareLifestatsInOffBrushingsCountProvider()

        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(LifetimeStatsCardViewState.initial(DynamicCardPosition.ZERO))

        viewModel.pushLifecycleTo(ON_START)

        val expectedInAppBrushings = 12
        val expectedOfflineBrushings = 38
        val nextItem = InOffBrushingsCount(
            profileId = 123L,
            onlineBrushingCount = expectedInAppBrushings,
            offlineBrushingCount = expectedOfflineBrushings
        )
        inOffBrushingsCountProvider.onNext(nextItem)

        viewStateObserver.assertLastValue(
            LifetimeStatsCardViewState
                .initial(DynamicCardPosition.ZERO)
                .copy(
                    isLoading = false,
                    inAppCount = expectedInAppBrushings,
                    offlineCount = expectedOfflineBrushings
                )
        )
    }

    /*
    onStop
     */

    @Test
    fun `onStop disposes subscription to smilesUseCase`() {
        val subject = prepareSmilesUseCase()
        prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        viewModel.pushLifecycleTo(ON_START)

        assertTrue(subject.hasSubscribers())

        viewModel.pushLifecycleTo(ON_STOP)

        assertFalse(subject.hasSubscribers())
    }

    @Test
    fun `onStop disposes subscription to lifetimeSmilesUseCase`() {
        prepareSmilesUseCase()
        val lifetimeSmilesSubject = prepareLifetimeSmilesUseCase()
        prepareLifestatsInOffBrushingsCountProvider()

        viewModel.pushLifecycleTo(ON_START)

        assertTrue(lifetimeSmilesSubject.hasSubscribers())

        viewModel.pushLifecycleTo(ON_STOP)

        assertFalse(lifetimeSmilesSubject.hasSubscribers())
    }

    @Test
    fun `onStop disposes subscription to inOffBrushingsCountProvider`() {
        prepareSmilesUseCase()
        prepareLifetimeSmilesUseCase()
        val inOffBrushingsCountProvider = prepareLifestatsInOffBrushingsCountProvider()

        viewModel.pushLifecycleTo(ON_START)

        assertTrue(inOffBrushingsCountProvider.hasSubscribers())

        viewModel.pushLifecycleTo(ON_STOP)

        assertFalse(inOffBrushingsCountProvider.hasSubscribers())
    }

    /*
    Utils
     */

    private fun prepareSmilesUseCase(): PublishProcessor<Int> {
        val subject = PublishProcessor.create<Int>()
        whenever(smilesUseCase.smilesAmountStream()).thenReturn(subject)
        return subject
    }

    private fun prepareLifetimeSmilesUseCase(): PublishProcessor<Int> {
        val subject = PublishProcessor.create<Int>()
        whenever(lifetimeSmilesUseCase.lifetimePoints()).thenReturn(subject)
        return subject
    }

    private fun prepareLifestatsInOffBrushingsCountProvider(): PublishProcessor<InOffBrushingsCount> {
        val profile = ProfileBuilder.create().build()
        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(profile))
        val subject = PublishProcessor.create<InOffBrushingsCount>()
        whenever(inOffBrushingsCountProvider.brushingsCountStream(profile.id)).thenReturn(subject)
        return subject
    }
}
