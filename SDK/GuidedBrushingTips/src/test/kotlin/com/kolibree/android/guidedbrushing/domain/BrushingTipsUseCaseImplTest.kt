/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.GuidedBrushingTipsFeature
import com.kolibree.android.guidedbrushing.data.BrushingTipsProvider
import com.kolibree.android.test.utils.TestFeatureToggle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class BrushingTipsUseCaseImplTest : BaseUnitTest() {

    private val brushingTipsProvider: BrushingTipsProvider = mock()

    private val appConfiguration: AppConfiguration = mock()
    private val featureToggleSet = TestFeatureToggle(GuidedBrushingTipsFeature, false)

    private lateinit var brushingTipsUseCase: BrushingTipsUseCase

    @Before
    fun setUp() {
        brushingTipsUseCase = BrushingTipsUseCaseImpl(
            brushingTipsProvider,
            appConfiguration,
            setOf(featureToggleSet)
        )
    }

    @Test
    fun `isBrushingTipsDisplayable returns true if the provider, the appConfiguration and the toggleSet are true`() {
        whenever(brushingTipsProvider.isScreenDisplayable()).thenReturn(Single.just(true))
        whenever(appConfiguration.showGuidedBrushingTips).thenReturn(true)
        featureToggleSet.value = true

        brushingTipsUseCase.isBrushingTipsDisplayable()
            .test()
            .assertValue(true)
    }

    @Test
    fun `isBrushingTipsDisplayable call returns false whenever one of the provider, appConfiguration or toggleSet are false`() {
        whenever(brushingTipsProvider.isScreenDisplayable()).thenReturn(Single.just(false))
        whenever(appConfiguration.showGuidedBrushingTips).thenReturn(true)
        featureToggleSet.value = true

        brushingTipsUseCase.isBrushingTipsDisplayable()
            .test()
            .assertValue(false)

        whenever(brushingTipsProvider.isScreenDisplayable()).thenReturn(Single.just(true))
        whenever(appConfiguration.showGuidedBrushingTips).thenReturn(false)
        featureToggleSet.value = true

        brushingTipsUseCase.isBrushingTipsDisplayable()
            .test()
            .assertValue(false)

        whenever(brushingTipsProvider.isScreenDisplayable()).thenReturn(Single.just(true))
        whenever(appConfiguration.showGuidedBrushingTips).thenReturn(true)
        featureToggleSet.value = false

        brushingTipsUseCase.isBrushingTipsDisplayable()
            .test()
            .assertValue(false)
    }

    @Test
    fun setHasClickedNoShowAgain() {
        whenever(brushingTipsProvider.setNoShowAgain())
            .thenReturn(Completable.complete())

        brushingTipsUseCase.setHasClickedNoShowAgain()
            .test()
            .assertComplete()

        verify(brushingTipsProvider).setNoShowAgain()
    }
}
