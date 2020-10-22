/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

class CompleteProfileBubbleViewModelTest : BaseUnitTest() {

    private val completeProfileBubbleUseCase: CompleteProfileBubbleUseCase = mock()

    private lateinit var viewModel: CompleteProfileBubbleViewModel

    override fun setup() {
        super.setup()
        viewModel = CompleteProfileBubbleViewModel(
            CompleteProfileBubbleViewState.initial(),
            completeProfileBubbleUseCase
        )
    }

    @Test
    fun `hide bubble if data stream asks for it`() {
        whenever(completeProfileBubbleUseCase.getShowCompleteProfileBubbleStream())
            .thenReturn(Flowable.just(true))
        whenever(completeProfileBubbleUseCase.getProfileCompletionPercentageStream())
            .thenReturn(Flowable.just(0))

        val showBubbleTester = viewModel.profileBubbleVisible.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        showBubbleTester.assertValueHistory(false, true)
        verify(eventTracker).sendEvent(CompleteProfileBubbleAnalytics.show())
    }

    @Test
    fun `pass completion progress to the view`() {
        val expectedProgressSequence = (0..100).step(10)

        whenever(completeProfileBubbleUseCase.getShowCompleteProfileBubbleStream())
            .thenReturn(Flowable.just(false))
        whenever(completeProfileBubbleUseCase.getProfileCompletionPercentageStream())
            .thenReturn(Flowable.fromIterable(expectedProgressSequence.toList()))

        val progressTester = viewModel.profileBubbleProgress.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        progressTester.assertValueHistory(
            *(expectedProgressSequence.map { it.toFloat() / 100 }.toList().toTypedArray())
        )
    }

    @Test
    fun `pass user click to use case`() {
        viewModel.onProfileBubbleGotItClick()
        verify(completeProfileBubbleUseCase).suppressBubble()
        verify(eventTracker).sendEvent(CompleteProfileBubbleAnalytics.close())
    }
}
