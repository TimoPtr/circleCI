/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.usecase

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.brushhead.sync.brushHeadInfo
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorkerConfigurator
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorkerConfigurator.Payload
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.OffsetDateTime.of
import org.threeten.bp.ZoneOffset

internal class BrushHeadConditionUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: BrushHeadConditionUseCase

    private val brushHeadRepository: BrushHeadRepository = mock()

    private val replaceBrushHeadWorkerConfigurator: ReplaceBrushHeadWorkerConfigurator = mock()

    private val toothbrushRepository: ToothbrushRepository = mock()

    override fun setup() {
        super.setup()

        useCase =
            BrushHeadConditionUseCaseImpl(
                brushHeadRepository,
                replaceBrushHeadWorkerConfigurator,
                toothbrushRepository
            )
    }

    @Test
    fun `resetBrushHead should configure the worker for future launch`() {
        val mac = "mac"
        val serial = "serial"

        whenever(brushHeadRepository.brushHeadInformationOnce(mac))
            .thenReturn(Single.just(brushHeadInfo()))

        whenever(brushHeadRepository.newBrushHeadCompletable(mac)).thenReturn(Completable.complete())

        val resetBrushHeadSingle = useCase.resetBrushHead(mac, serial).test()

        resetBrushHeadSingle.assertComplete()
        verify(replaceBrushHeadWorkerConfigurator).configure(Payload(mac, serial))
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should configure the worker if the local date is newer than the date sent to the back-end`() {
        val (mac, serial, _, _) = updateBrushHeadDateIfNeededHappyPath()

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(replaceBrushHeadWorkerConfigurator).configure(Payload(mac, serial))

        testObserver
            .assertValueCount(1)
            .assertComplete()
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should configure the worker if the local date is not empty and the remote date is null`() {
        val (mac, serial, _, _) = updateBrushHeadDateIfNeededHappyPath()

        whenever(brushHeadRepository.getLastDateSentToApiMaybe(mac)).thenReturn(Maybe.empty())

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(replaceBrushHeadWorkerConfigurator).configure(Payload(mac, serial))

        testObserver
            .assertValueCount(1)
            .assertComplete()
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should not configure the worker if local date does not exists`() {
        val (mac, _, _, _) = updateBrushHeadDateIfNeededHappyPath()

        whenever(brushHeadRepository.hasBrushHeadReplacedDateOnce(mac))
            .thenReturn(Single.just(false))

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(replaceBrushHeadWorkerConfigurator, never()).configure(any())
        testObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should not configure the worker if local date is equal to the last date sent to api`() {
        val (mac, _, localDate) = updateBrushHeadDateIfNeededHappyPath()

        whenever(brushHeadRepository.getLastDateSentToApiMaybe(mac))
            .thenReturn(Maybe.just(localDate))

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(brushHeadRepository, never()).sendReplacedDateToApiCompletable(any(), any(), any())
        testObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should not configure the worker if local date is older to the last date sent to api`() {
        val (mac, _, localDate, _) = updateBrushHeadDateIfNeededHappyPath()

        whenever(brushHeadRepository.getLastDateSentToApiMaybe(mac))
            .thenReturn(Maybe.just(localDate.plusDays(1)))

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(brushHeadRepository, never()).sendReplacedDateToApiCompletable(any(), any(), any())
        testObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should not configure the worker if local date is the same but has more millis as the one sent to api`() {
        val (mac, _, _, _) = updateBrushHeadDateIfNeededHappyPath()

        val localDate = of(2020, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC)

        whenever(brushHeadRepository.brushHeadInformationOnce(mac))
            .thenReturn(Single.just(brushHeadInfo(resetDate = localDate.plusNanos(1))))

        whenever(brushHeadRepository.getLastDateSentToApiMaybe(mac))
            .thenReturn(Maybe.just(localDate))

        val testObserver = useCase.updateBrushHeadDateIfNeeded(mac).test()

        verify(brushHeadRepository, never()).sendReplacedDateToApiCompletable(any(), any(), any())
        testObserver
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `getBrushHeadInformationFromApi dispatch result from the repository`() {
        val mac = "mac"
        val serial = "serial"
        val expectedBrushHeadInformation = mock<BrushHeadInformation>()

        whenever(brushHeadRepository.getBrushHeadInformationFromApi(mac, serial))
            .thenReturn(Single.just(expectedBrushHeadInformation))

        val testObserver = useCase.getBrushHeadInformationFromApi(mac, serial).test()

        testObserver
            .assertValue(expectedBrushHeadInformation)
            .assertComplete()
    }

    @Test
    fun `writeReplacedDate dispatch result from the repository`() {
        val mac = "mac"
        val date = TrustedClock.getNowOffsetDateTime()

        whenever(brushHeadRepository.writeBrushHeadInfo(brushHeadInfo(mac, date)))
            .thenReturn(Completable.complete())

        val testObserver = useCase.writeBrushHeadInfo(brushHeadInfo(mac, date)).test()

        verify(brushHeadRepository).writeBrushHeadInfo(brushHeadInfo(mac, date))

        testObserver
            .assertComplete()
    }

    private fun updateBrushHeadDateIfNeededHappyPath(): HappyPathValues {
        val mac = "mac"
        val serial = "serial"
        val localDate = TrustedClock.getNowOffsetDateTime()
        val apiDate = TrustedClock.getNowOffsetDateTime().minusDays(3)

        whenever(toothbrushRepository.getAccountToothbrush(mac)).thenReturn(
            Maybe.just(AccountToothbrush(mac, "name", ARA, 123, serial = serial))
        )
        whenever(brushHeadRepository.hasBrushHeadReplacedDateOnce(mac)).thenReturn(Single.just(true))
        whenever(brushHeadRepository.brushHeadInformationOnce(mac))
            .thenReturn(Single.just(brushHeadInfo(resetDate = localDate)))
        whenever(brushHeadRepository.getLastDateSentToApiMaybe(mac)).thenReturn(Maybe.just(apiDate))

        return HappyPathValues(mac, serial, localDate, apiDate)
    }

    data class HappyPathValues(
        val mac: String,
        val serial: String,
        val localDate: OffsetDateTime,
        val apiDate: OffsetDateTime
    )

    private fun assertHeadConditionForDuration(expectedCondition: BrushHeadCondition) {
        val mac = "01:01:AA"
        val replacementDate = TrustedClock.getNowOffsetDateTime()
        whenever(brushHeadRepository.brushHeadInformationOnce(mac))
            .thenReturn(Single.just(brushHeadInfo(resetDate = replacementDate)))

        useCase.headCondition(mac).test()
            .assertValue(
                BrushHeadConditionData(
                    condition = expectedCondition,
                    lastReplacementDate = replacementDate.toLocalDate()
                )
            )
    }
}
