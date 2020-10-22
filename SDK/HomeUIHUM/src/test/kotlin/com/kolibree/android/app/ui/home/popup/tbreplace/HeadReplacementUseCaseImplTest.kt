/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.GOOD
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.NEEDS_REPLACEMENT
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionData
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class HeadReplacementUseCaseImplTest : BaseUnitTest() {

    private val brushHeadConditionUseCase: BrushHeadConditionUseCase = mock()
    private val headReplacementProvider: HeadReplacementProvider = mock()

    private lateinit var useCase: HeadReplacementUseCaseImpl

    @Before
    fun setUp() {
        useCase = HeadReplacementUseCaseImpl(
            brushHeadConditionUseCase,
            headReplacementProvider
        )
    }

    @Test
    fun `isDisplayable complete with no values if toothbrush head does not need replacement`() {
        val mac = "789654123"

        whenever(brushHeadConditionUseCase.headCondition(mac))
            .thenReturn(Single.just(BrushHeadConditionData(GOOD, LocalDate.MIN)))

        val isDisplayableObserver = useCase.isDisplayable(mac).test()

        isDisplayableObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `isDisplayable complete with no values if toothbrush need replacement but the never show again date is the same as the head replacement date`() {
        val (mac, brushHeadDate) = isDisplayableHappyPath()

        whenever(headReplacementProvider.getWarningHiddenDate(mac))
            .thenReturn(Single.just(brushHeadDate))

        val isDisplayableObserver = useCase.isDisplayable(mac).test()

        isDisplayableObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `isDisplayable complete and returns values if toothbrush need replacement and the never show again date is different than the head replacement date`() {
        val (mac, brushHeadDate) = isDisplayableHappyPath()

        val isDisplayableObserver = useCase.isDisplayable(mac).test()

        isDisplayableObserver
            .assertValue(brushHeadDate)
            .assertComplete()
    }

    @Test
    fun `setReplaceHeadShown calls the setNeverShowAgainDate from the provider with the right params`() {
        val mac = "789654123"
        val finalOdysseyDate = LocalDate.of(3001, 1, 1)
        whenever(headReplacementProvider.setWarningHiddenDate(mac, finalOdysseyDate))
            .thenReturn(Completable.complete())

        useCase.setReplaceHeadShown(mac, finalOdysseyDate).test()

        verify(headReplacementProvider).setWarningHiddenDate(mac, finalOdysseyDate)
    }

    private fun isDisplayableHappyPath(): Pair<String, LocalDate> {
        val mac = "789654123"
        val odysseyTwoDate = LocalDate.of(2010, 1, 1)
        val odysseyThreeDate = LocalDate.of(2061, 1, 1)

        whenever(brushHeadConditionUseCase.headCondition(mac))
            .thenReturn(Single.just(BrushHeadConditionData(NEEDS_REPLACEMENT, odysseyTwoDate)))
        whenever(headReplacementProvider.getWarningHiddenDate(mac))
            .thenReturn(Single.just(odysseyThreeDate))

        return Pair(mac, odysseyTwoDate)
    }
}
