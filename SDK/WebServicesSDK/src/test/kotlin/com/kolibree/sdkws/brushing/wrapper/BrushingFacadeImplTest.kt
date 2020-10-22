package com.kolibree.sdkws.brushing.wrapper

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness.LEFT_HANDED
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.exception.NoExistingBrushingException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class BrushingFacadeImplTest {

    private lateinit var brushingFacade: BrushingFacadeImpl

    private val brushingsRepository = mock<BrushingsRepository>()
    private val kolibreeConnector = mock<InternalKolibreeConnector>()
    private val checkupCalculator = mock<CheckupCalculator>()

    private val brushing =
        Brushing(
            duration,
            goalDuration,
            timestamp,
            12,
            12,
            processedData,
            profileId
        )

    @Before
    fun setUp() {
        brushingFacade =
            spy(BrushingFacadeImpl(brushingsRepository, kolibreeConnector, checkupCalculator))
        whenever(brushingFacade.currentAccountId()).thenReturn(accountId)
        whenever(kolibreeConnector.accountId).thenReturn(accountId)
    }

    @Test
    fun verifyAddBrushing() {
        whenever(brushingsRepository.addBrushing(any(), any(), any())).thenReturn(
            Single.just(
                brushing
            )
        )
        brushingFacade.addBrushing(brushing, profile).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(brushing)
    }

    @Test
    fun verifyGetBrushings() {
        whenever(brushingsRepository.getBrushings(any())).thenReturn(Single.just(listOf(brushing)))
        brushingFacade.getBrushings(profileId).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(listOf(brushing))
    }

    @Test
    fun `Given a valid brushing When deleting a brushing then the kolibreeId is provided`() {

        val expected = Brushing(
            duration = brushing.duration,
            goalDuration = brushing.goalDuration,
            dateTime = brushing.dateTime,
            processedData = brushing.processedData,
            game = brushing.game,
            kolibreeId = brushing.kolibreeId,
            points = 0,
            profileId = profileId,
            coins = 0
        )

        whenever(
            brushingsRepository.deleteBrushing(
                any(),
                any(),
                any()
            )
        ).thenReturn(Completable.complete())
        brushingFacade.deleteBrushing(brushing).ambWith {

            verify(brushingsRepository).deleteBrushing(profileId, accountId, expected)
        }
    }

    @Test
    fun verifyGetBrushingsSince() {
        whenever(brushingsRepository.getBrushingsSince(any(), any())).thenReturn(
            Single.just(
                listOf(
                    brushing
                )
            )
        )
        brushingFacade.getBrushingsSince(TrustedClock.getNowOffsetDateTime(), profileId).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(listOf(brushing))
    }

    @Test
    fun verifyGetBrushingsBetween() {
        whenever(brushingsRepository.getBrushingsBetween(any(), any(), any())).thenReturn(
            Single.just(
                listOf(
                    brushing
                )
            )
        )
        brushingFacade.getBrushingsBetween(
            TrustedClock.getNowOffsetDateTime(), TrustedClock.getNowOffsetDateTime(), profileId
        ).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(listOf(brushing))
    }

    @Test
    fun verifyGetLastBrushingSessionWithExistingBrushing() {
        whenever(brushingsRepository.getLastBrushingSession(any())).thenReturn(brushing)
        brushingFacade.getLastBrushingSession(profileId).test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(brushing)
    }

    @Test
    fun verifyGetLastBrushingSessionWithNoBrushing() {
        whenever(brushingsRepository.getLastBrushingSession(any())).thenReturn(null)
        brushingFacade.getLastBrushingSession(profileId).test()
            .assertError(NoExistingBrushingException(profileId))
    }

    /*
    synchronizedBrushingSessions
     */

    @Test
    fun `synchronizedBrushingSessions emits empty list on synchronization error`() {
        val beginDate = TrustedClock.getNowOffsetDateTime()
        val endDate = TrustedClock.getNowOffsetDateTime()
        val expectedList = listOf<Brushing>()

        whenever(brushingsRepository.fetchRemoteBrushings(any(), any(), any(), any(), anyOrNull()))
            .thenReturn(Single.error(NetworkNotAvailableException()))
        whenever(brushingsRepository.getBrushingsBetween(any(), any(), any()))
            .thenReturn(Single.just(expectedList))

        brushingFacade
            .synchronizedBrushingSessions(profileId, beginDate, endDate)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(expectedList)

        verify(brushingsRepository, never()).getBrushingsBetween(beginDate, endDate, profileId)
    }

    @Test
    fun `synchronizedBrushingSessions emits synchronized data`() {
        val beginDate = TrustedClock.getNowOffsetDateTime()
        val endDate = TrustedClock.getNowOffsetDateTime()
        val expectedBeginLocalDate = beginDate.toLocalDate()
        val expectedEndLocalDate = endDate.toLocalDate()
        val expectedList = listOf<Brushing>()

        whenever(brushingsRepository.fetchRemoteBrushings(any(), any(), any(), any(), anyOrNull()))
            .thenReturn(Single.just(Unit))
        whenever(brushingsRepository.getBrushingsBetween(any(), any(), any()))
            .thenReturn(Single.just(expectedList))

        brushingFacade
            .synchronizedBrushingSessions(profileId, beginDate, endDate)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(expectedList)

        verify(brushingsRepository).getBrushingsBetween(beginDate, endDate, profileId)
        verify(brushingsRepository).fetchRemoteBrushings(
            accountId,
            profileId,
            expectedBeginLocalDate,
            expectedEndLocalDate
        )
    }

    /*
    getBrushingSessions
     */

    @Test
    fun `getBrushingSessions no local data, with remote data, emits synchronized data`() {
        val expectedBrushingId1 = 1986L
        val expectedBrushingId2 = 1983L

        val remoteData = listOf(
            createBrushing(expectedBrushingId1),
            createBrushing(expectedBrushingId2)
        )

        val expectedData = remoteData.map { it as IBrushing }

        whenever(brushingsRepository.fetchRemoteBrushings(any(), any(), any(), any(), anyOrNull()))
            .thenReturn(Single.just(Unit))
        whenever(brushingsRepository.getBrushingsBetween(any(), any(), any()))
            .thenReturn(Single.just(emptyList()))
        doReturn(Single.just(remoteData))
            .whenever(brushingFacade)
            .synchronizedBrushingSessions(any(), any(), any())

        brushingFacade
            .getBrushingSessions(TrustedClock.getNowOffsetDateTime(), TrustedClock.getNowOffsetDateTime(), profileId)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(expectedData)
    }

    @Test
    fun `getBrushingSessions with local data, with remote data, emits local then synchronized data`() {
        val expectedBrushingId1 = 1986L
        val expectedBrushingId2 = 1983L
        val expectedBrushingId3 = 1982L
        val expectedBrushingId4 = 1989L

        val localData = listOf(
            createBrushing(expectedBrushingId1),
            createBrushing(expectedBrushingId2)
        )

        val remoteData = listOf(
            createBrushing(expectedBrushingId2),
            createBrushing(expectedBrushingId3),
            createBrushing(expectedBrushingId4)
        )

        val expectedData = remoteData.map { it as IBrushing }

        whenever(brushingsRepository.fetchRemoteBrushings(any(), any(), any(), any(), anyOrNull()))
            .thenReturn(Single.just(Unit))
        whenever(brushingsRepository.getBrushingsBetween(any(), any(), any()))
            .thenReturn(Single.just(localData))
        doReturn(Single.just(remoteData))
            .whenever(brushingFacade)
            .synchronizedBrushingSessions(any(), any(), any())

        brushingFacade
            .getBrushingSessions(TrustedClock.getNowOffsetDateTime(), TrustedClock.getNowOffsetDateTime(), profileId)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(2)
            .assertValueAt(0, localData)
            .assertLastValue(expectedData)
    }

    /*
    Utils
     */

    private fun createBrushing(id: Long) = Brushing(
        duration = 1986L,
        goalDuration = 120,
        dateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(19831982L), ZoneOffset.UTC),
        coins = 0,
        points = 0,
        processedData = null,
        profileId = profileId,
        kolibreeId = id,
        game = "co",
        toothbrushMac = "mac mac mac"
    )

    companion object {
        private const val accountId = 41L
        private const val profileId = 42L
        private const val duration = 132L
        private const val goalDuration = 220
        private val timestamp = OffsetDateTime.ofInstant(Instant.ofEpochSecond(123123123L), ZoneOffset.UTC)
        private const val processedData: String = "processed_data"
        val profile = Profile(
            brushingGoalTime = 120,
            id = 42,
            firstName = "John",
            gender = Gender.MALE,
            handedness = LEFT_HANDED,
            coachTransitionSounds = true,
            deletable = false,
            points = 0,
            exactBirthdate = false,
            age = 42,
            createdDate = "1990-02-02T12:00:00+0000",
            birthday = DateConvertersString().getLocalDateFromString("1990-02-04")
        )
    }
}
