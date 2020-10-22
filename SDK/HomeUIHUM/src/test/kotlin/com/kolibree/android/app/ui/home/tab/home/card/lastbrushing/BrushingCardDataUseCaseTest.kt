/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime

internal class BrushingCardDataUseCaseTest : BaseUnitTest() {

    lateinit var useCase: BrushingCardDataUseCase

    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val brushingsRepository: BrushingsRepository = mock()
    private val checkupCalculator: CheckupCalculator = mock()

    override fun setup() {
        super.setup()

        useCase = BrushingCardDataUseCase(
            currentProfileProvider = currentProfileProvider,
            brushingsRepository = brushingsRepository,
            checkupCalculator = checkupCalculator
        )
    }

    /*
    mapToBrushingCardData
     */
    @Test
    fun `new user no brushings profile created today`() {
        mockProfileCreationDate(TrustedClock.getNowZonedDateTime())

        val data = useCase.mapToBrushingCardData(emptyList())
        val today = TrustedClock.getNowLocalDate()
        assertEquals(5, data.size)

        assertTrue(data[0].isEmptyDay)
        assertEquals(today, data[0].date)

        assertTrue(data[1].isDashedDay)
        assertEquals(today.minusDays(1), data[1].date)

        assertTrue(data[2].isDashedDay)
        assertEquals(today.minusDays(2), data[2].date)

        assertTrue(data[3].isDashedDay)
        assertEquals(today.minusDays(3), data[3].date)

        assertTrue(data[4].isDashedDay)
        assertEquals(today.minusDays(4), data[4].date)
    }

    @Test
    fun `new user no brushings profile created 2 days ago`() {
        mockProfileCreationDate(TrustedClock.getNowZonedDateTime().minusDays(2))

        val data = useCase.mapToBrushingCardData(emptyList())
        val today = TrustedClock.getNowLocalDate()
        assertEquals(5, data.size)

        assertTrue(data[0].isEmptyDay)
        assertEquals(today, data[0].date)

        assertTrue(data[1].isEmptyDay)
        assertEquals(today.minusDays(1), data[1].date)

        assertTrue(data[2].isEmptyDay)
        assertEquals(today.minusDays(2), data[2].date)

        assertTrue(data[3].isDashedDay)
        assertEquals(today.minusDays(3), data[3].date)

        assertTrue(data[4].isDashedDay)
        assertEquals(today.minusDays(4), data[4].date)
    }

    @Test
    fun `new user no brushings profile created week ago`() {
        mockProfileCreationDate(TrustedClock.getNowZonedDateTime().minusDays(7))

        val data = useCase.mapToBrushingCardData(emptyList())
        val today = TrustedClock.getNowLocalDate()
        assertEquals(8, data.size)

        assertTrue(data[0].isEmptyDay)
        assertEquals(today, data[0].date)

        assertTrue(data[1].isEmptyDay)
        assertEquals(today.minusDays(1), data[1].date)

        assertTrue(data[2].isEmptyDay)
        assertEquals(today.minusDays(2), data[2].date)

        assertTrue(data[3].isEmptyDay)
        assertEquals(today.minusDays(3), data[3].date)

        assertTrue(data[4].isEmptyDay)
        assertEquals(today.minusDays(4), data[4].date)
    }

    @Test
    fun `new user three brushings profile created today`() {
        mockCheckupData()
        mockProfileCreationDate(TrustedClock.getNowZonedDateTime())

        val brushingDate = TrustedClock.getNowOffsetDateTime()
        val brushing = listOf(
            brushing(brushingDate),
            brushing(brushingDate),
            brushing(brushingDate)
        )
        val data = useCase.mapToBrushingCardData(brushing)
        val today = TrustedClock.getNowLocalDate()
        assertEquals(5, data.size)

        assertTrue(data[0].isBrushingDay)
        assertEquals(brushingDate.toLocalDate(), data[0].date)

        assertTrue(data[1].isBrushingDay)
        assertEquals(brushingDate.toLocalDate(), data[1].date)

        assertTrue(data[2].isBrushingDay)
        assertEquals(brushingDate.toLocalDate(), data[2].date)

        assertTrue(data[3].isDashedDay)
        assertEquals(today.minusDays(1), data[3].date)

        assertTrue(data[4].isDashedDay)
        assertEquals(today.minusDays(2), data[4].date)
    }

    @Test
    fun `old user brushing from only last 30 days`() {
        mockCheckupData()
        mockProfileCreationDate(TrustedClock.getNowZonedDateTime().minusDays(50))

        val brushingDate = TrustedClock.getNowOffsetDateTime()
        val brushingDate1 = brushingDate.minusDays(1)
        val brushingDate2 = brushingDate.minusDays(5)
        val brushings = listOf(
            brushing(brushingDate1),
            brushing(brushingDate2),
            brushing(brushingDate2)
        )

        val data = useCase.mapToBrushingCardData(brushings)
        val today = TrustedClock.getNowLocalDate()

        // 30 days but one day with 2 brushings
        assertEquals(31, data.size)

        assertTrue(data[0].isEmptyDay)
        assertEquals(today, data[0].date)

        assertTrue(data[1].isBrushingDay)
        assertEquals(brushingDate1.toLocalDate(), data[1].date)

        assertTrue(data[2].isEmptyDay)
        assertEquals(today.minusDays(2), data[2].date)

        assertTrue(data[3].isEmptyDay)
        assertEquals(today.minusDays(3), data[3].date)

        assertTrue(data[4].isEmptyDay)
        assertEquals(today.minusDays(4), data[4].date)

        assertTrue(data[5].isBrushingDay)
        assertEquals(brushingDate2.toLocalDate(), data[5].date)

        assertTrue(data[6].isBrushingDay)
        assertEquals(brushingDate2.toLocalDate(), data[6].date)
    }

    /*
    shouldAddNextData
     */

    @Test
    fun `shouldAddNextData returns true if current date is after end date`() {
        val current = TrustedClock.getNowLocalDate()
        val end = current.minusDays(1)
        assertTrue(useCase.shouldAddNextData(current, end, 10))
    }

    @Test
    fun `shouldAddNextData returns true if current date is equal end date`() {
        val current = TrustedClock.getNowLocalDate()
        val end = current
        assertTrue(useCase.shouldAddNextData(current, end, 10))
    }

    @Test
    fun `shouldAddNextData returns false if current date is before end date`() {
        val current = TrustedClock.getNowLocalDate()
        val end = current.plusDays(1)
        assertFalse(useCase.shouldAddNextData(current, end, 10))
    }

    @Test
    fun `shouldAddNextData returns true if size is less than min size`() {
        val current = TrustedClock.getNowLocalDate()
        val end = current.plusDays(1)
        assertTrue(useCase.shouldAddNextData(current, end, 0))
    }

    /*
    endDate
     */

    @Test
    fun `endDate is 30 days ago if profile created before`() {
        val today = TrustedClock.getNowZonedDateTime()
        val profileCreation = today.minusDays(40)
        mockProfileCreationDate(profileCreation)
        val expectedEndDate = today.minusDays(29).toLocalDate()
        assertEquals(expectedEndDate, useCase.endDate())
    }

    @Test
    fun `endDate is profile creation date if created less than 30 days ago`() {
        val today = TrustedClock.getNowZonedDateTime()
        val profileCreation = today.minusDays(15)
        mockProfileCreationDate(profileCreation)
        val expectedEndDate = profileCreation.toLocalDate()
        assertEquals(expectedEndDate, useCase.endDate())
    }

    /*
    updateSelection
     */

    @Test
    fun `updateSelection update isSelected on the first item`() {
        val data = listOf(
            cardData(isSelected = false, position = 0),
            cardData(isSelected = false, position = 1),
            cardData(isSelected = false, position = 2)
        )

        val updatedData = useCase.updateSelection(data)
        assertTrue(updatedData[0].isSelected)
        assertEquals(0, updatedData[0].position)

        assertFalse(updatedData[1].isSelected)
        assertEquals(1, updatedData[1].position)

        assertFalse(updatedData[2].isSelected)
        assertEquals(2, updatedData[2].position)
    }

    /*
    dashedBrushingCardData
     */

    @Test
    fun `dashedBrushingCardData returns dashed card data`() {
        val date = TrustedClock.getNowLocalDate()
        val data = useCase.dashedBrushingCardData(date)
        assertEquals("--", data.day)
        assertEquals("--", data.dayOfWeek)
        assertEquals(0f, data.coverage)
        assertEquals(0f, data.durationPercentage)
        assertFalse(data.isManual)
    }

    /*
    isOlderThenProfile
     */

    @Test
    fun `isOlderThenProfile returns true if date is before profile creation date`() {
        val profileCreationDate = TrustedClock.getNowZonedDateTime().minusDays(10)
        mockProfileCreationDate(profileCreationDate)

        val date = profileCreationDate.minusDays(1).toLocalDate()
        assertTrue(useCase.isOlderThenProfile(date))
    }

    @Test
    fun `isOlderThenProfile returns false if date is after profile creation date`() {
        val profileCreationDate = TrustedClock.getNowZonedDateTime().minusDays(10)
        mockProfileCreationDate(profileCreationDate)

        val date = profileCreationDate.toLocalDate()
        assertFalse(useCase.isOlderThenProfile(date))
    }

    @Test
    fun `isOlderThenProfile returns false if date is equal profile creation date`() {
        val profileCreationDate = TrustedClock.getNowZonedDateTime().minusDays(10)
        mockProfileCreationDate(profileCreationDate)

        val date = profileCreationDate.toLocalDate()
        assertFalse(useCase.isOlderThenProfile(date))
    }

    /*
    brushingOnDate
     */

    @Test
    fun `brushingOnDate returns brushing with the same date`() {
        val startDate = TrustedClock.getNowOffsetDateTime()
        val brushings = listOf(
            brushing(startDate.minusDays(10)),
            brushing(startDate.minusDays(5)),
            brushing(startDate.minusDays(4)),
            brushing(startDate.minusDays(4)),
            brushing(startDate.minusDays(4)),
            brushing(startDate.minusDays(3)),
            brushing(startDate.minusDays(1))
        )
        val date = startDate.minusDays(4).toLocalDate()
        val onDate = useCase.brushingOnDate(date, brushings)
        assertEquals(3, onDate.size)
    }

    @Test
    fun `brushingOnDate returns empty list if no brushing on that day`() {
        val startDate = TrustedClock.getNowOffsetDateTime()
        val brushings = listOf(
            brushing(startDate.minusDays(10)),
            brushing(startDate.minusDays(5)),
            brushing(startDate.minusDays(7)),
            brushing(startDate.minusDays(4)),
            brushing(startDate.minusDays(3)),
            brushing(startDate.minusDays(1))
        )
        val date = startDate.minusDays(2).toLocalDate()
        val onDate = useCase.brushingOnDate(date, brushings)
        assertTrue(onDate.isEmpty())
    }

    /*
    withLeadingZero
     */

    @Test
    fun `withLeadingZero adds zero if one-digit number`() {
        assertEquals("01", useCase.withLeadingZero(1))
        assertEquals("02", useCase.withLeadingZero(2))
        assertEquals("03", useCase.withLeadingZero(3))
        assertEquals("04", useCase.withLeadingZero(4))
        assertEquals("05", useCase.withLeadingZero(5))
        assertEquals("06", useCase.withLeadingZero(6))
        assertEquals("07", useCase.withLeadingZero(7))
        assertEquals("08", useCase.withLeadingZero(8))
        assertEquals("09", useCase.withLeadingZero(9))
    }

    @Test
    fun `withLeadingZero does nothing if number has at least two digits`() {
        assertEquals("10", useCase.withLeadingZero(10))
        assertEquals("14", useCase.withLeadingZero(14))
        assertEquals("31", useCase.withLeadingZero(31))
    }

    /*
    takeLastMonthBrushings
     */

    @Test
    fun `takeLastMonthBrushings returns only brushing from last 30 days`() {
        val today = TrustedClock.getNowOffsetDateTime()
        val brushings = listOf(
            brushing(today.minusDays(17)), // 1
            brushing(today.minusDays(13)), // 2
            brushing(today.minusDays(40)),
            brushing(today.minusDays(5)), // 3
            brushing(today.minusDays(1)), // 4
            brushing(today.minusDays(31)),
            brushing(today.minusDays(60)),
            brushing(today.minusDays(3)) // 5
        )
        assertEquals(5, useCase.takeLastMonthBrushings(brushings).size)
    }

    @Test
    fun `takeLastMonthBrushings returns empty list if all brushing older than 30 days`() {
        val today = TrustedClock.getNowOffsetDateTime()
        val brushings = listOf(
            brushing(today.minusDays(50)),
            brushing(today.minusDays(52)),
            brushing(today.minusDays(44)),
            brushing(today.minusDays(67)),
            brushing(today.minusDays(32))
        )
        assertEquals(0, useCase.takeLastMonthBrushings(brushings).size)
    }

    @Test
    fun `takeLastMonthBrushings returns all brushing if all younger than 30 days`() {
        val today = TrustedClock.getNowOffsetDateTime()
        val brushings = listOf(
            brushing(today.minusDays(3)),
            brushing(today.minusDays(2)),
            brushing(today.minusDays(4)),
            brushing(today.minusDays(29)),
            brushing(today.minusDays(17))
        )
        assertEquals(brushings.size, useCase.takeLastMonthBrushings(brushings).size)
    }

    @Test
    fun `reloads data when profile changes`() {
        val profile1 = ProfileBuilder.create().withId(1).build()
        val profile2 = ProfileBuilder.create().withId(2).build()
        val profile3 = ProfileBuilder.create().withId(3).build()

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(profile1, profile2, profile3))

        whenever(brushingsRepository.brushingsFlowable(any()))
            .thenReturn(Flowable.never())

        useCase.load().test()

        for (profile in listOf(profile1, profile2, profile3)) {
            verify(brushingsRepository).brushingsFlowable(profile.id)
        }
    }

    /*
    utils
     */

    private fun mockCheckupData(surfacePercentage: Int = 0) {
        val data = mock<CheckupData>()
        whenever(data.surfacePercentage).thenReturn(surfacePercentage)
        whenever(checkupCalculator.calculateCheckup(any(), any(), any()))
            .thenReturn(data)
    }

    private fun brushing(date: OffsetDateTime) = BrushingBuilder.create()
        .withDateTime(date)
        .build()

    private fun mockProfileCreationDate(profileCreationDate: ZonedDateTime) {
        val profile = ProfileBuilder.create()
            .withCreationDate(profileCreationDate)
            .build()
        whenever(currentProfileProvider.currentProfile()).thenReturn(profile)
    }

    private fun cardData(
        isSelected: Boolean = false,
        position: Int = 0,
        isManual: Boolean = false
    ) = BrushingCardData(
        isManual = isManual,
        coverage = 0f,
        isSelected = isSelected,
        position = position,
        day = "",
        durationPercentage = 0f,
        dayOfWeek = "",
        date = TrustedClock.getNowLocalDate(),
        type = BrushingType.None,
        brushingDate = TrustedClock.getNowOffsetDateTime(),
        durationInSeconds = 0L,
        colorMouthZones = mapOf()
    )
}
