/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.matchers.collections.shouldNotContain
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertTrue
import org.junit.Test

internal class BrushBetterUseCaseImplTest : BaseUnitTest() {

    private val startNonUnityGameUseCase = mock<StartNonUnityGameUseCase>()

    private lateinit var useCase: BrushBetterUseCase

    override fun setup() {
        super.setup()

        val featureToggle = ConstantFeatureToggle(ShowMindYourSpeedFeature, true)
        useCase = BrushBetterUseCaseImpl(startNonUnityGameUseCase, featureToggle)
    }

    @Test
    fun `navigates to guided brushing`() {
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.CoachPlus)).thenReturn(subject)

        useCase.onItemClick(BrushBetterItem.GUIDED_BRUSHING).test()
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `navigates to mind your speed`() {
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.SpeedControl)).thenReturn(subject)

        useCase.onItemClick(BrushBetterItem.MIND_YOUR_SPEED).test()
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `navigates to adjust brushing angle`() {
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestAngles)).thenReturn(subject)

        useCase.onItemClick(BrushBetterItem.ADJUST_BRUSHING_ANGLE).test()
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `navigates to test brushing`() {
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(subject)

        useCase.onItemClick(BrushBetterItem.TEST_BRUSHING).test()
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `adjust angle is hidden`() {
        val items = useCase.getItems().blockingFirst()
        items.shouldNotContain(BrushBetterItem.ADJUST_BRUSHING_ANGLE)
    }
}
