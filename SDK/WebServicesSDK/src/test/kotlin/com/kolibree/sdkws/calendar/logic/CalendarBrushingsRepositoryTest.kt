/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.calendar.logic.api.BrushingStreaksApi
import com.kolibree.android.calendar.logic.api.model.BrushingStreaksResponse
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.calendar.logic.persistence.BrushingStreaksDao
import com.kolibree.android.calendar.logic.persistence.model.BrushingStreakEntity
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import retrofit2.Response

class CalendarBrushingsRepositoryTest : BaseUnitTest() {

    private val api = mock<BrushingStreaksApi>()

    private val dao = mock<BrushingStreaksDao>()

    private val networkChecker = mock<NetworkChecker>()

    private val brushingsRepository = mock<BrushingsRepository>()

    private lateinit var repository: CalendarBrushingsRepository

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        repository = CalendarBrushingsRepository(api, dao, networkChecker, brushingsRepository, false)
    }

    override fun tearDown() {
        FailEarly.overrideDelegateWith(TestDelegate)
        super.tearDown()
    }

    @Test
    fun `getStreaksForProfile queries only database when there is no internet connection`() {
        val profile = 1234L

        doReturn(emptyList<BrushingStreakEntity>()).whenever(dao).queryByProfile(profile)
        doReturn(Single.never<Response<BrushingStreaksResponse>>()).whenever(api).getStreaksForProfile(profile)
        doReturn(false).whenever(networkChecker).hasConnectivity()

        repository.getStreaksForProfile(profile).test()

        verify(api, times(0)).getStreaksForProfile(profile)
        verify(dao, times(1)).queryByProfile(profile)
    }

    @Test
    fun `getStreaksForProfile queries both database and api when there is an internet connection`() {
        val profile = 1234L
        val today = LocalDate.of(2019, 3, 5)
        val yesterday = today.minusDays(1)

        doReturn(listOf(BrushingStreakEntity.from(profile, yesterday, today))).whenever(dao).queryByProfile(profile)
        doReturn(Single.never<Response<BrushingStreaksResponse>>()).whenever(api).getStreaksForProfile(profile)
        doReturn(true).whenever(networkChecker).hasConnectivity()

        val observer = repository.getStreaksForProfile(profile).test()

        observer.assertValueCount(1)
        observer.assertValue(setOf(BrushingStreak(yesterday, today)))

        verify(api, times(1)).getStreaksForProfile(profile)
        verify(dao, times(1)).queryByProfile(profile)
    }

    @Test
    fun `getOnlineAndOfflineStreaksForProfile returns empty object if db is empty and api returns error`() {
        val profile = 1234L

        doReturn(emptyList<BrushingStreakEntity>()).whenever(dao).queryByProfile(profile)
        doReturn(Single.error<Response<BrushingStreaksResponse>>(RuntimeException("Exception!")))
            .whenever(api).getStreaksForProfile(profile)

        val observer = repository.getOnlineAndOfflineStreaksForProfile(profile).test()

        observer.assertValueCount(1)
        observer.assertValue(emptySet())
        observer.assertError(RuntimeException::class.java)

        verify(api, times(1)).getStreaksForProfile(profile)
        verify(dao, times(1)).queryByProfile(profile)
    }

    @Test
    fun `getOnlineAndOfflineStreaksForProfile doesn't persist any data if api returns error`() {
        val profile = 1234L
        val today = LocalDate.of(2019, 3, 5)
        val yesterday = today.minusDays(1)

        doReturn(listOf(BrushingStreakEntity.from(profile, yesterday, today))).whenever(dao).queryByProfile(profile)
        doReturn(Single.error<Response<BrushingStreaksResponse>>(RuntimeException("Exception!")))
            .whenever(api).getStreaksForProfile(profile)

        val observer = repository.getOnlineAndOfflineStreaksForProfile(profile).test()

        observer.assertValueCount(1)
        observer.assertValue(setOf(BrushingStreak(yesterday, today)))
        observer.assertError(RuntimeException::class.java)

        verify(dao, times(0)).replaceForProfile(profile, emptyList())
    }

    @Test
    fun `getOnlineAndOfflineStreaksForProfile updates local data based on API response`() {
        val profile = 1234L
        val today = "2018-03-20"
        val yesterday = "2018-03-19"
        val twoDaysAgo = "2018-03-18"
        val lastWeek = "2018-03-13"
        val twoWeeksAgo = "2018-03-06"

        doReturn(listOf(BrushingStreakEntity.from(profile, localDateFrom(twoWeeksAgo), localDateFrom(lastWeek))))
            .whenever(dao).queryByProfile(profile)

        doReturn(
            Single.just(
                Response.success(
                    BrushingStreaksResponse.withDates(
                        listOf(
                            listOf(twoWeeksAgo, lastWeek),
                            listOf(twoDaysAgo, yesterday),
                            listOf(yesterday, today)
                        )
                    )
                )
            )
        ).whenever(api).getStreaksForProfile(profile)

        val observer = repository.getOnlineAndOfflineStreaksForProfile(profile).test()

        observer.assertComplete()
        observer.assertValueCount(2)
        observer.assertValueAt(0, setOf(BrushingStreak(localDateFrom(twoWeeksAgo), localDateFrom(lastWeek))))
        observer.assertValueAt(
            1, setOf(
                BrushingStreak(localDateFrom(twoWeeksAgo), localDateFrom(lastWeek)),
                BrushingStreak(localDateFrom(twoDaysAgo), localDateFrom(yesterday)),
                BrushingStreak(localDateFrom(yesterday), localDateFrom(today))
            )
        )

        verify(dao, times(1)).replaceForProfile(
            profile, listOf(
                BrushingStreakEntity.from(profile, localDateFrom(twoWeeksAgo), localDateFrom(lastWeek)),
                BrushingStreakEntity.from(profile, localDateFrom(twoDaysAgo), localDateFrom(yesterday)),
                BrushingStreakEntity.from(profile, localDateFrom(yesterday), localDateFrom(today))
            )
        )
    }

    @Test
    fun `getOnlyOfflineStreaksForProfile fetches profile related streaks from database`() {
        val today = LocalDate.of(2019, 3, 5)
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)
        val lastWeek = today.minusWeeks(1)

        val firstProfile = 1234L
        val secondProfile = 2345L

        val streaksForFirstProfile =
            listOf(BrushingStreakEntity.from(firstProfile, BrushingStreak(lastWeek, twoDaysAgo)))
        val streaksForSecondProfile =
            listOf(BrushingStreakEntity.from(firstProfile, BrushingStreak(twoDaysAgo, yesterday)))

        doReturn(Single.never<Response<BrushingStreaksResponse>>()).whenever(api).getStreaksForProfile(anyLong())
        doReturn(streaksForFirstProfile).whenever(dao).queryByProfile(firstProfile)
        doReturn(streaksForSecondProfile).whenever(dao).queryByProfile(secondProfile)

        run {
            val observer = repository.getOnlyOfflineStreaksForProfile(firstProfile).test()
            observer.assertValue(setOf(BrushingStreak(lastWeek, twoDaysAgo)))
        }
        run {
            val observer = repository.getOnlyOfflineStreaksForProfile(secondProfile).test()
            observer.assertValue(setOf(BrushingStreak(twoDaysAgo, yesterday)))
        }
        verify(api, times(0)).getStreaksForProfile(anyLong())
    }

    @Test
    fun `getSanitizedStreaks handles empty body`() {
        val response = BrushingStreaksResponse(null)

        val streaks = repository.getSanitizedStreaks(response)

        assertEquals(emptySet<Pair<LocalDate, LocalDate>>(), streaks)
    }

    @Test
    fun `getSanitizedStreaks handles empty streak date list`() {
        val response = BrushingStreaksResponse(
            BrushingStreaksResponse.ResponseBody(null)
        )

        val streaks = repository.getSanitizedStreaks(response)

        assertEquals(emptySet<Pair<LocalDate, LocalDate>>(), streaks)
    }

    @Test
    fun `getSanitizedStreaks filters out incorrect dates in response data`() {
        val response = BrushingStreaksResponse.withDates(
            listOf(
                listOf("2018-01-03", "2018-01-04"), // OK
                listOf("????", "2018-01-04"), // no date
                listOf("2018-01-03", null), // null
                listOf("2018-01-03", ""), // no date
                listOf(), // empty list
                null, // null list
                listOf("2018-01-03"), // too few elements
                listOf("2018-01-03", "2018-01-03", "2018-01-03"), // too many elements
                listOf("2018-01-05", "2018-01-03"), // no asc order
                listOf(null, "2018-01-03") // null
            )
        )

        val streaks = repository.getSanitizedStreaks(response)

        assertEquals(
            setOf(BrushingStreak(LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 4))),
            streaks
        )
    }

    @Test
    fun `calculateStreaksFromBrushings constructs streak from brushings done on 2 or more consecutive days`() {
        val startDate = LocalDate.of(2018, 3, 4)
        val streakDates = setOf<LocalDate>(
            startDate,
            startDate.plusDays(1),
            startDate.plusDays(10),
            startDate.plusDays(11),
            startDate.plusDays(12)
        )
        val singleBrushingDays = setOf<LocalDate>(
            startDate.plusDays(2),
            startDate.plusDays(5),
            startDate.plusDays(9),
            startDate.plusDays(13)
        )

        var kolibreeId = 1L
        val buildBrushingForDate: (LocalDate) -> Brushing = {
            BrushingBuilder.create()
                .withKolibreeId(kolibreeId++)
                .withDateTime(it.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime())
                .build()
        }

        val streakBrushings =
            streakDates.map { buildBrushingForDate(it) } + streakDates.map { buildBrushingForDate(it) }

        val nonStreakBrushings = singleBrushingDays.map { buildBrushingForDate(it) }

        val streaks = repository.calculateStreaksFromBrushings(streakBrushings + nonStreakBrushings)

        assertEquals(
            setOf(
                BrushingStreak(startDate, startDate.plusDays(1)),
                BrushingStreak(startDate.plusDays(10), startDate.plusDays(12))
            ),
            streaks
        )
    }

    private fun localDateFrom(date: String): LocalDate = LocalDate.from(DATE_FORMATTER.parse(date))
}
