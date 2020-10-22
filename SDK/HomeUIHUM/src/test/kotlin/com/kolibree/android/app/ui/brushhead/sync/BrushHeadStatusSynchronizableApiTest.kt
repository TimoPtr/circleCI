/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.brushhead.sync.model.BrushHeadInformationSet
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountToothbrush
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime

class BrushHeadStatusSynchronizableApiTest : BaseUnitTest() {

    private val brushHeadRepository: BrushHeadRepository = mock()
    private val toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase = mock()

    private val api =
        BrushHeadStatusSynchronizableApi(brushHeadRepository, toothbrushesForProfileUseCase)

    @Test
    fun `get doesn't interact with useCase if profile has 0 brushes`() {
        mockProfileToothbrushes()

        api.get(profileId)

        verifyNoMoreInteractions(brushHeadRepository)
    }

    @Test
    fun `get returns BrushHeadReplacedDates with info from backend for each AccountToothbrush`() {
        val mac1 = "mac1"
        val toothbrush1 = createAccountToothbrush(mac1)
        val mac2 = "mac2"
        val toothbrush2 = createAccountToothbrush(mac2)
        mockProfileToothbrushes(listOf(toothbrush1, toothbrush2))

        val brushHeadInfo1 =
            brushHeadInfo(
                mac = toothbrush1.mac,
                resetDate = TrustedClock.getNowOffsetDateTime().minusMinutes(1)
            )
        whenever(
            brushHeadRepository.getBrushHeadInformationFromApi(
                serialNumber = toothbrush1.serial,
                mac = toothbrush1.mac
            )
        )
            .thenReturn(Single.just(brushHeadInfo1))

        val brushHeadInfo2 =
            brushHeadInfo(
                mac = toothbrush2.mac,
                resetDate = TrustedClock.getNowOffsetDateTime().minusMinutes(2)
            )
        whenever(
            brushHeadRepository.getBrushHeadInformationFromApi(
                serialNumber = toothbrush2.serial,
                mac = toothbrush2.mac
            )
        )
            .thenReturn(Single.just(brushHeadInfo2))

        val replacedDates = api.get(profileId)

        val expectedBrushHeadDates = BrushHeadInformationSet(setOf(brushHeadInfo1, brushHeadInfo2))

        assertEquals(expectedBrushHeadDates, replacedDates)
    }

    /*
    Utils
     */

    private fun mockProfileToothbrushes(toothbrushes: List<AccountToothbrush> = emptyList()) {
        whenever(toothbrushesForProfileUseCase.profileAccountToothbrushesOnceAndStream(profileId))
            .thenReturn(
                Flowable.just(
                    toothbrushes,
                    emptyList()
                )
            ) // make sure we only read 1st value
    }

    companion object {
        private const val profileId = ProfileBuilder.DEFAULT_ID
    }
}

internal fun brushHeadInfo(
    mac: String = KLTBConnectionBuilder.DEFAULT_MAC,
    resetDate: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    percentageLeft: Int = 100
): BrushHeadInformation {
    return BrushHeadInformation(
        macAddress = mac,
        resetDate = resetDate,
        percentageLeft = percentageLeft
    )
}
