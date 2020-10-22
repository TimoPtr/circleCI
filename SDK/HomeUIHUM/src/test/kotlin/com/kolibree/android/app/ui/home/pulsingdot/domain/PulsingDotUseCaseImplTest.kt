/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotProvider
import com.kolibree.android.feature.PulsingDotFeature
import com.kolibree.android.test.utils.TestFeatureToggle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class PulsingDotUseCaseImplTest : BaseUnitTest() {

    private lateinit var pulsingDotProvider: PulsingDotProvider
    private lateinit var pulsingDotFeatureToggle: TestFeatureToggle<Boolean>
    private lateinit var useCaseImpl: PulsingDotUseCaseImpl

    @Before
    fun init() {
        pulsingDotProvider = mock()
        pulsingDotFeatureToggle = TestFeatureToggle(PulsingDotFeature, initialValue = false)
        useCaseImpl = PulsingDotUseCaseImpl(pulsingDotProvider, setOf(pulsingDotFeatureToggle))
    }

    @Test
    fun `shouldShowPulsingDot should return true when it is forced from the feature toggle`() {
        pulsingDotFeatureToggle.value = true

        val shouldShowSingle = useCaseImpl.shouldShowPulsingDot(PulsingDot.SMILE).test()

        shouldShowSingle.assertResult(true)
        verifyZeroInteractions(pulsingDotProvider)
    }

    @Test
    fun `shouldShowPulsingDot should return false when the user already clicked`() {
        whenever(pulsingDotProvider.isClicked(PulsingDotPersistence.SMILE))
            .thenReturn(Observable.just(true))
        whenever(pulsingDotProvider.getTimesShown(PulsingDotPersistence.SMILE)).thenReturn(1)

        val shouldShowSingle = useCaseImpl.shouldShowPulsingDot(PulsingDot.SMILE).test()

        shouldShowSingle.assertResult(false)
    }

    @Test
    fun `shouldShowPulsingDot should return false when the user have seen five times`() {
        whenever(pulsingDotProvider.isClicked(PulsingDotPersistence.SMILE))
            .thenReturn(Observable.just(false))
        whenever(pulsingDotProvider.getTimesShown(PulsingDotPersistence.SMILE)).thenReturn(5)

        val shouldShowSingle = useCaseImpl.shouldShowPulsingDot(PulsingDot.SMILE).test()

        shouldShowSingle.assertResult(false)
    }

    @Test
    fun `shouldShowPulsingDot should return true when the user has never seen and clicked the pulsing dot and should increment the provider`() {
        whenever(pulsingDotProvider.isClicked(PulsingDotPersistence.SMILE))
            .thenReturn(Observable.just(false))
        whenever(pulsingDotProvider.getTimesShown(PulsingDotPersistence.SMILE)).thenReturn(0)

        val shouldShowSingle = useCaseImpl.shouldShowPulsingDot(PulsingDot.SMILE).test()

        shouldShowSingle.assertResult(true)
        verify(pulsingDotProvider).incTimesShown(PulsingDotPersistence.SMILE)
    }

    @Test
    fun `should show explanation should be true when the user has never seen it`() {
        whenever(pulsingDotProvider.isExplanationShown()).thenReturn(Observable.just(false))

        val shouldShowSingle = useCaseImpl.shouldShowExplanation().test()

        shouldShowSingle.assertResult(true)
    }

    @Test
    fun `should show explanation should be false when the user has seen the explanation`() {
        whenever(pulsingDotProvider.isExplanationShown()).thenReturn(Observable.just(true))

        val shouldShowSingle = useCaseImpl.shouldShowExplanation().test()

        shouldShowSingle.assertResult(false)
    }

    @Test
    fun `onPulsingDotClicked should notify the provider`() {
        useCaseImpl.onPulsingDotClicked(PulsingDot.SMILE)

        verify(pulsingDotProvider).setIsClicked(PulsingDotPersistence.SMILE)
    }

    @Test
    fun `onPulsingDotExplanationShown should notify the provider`() {
        useCaseImpl.onExplanationShown()

        verify(pulsingDotProvider).setExplanationShown()
    }
}
