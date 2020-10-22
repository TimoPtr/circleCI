/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws.brushing.persistence.repo

import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.sdkws.brushing.BrushingApiManager
import com.kolibree.sdkws.brushing.DEFAULT_ACCOUNT_ID
import com.kolibree.sdkws.brushing.GAME2
import com.kolibree.sdkws.brushing.GOAL_DURATION
import com.kolibree.sdkws.brushing.NEW_KOLIBREE_ID
import com.kolibree.sdkws.brushing.PROFILE_ID_USER1
import com.kolibree.sdkws.brushing.createBrushing
import com.kolibree.sdkws.brushing.createBrushingData
import com.kolibree.sdkws.brushing.createBrushingInternal
import com.kolibree.sdkws.brushing.createBrushingResponseItem
import com.kolibree.sdkws.brushing.createProfile
import com.kolibree.sdkws.brushing.generateBrushingResponse
import com.kolibree.sdkws.brushing.generateBrushingResponseBetweenDates
import com.kolibree.sdkws.brushing.models.BrushingResponse
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.SingleSubject
import java.util.UUID
import java.util.concurrent.Executors
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.nullable
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import timber.log.Timber

@ExperimentalCoroutinesApi
internal class BrushingsRepositoryImplTest : BaseUnitTest() {

    private val brushingManager = mock<BrushingApiManager>()
    private val profileDatastore = mock<ProfileDatastore>()
    private lateinit var localBrushingsProcessor: LocalBrushingsProcessor
    private val brushingsDatastore: BrushingsDatastore = mock()

    private lateinit var brushingsRepository: BrushingsRepositoryImpl

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun setup() {
        super.setup()
        Dispatchers.setMain(mainThreadSurrogate)
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `addBrushing invokes offlineProcessor onBrushingCreated on subscription`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            whenever(
                brushingManager.createBrushing(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong(),
                    any()
                )
            )
                .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

            val brushingData = createBrushingData()
            val profile = createProfile()

            val unsubscribedSingle =
                brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())

            verify(localBrushingsProcessor, never()).onBrushingCreated(any())

            unsubscribedSingle.test()

            Timber.d("Verifying onBrushingCreated")
            verify(localBrushingsProcessor).onBrushingCreated(any())
        }

    @Test
    fun `addBrushing invokes uploadNewBrushing even if onBrushingCreated crashes`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingCreatedException()

            initDefault()

            whenever(
                brushingManager.createBrushing(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong(),
                    any()
                )
            )
                .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

            val brushingData = createBrushingData()
            val profile = createProfile()

            brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
                .test()

            verify(brushingsRepository).uploadNewBrushing(any(), any(), any())
        }

    @Test
    fun `addBrushing invokes uploadNewBrushing if network is available`() =
        runBlockingTest {
            initDefault()

            whenever(
                brushingManager.createBrushing(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong(),
                    any()
                )
            )
                .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

            val brushingData = createBrushingData()
            val profile = createProfile()

            val uploadBrushingSubject = SingleSubject.create<Brushing>()

            doReturn(uploadBrushingSubject)
                .whenever(brushingsRepository)
                .uploadNewBrushing(any(), any(), any())

            brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
                .test()

            assertTrue(uploadBrushingSubject.hasObservers())
        }

    @Test
    fun `addBrushing invokes uploadNewBrushing if network not is available`() =
        runBlockingTest {
            initDefault()

            whenever(
                brushingManager.createBrushing(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong(),
                    any()
                )
            )
                .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

            val brushingData = createBrushingData()
            val profile = createProfile()

            val uploadBrushingSubject = SingleSubject.create<Brushing>()

            doReturn(uploadBrushingSubject)
                .whenever(brushingsRepository)
                .uploadNewBrushing(any(), any(), any())

            brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
                .test()

            assertTrue(uploadBrushingSubject.hasObservers())
        }

    /*
    DELETE BRUSHING
     */

    @Test
    fun `deleteBrushing invokes offlineProcessor onBrushingDeleted for brushings without brushingId`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val accountId = 12L
            val profileId = 34L
            val brushingId = 0L
            val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

            whenever(brushingsDatastore.deleteByDateTime(brushing.dateTime)).thenReturn(
                Completable.complete()
            )

            brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing).test()

            verify(localBrushingsProcessor).onBrushingRemoved(any())
        }

    @Test
    fun `deleteBrushing invokes deleteBrushing even if onBrushingRemoved throws error when brushing wasn't synchronized`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingRemovedException()

            initDefault()

            val accountId = 12L
            val profileId = 34L
            val brushingId = 0L
            val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

            whenever(brushingsDatastore.deleteByDateTime(brushing.dateTime)).thenReturn(
                Completable.complete()
            )

            brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing).test()

            verify(brushingsDatastore).deleteByDateTime(brushing.dateTime)
        }

    @Test
    fun `deleteBrushing invokes offlineProcessor onBrushingDeleted even if remote request failed`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val accountId = 12L
            val profileId = 34L
            val brushingId = 45L
            val brushing = mockForDeleteBrushing(accountId, profileId, brushingId, false)

            brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing).test()

            verify(localBrushingsProcessor).onBrushingRemoved(any())
        }

    @Test
    fun `deleteBrushing invokes offlineProcessor onBrushingDeleted if remote request succeeded`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val accountId = 12L
            val profileId = 34L
            val brushingId = 45L
            val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

            whenever(localBrushingsProcessor.onBrushingRemoved(any())).thenReturn(Unit)

            brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing).test()

            Timber.d("Verifying if remote request succeeded on %s", localBrushingsProcessor)
            verify(localBrushingsProcessor).onBrushingRemoved(any())
        }

    @Test
    fun `deleteBrushing invokes deleteBrushing even if onBrushingRemoved throws error when brushing was synchronized`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingRemovedException()

            initDefault()

            val accountId = 12L
            val profileId = 34L
            val brushingId = 45L
            val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

            brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing).test()

            verify(brushingManager).deleteBrushing(any(), any(), any())
        }

    /*
    SYNCHRONIZE BRUSHINGS
     */
    @Test
    fun `synchronizeBrushing invokes onBrushingsCreated only for new brushings`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val existingBrushingResponse = BrushingResponse(
                kolibreeId = 1 + NEW_KOLIBREE_ID,
                coins = 0,
                game = GAME2,
                duration = 1,
                datetime = TrustedClock.getNowOffsetDateTime(),
                goalDuration = GOAL_DURATION,
                profileId = PROFILE_ID_USER1,
                idempotencyKey = UUID.randomUUID().toString()
            )
            val newBrushingResponse = createBrushingResponseItem()
            val brushingResponse =
                generateBrushingResponse(
                    PROFILE_ID_USER1,
                    listOf(newBrushingResponse, existingBrushingResponse)
                )

            val existingBrushing =
                brushingResponse.getBrushings()
                    .single { it.kolibreeId == existingBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(existingBrushing)).thenReturn(true)

            val newBrushing = brushingResponse.getBrushings()
                .single { it.kolibreeId == newBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(newBrushing)).thenReturn(false)

            whenever(brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER1))
                .thenReturn(
                    Single.just(emptyList())
                )

            whenever(
                brushingManager.getBrushingsInDateRange(
                    eq(DEFAULT_ACCOUNT_ID),
                    eq(PROFILE_ID_USER1),
                    any(),
                    any(),
                    nullable(Int::class.java)
                )
            ).thenReturn(
                Single.just(brushingResponse)
            )

            brushingsRepository.synchronizeBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1).test()

            argumentCaptor<List<IBrushing>> {
                verify(localBrushingsProcessor).onBrushingsCreated(capture())

                assertEquals(newBrushing.extractBrushing(), firstValue.single())
            }
        }

    @Test
    fun `synchronizeBrushing invokes addBrushingIfDoNotExist even if onBrushingsCreated throws exception`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingsCreatedException()

            initDefault()

            whenever(brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER1)).thenReturn(
                Single.just(emptyList())
            )

            whenever(
                brushingManager.getBrushingsInDateRange(
                    eq(DEFAULT_ACCOUNT_ID),
                    eq(PROFILE_ID_USER1),
                    any(),
                    any(),
                    nullable(Int::class.java)
                )
            ).thenReturn(Single.just(generateBrushingResponse()))

            brushingsRepository.synchronizeBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1).test()

            verify(brushingsDatastore).addBrushingIfDoNotExist(any())
        }

    @Test
    fun `synchronizeBrushing should get brushings from this date to one month before`() =
        runBlockingTest {
            localBrushingsProcessor = mock()

            // We get rid off the 'spy', because it's clashing with TrustedClock static methods
            brushingsRepository = BrushingsRepositoryImpl(
                brushingManager,
                brushingsDatastore,
                profileDatastore,
                localBrushingsProcessor,
                CoroutineScope(Unconfined)
            )

            val currentDate = TrustedClock.getNowZonedDateTime()
                .withYear(2019)
                .withMonth(Month.AUGUST.value)
                .withDayOfMonth(31)

            val pastDate = TrustedClock.getNowZonedDateTime()
                .withYear(2019)
                .withMonth(Month.JULY.value)
                .withDayOfMonth(31)

            TrustedClock.setFixedDate(currentDate)

            whenever(brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER1)).thenReturn(
                Single.just(emptyList())
            )

            whenever(
                brushingManager.getBrushingsInDateRange(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    pastDate.toLocalDate(),
                    currentDate.toLocalDate()
                )
            ).thenReturn(Single.just(mock()))

            brushingsRepository.synchronizeBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1)

            // This verify is a little bit redundant because the mockings we did with the 'whenever'
            // guarantee the successful "Happy path" of `synchronizeBrushing` with the desired dates
            verify(brushingManager).getBrushingsInDateRange(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                pastDate.toLocalDate(),
                currentDate.toLocalDate()
            )
        }

    /*
    FETCH REMOTE BRUSHINGS BETWEEN DATES
     */
    @Test
    fun `fetchRemoteBrushings between dates adds new brushings to data store`() {
        initDefault()

        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(5)

        whenever(
            brushingManager.getBrushingsInDateRange(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                startDate,
                endDate
            )
        ).thenReturn(
            Single.just(
                generateBrushingResponseBetweenDates(
                    startDate = startDate,
                    endDate = endDate
                )
            )
        )

        brushingsRepository.fetchRemoteBrushings(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            startDate,
            endDate
        ).test()

        verify(brushingsDatastore, times(6)).addBrushingIfDoNotExist(any())
    }

    @Test
    fun `fetchRemoteBrushings between dates invokes onBrushingsCreated only for new brushings`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val endDate = TrustedClock.getNowLocalDate()
            val startDate = endDate.minusDays(5)

            val existingBrushingResponse = BrushingResponse(
                kolibreeId = 1 + NEW_KOLIBREE_ID,
                coins = 0,
                game = GAME2,
                duration = 1,
                datetime = endDate.atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime(),
                goalDuration = GOAL_DURATION,
                profileId = PROFILE_ID_USER1,
                idempotencyKey = UUID.randomUUID().toString()
            )
            val newBrushingResponse = createBrushingResponseItem()
            val brushingResponse =
                generateBrushingResponse(
                    PROFILE_ID_USER1,
                    listOf(newBrushingResponse, existingBrushingResponse)
                )

            val existingBrushing =
                brushingResponse.getBrushings()
                    .single { it.kolibreeId == existingBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(existingBrushing)).thenReturn(true)

            val newBrushing = brushingResponse.getBrushings()
                .single { it.kolibreeId == newBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(newBrushing)).thenReturn(false)

            whenever(
                brushingManager.getBrushingsInDateRange(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    startDate,
                    endDate
                )
            ).thenReturn(
                Single.just(brushingResponse)
            )

            brushingsRepository.fetchRemoteBrushings(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                startDate,
                endDate
            ).test()

            argumentCaptor<List<IBrushing>> {
                verify(localBrushingsProcessor).onBrushingsCreated(capture())

                assertEquals(newBrushing.extractBrushing(), firstValue.single())
            }
        }

    @Test
    fun `fetchRemoteBrushings between dates invokes addBrushingIfDoNotExist even if onBrushingsCreated throws exception`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingsCreatedException()

            initDefault()

            val endDate = TrustedClock.getNowLocalDate()
            val startDate = endDate.minusDays(5)

            whenever(
                brushingManager.getBrushingsInDateRange(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    startDate,
                    endDate
                )
            ).thenReturn(
                Single.just(
                    generateBrushingResponseBetweenDates(
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            )

            brushingsRepository.fetchRemoteBrushings(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                startDate,
                endDate
            ).test()

            verify(brushingsDatastore, times(6)).addBrushingIfDoNotExist(any())
        }

    /*
    FETCH REMOTE BRUSHINGS BEFORE CHOSEN BRUSHING
    */
    @Test
    fun `fetchRemoteBrushings before chosen brushing adds new brushings to data store`() {
        initDefault()

        val referenceBrushing = createBrushing(PROFILE_ID_USER1, 2, kolibreeId = 20)
        whenever(
            brushingManager.getBrushingsOlderThanBrushing(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                referenceBrushing,
                limit = 10
            )
        ).thenReturn(Single.just(generateBrushingResponse()))

        brushingsRepository.fetchRemoteBrushings(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            beforeBrushing = referenceBrushing,
            limit = 10
        ).test()

        verify(brushingsDatastore).addBrushingIfDoNotExist(any())
    }

    @Test
    fun `fetchRemoteBrushings before chosen brushing invokes onBrushingsCreated only for new brushings`() =
        runBlockingTest {
            mockStatsOfflineProcessorStandard()

            initDefault()

            val referenceBrushing = createBrushing(PROFILE_ID_USER1, 2, kolibreeId = 20)

            val existingBrushingResponse = BrushingResponse(
                kolibreeId = referenceBrushing.kolibreeId!!,
                coins = 0,
                game = GAME2,
                duration = 1,
                datetime = referenceBrushing.dateTime,
                goalDuration = GOAL_DURATION,
                profileId = PROFILE_ID_USER1,
                idempotencyKey = UUID.randomUUID().toString()
            )
            val newBrushingResponse = createBrushingResponseItem()
            val brushingResponse =
                generateBrushingResponse(
                    PROFILE_ID_USER1,
                    listOf(newBrushingResponse, existingBrushingResponse)
                )

            val existingBrushing =
                brushingResponse.getBrushings()
                    .single { it.kolibreeId == existingBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(existingBrushing)).thenReturn(true)

            val newBrushing = brushingResponse.getBrushings()
                .single { it.kolibreeId == newBrushingResponse.kolibreeId }
            whenever(brushingsDatastore.exists(newBrushing)).thenReturn(false)

            whenever(
                brushingManager.getBrushingsOlderThanBrushing(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    referenceBrushing,
                    limit = 10
                )
            ).thenReturn(
                Single.just(brushingResponse)
            )

            brushingsRepository.fetchRemoteBrushings(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                referenceBrushing,
                limit = 10
            ).test()

            argumentCaptor<List<IBrushing>> {
                verify(localBrushingsProcessor).onBrushingsCreated(capture())

                assertEquals(newBrushing.extractBrushing(), firstValue.single())
            }
        }

    @Test
    fun `fetchRemoteBrushings before chosen brushing invokes addBrushingIfDoNotExist even if onBrushingsCreated throws exception`() =
        runBlockingTest {
            mockStatsOfflineOnBrushingCreatedException()

            initDefault()

            val referenceBrushing = createBrushing(PROFILE_ID_USER1, 2, kolibreeId = 20)

            whenever(
                brushingManager.getBrushingsOlderThanBrushing(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    referenceBrushing,
                    limit = 10
                )
            ).thenReturn(Single.just(generateBrushingResponse()))

            brushingsRepository.fetchRemoteBrushings(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                referenceBrushing,
                limit = 10
            ).test()

            verify(brushingsDatastore).addBrushingIfDoNotExist(any())
        }

    /*
    GET BRUSHINGS
     */
    @Test
    fun `getBrushings does not return duplicates`() {
        initDefault()

        val brushing = createBrushingInternal()
        val profileId = PROFILE_ID_USER1
        whenever(brushingsDatastore.getBrushings(profileId)).thenReturn(
            Single.just(
                listOf(
                    brushing,
                    brushing
                )
            )
        )

        brushingsRepository.getBrushings(profileId).test()
            .assertValue { it == listOf(brushing.extractBrushing()) }
    }

    /*
    BRUSHINGS FLOWABLE
     */
    @Test
    fun `brushingsFlowable does not return duplicates`() {
        initDefault()

        val brushing = createBrushingInternal()
        val profileId = PROFILE_ID_USER1
        val flowableSubject: PublishProcessor<List<BrushingInternal>> = PublishProcessor.create()
        whenever(brushingsDatastore.brushingsFlowable(profileId)).thenReturn(flowableSubject)

        val observer = brushingsRepository.brushingsFlowable(profileId).test()

        observer.assertEmpty()

        flowableSubject.onNext(listOf(brushing, brushing))

        observer.assertValue { it == listOf(brushing.extractBrushing()) }
    }

    /*
    createBrushingFromNonSyncBrushing (pushNonSynchronizedBrushingSessions)
     */

    @Test
    fun `createBrushingFromNonSyncBrushing completes when there is no out of sync data`() {
        initDefault()
        whenever(brushingsDatastore.getNonSynchronizedBrushing(any()))
            .thenReturn(Single.just(emptyList()))

        brushingsRepository.createBrushingFromNonSyncBrushing(1L, 1L)
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(brushingsRepository, never())
            .pushNonSynchronizedBrushingSessions(any(), any(), any())
        verify(brushingsDatastore, never()).clearNonSynchronized(any())
    }

    @Test
    fun `createBrushingFromNonSyncBrushing pushes out of sync data, updates it locally then clears remaining sessions`() {
        initDefault()
        val expectedAccountId = 1986L
        val expectedProfileId = 1983L
        val expectedNonSynchronizedList = listOf<BrushingInternal>(mock())
        val expectedSynchronizedBrushing1 = mock<BrushingInternal>()
        val expectedSynchronizedBrushing2 = mock<BrushingInternal>()

        whenever(brushingsDatastore.getNonSynchronizedBrushing(expectedProfileId))
            .thenReturn(Single.just(expectedNonSynchronizedList))
        whenever(brushingsDatastore.clearNonSynchronized(expectedProfileId))
            .thenReturn(Completable.complete())
        whenever(
            brushingManager.createBrushings(
                expectedAccountId,
                expectedProfileId,
                expectedNonSynchronizedList
            )
        ).thenReturn(
            Single.just(
                createBrushingResponse(
                    listOf(
                        expectedSynchronizedBrushing1,
                        expectedSynchronizedBrushing2
                    ), expectedProfileId
                )
            )
        )

        brushingsRepository.createBrushingFromNonSyncBrushing(expectedAccountId, expectedProfileId)
            .test()
            .assertComplete()
            .assertNoErrors()

        // Brushing sessions have been pushed to the server
        verify(brushingManager).createBrushings(
            expectedAccountId,
            expectedProfileId,
            expectedNonSynchronizedList
        )

        // Each brushing session has been updated locally
        verify(brushingsDatastore, times(2)).updateBrushing(any())

        // Remaining brushing sessions have been cleared
        verify(brushingsDatastore).clearNonSynchronized(expectedProfileId)
    }

    /*
    UTILS
     */

    private fun initDefault() {
        if (!this::localBrushingsProcessor.isInitialized) {
            localBrushingsProcessor = mock()
        }

        brushingsRepository =
            spy(
                BrushingsRepositoryImpl(
                    brushingManager,
                    brushingsDatastore,
                    profileDatastore,
                    localBrushingsProcessor,
                    CoroutineScope(Unconfined)
                )
            )
    }

    private fun mockStatsOfflineProcessorStandard() {
        localBrushingsProcessor = mock {
            onBlocking { onBrushingRemoved(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingCreated(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingsCreated(any()) }.thenAnswer { return@thenAnswer Unit }
        }
    }

    private fun mockStatsOfflineOnBrushingCreatedException() {
        localBrushingsProcessor = mock {
            onBlocking { onBrushingRemoved(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingCreated(any()) }.thenAnswer { throw TestForcedException() }
            onBlocking { onBrushingsCreated(any()) }.thenAnswer { return@thenAnswer Unit }
        }
    }

    private fun mockStatsOfflineOnBrushingsCreatedException() {
        localBrushingsProcessor = mock {
            onBlocking { onBrushingRemoved(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingCreated(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingsCreated(any()) }.thenAnswer { throw TestForcedException() }
        }
    }

    private fun mockStatsOfflineOnBrushingRemovedException() {
        localBrushingsProcessor = mock {
            onBlocking { onBrushingRemoved(any()) }.thenAnswer { throw TestForcedException() }
            onBlocking { onBrushingCreated(any()) }.thenAnswer { return@thenAnswer Unit }
            onBlocking { onBrushingsCreated(any()) }.thenAnswer { return@thenAnswer Unit }
        }
    }

    private fun mockForDeleteBrushing(
        accountId: Long,
        profileId: Long,
        brushingId: Long,
        requestResult: Boolean = true
    ): Brushing {
        val internal =
            createBrushingInternal(profileId = profileId, minusDay = 0L, kolibreeId = brushingId)
        val brushing = internal.extractBrushing()

        whenever(brushingsDatastore.deleteLocally(brushing.dateTime)).thenReturn(Completable.complete())

        whenever(
            brushingManager.deleteBrushing(
                accountId,
                profileId,
                brushingId
            )
        ).thenReturn(Single.just(requestResult))

        return brushing
    }

    private fun createBrushingResponse(
        sessions: List<BrushingInternal>,
        profileId: Long
    ) = BrushingsResponse(
        sessions.map {
            BrushingResponse(
                game = "of",
                duration = 60L,
                datetime = TrustedClock.getNowOffsetDateTime(),
                profileId = profileId,
                coins = 0,
                goalDuration = GOAL_DURATION,
                kolibreeId = 1L,
                idempotencyKey = UUID.randomUUID().toString()
            )
        }
    )
}
