/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
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

/** [SynchronizeBrushingModeUseCaseImpl] tests */
class SynchronizeBrushingModeUseCaseImplTest : BaseUnitTest() {

    private val brushingModeRepository = mock<BrushingModeRepository>()

    private lateinit var useCase: SynchronizeBrushingModeUseCaseImpl

    @Before
    fun before() {
        useCase = spy(SynchronizeBrushingModeUseCaseImpl(brushingModeRepository))
    }

    /*
    isCandidateForSync
     */

    @Test
    fun `isCandidateForSync emits false when toothbrush is in bootloader`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withBootloader(true)
            .build()

        useCase
            .isCandidateForSync(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(false)
    }

    @Test
    fun `isCandidateForSync emits false when brushing mode isn't available`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withoutBrushingMode()
            .build()

        useCase
            .isCandidateForSync(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(false)
    }

    @Test
    fun `isCandidateForSync emits false when brushing mode is available and tb is shared`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withSharedMode()
            .withBrushingMode()
            .build()

        useCase
            .isCandidateForSync(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(false)
    }

    @Test
    fun `isCandidateForSync emits true when brushing mode is available and tb is not shared`() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(2L)
            .withBrushingMode()
            .build()

        useCase
            .isCandidateForSync(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(true)
    }

    /*
    fetchToothbrushProfileBrushingMode
     */

    @Test
    fun `fetchToothbrushProfileBrushingMode creates ProfileBrushingMode from toothbrush data`() {
        val profileId = 1986L
        val toothbrushBrushingMode = BrushingMode.Strong
        val toothbrushLastSync = TrustedClock.getNowOffsetDateTime()
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), toothbrushBrushingMode, toothbrushLastSync)
            .build()

        useCase
            .fetchToothbrushProfileBrushingMode(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(
                ProfileBrushingMode(
                    profileId,
                    toothbrushBrushingMode,
                    toothbrushLastSync
                )
            )
    }

    /*
    fetchDatabaseProfileBrushingMode
     */

    @Test
    fun `fetchDatabaseProfileBrushingMode emits NULL pattern when there is no local data`() {
        val profileId = 1986L
        val connection = KLTBConnectionBuilder.createAndroidLess().withOwnerId(profileId).build()
        whenever(brushingModeRepository.getForProfile(any())).thenReturn(ProfileBrushingMode.NULL)

        useCase
            .fetchDatabaseProfileBrushingMode(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(ProfileBrushingMode.NULL)
    }

    @Test
    fun `fetchDatabaseProfileBrushingMode emits local ProfileBrushingMode from repository`() {
        val profileId = 1983L
        val localData = ProfileBrushingMode(profileId, BrushingMode.Slow, TrustedClock.getNowOffsetDateTime())
        val connection = KLTBConnectionBuilder.createAndroidLess().withOwnerId(profileId).build()
        whenever(brushingModeRepository.getForProfile(profileId)).thenReturn(localData)

        useCase
            .fetchDatabaseProfileBrushingMode(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(localData)
    }

    /*
    syncFromDatabaseToToothbrush
     */

    @Test
    fun `syncFromDatabaseToToothbrush sets toothbrush's new brushing mode`() {
        val brushingMode = BrushingMode.Strong
        val dbState = ProfileBrushingMode(2L, brushingMode, TrustedClock.getNowOffsetDateTime())
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withBrushingMode(listOf(), brushingMode)
            .build()

        useCase
            .syncFromDatabaseToToothbrush(connection, dbState)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()

        verify(connection.brushingMode()).set(brushingMode)
        verify(brushingModeRepository, never()).setForProfile(any(), any())
    }

    /*
    syncFromToothbrushToDatabase
     */

    @Test
    fun `syncFromToothbrushToDatabase `() {
        val profileId = 1983L
        val brushingMode = BrushingMode.Strong
        val tbState =
            ProfileBrushingMode(profileId, brushingMode, TrustedClock.getNowOffsetDateTime())

        useCase
            .syncFromToothbrushToDatabase(tbState)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()

        verify(brushingModeRepository).setForProfile(profileId, brushingMode)
    }

    /*
    chooseSynchronizationCompletable
     */

    @Test
    fun `chooseSynchronizationCompletable directly completes when modes are identical`() {
        val profileId = 1986L
        val mode = BrushingMode.Slow

        val dbState = ProfileBrushingMode(profileId, mode, TrustedClock.getNowOffsetDateTime())
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbState = ProfileBrushingMode(profileId, mode, TrustedClock.getNowOffsetDateTime())
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), mode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase, never()).syncFromToothbrushToDatabase(any())
        verify(useCase, never()).syncFromDatabaseToToothbrush(any(), any())
    }

    @Test
    fun `chooseSynchronizationCompletable chooses syncFromToothbrushToDatabase when there is no local data`() {
        val profileId = 1986L
        val mode = BrushingMode.Slow

        doReturn(Single.just(ProfileBrushingMode.NULL))
            .whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbState = ProfileBrushingMode(profileId, mode, TrustedClock.getNowOffsetDateTime())
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())
        doReturn(Completable.complete()).whenever(useCase).syncFromToothbrushToDatabase(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), mode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase).syncFromToothbrushToDatabase(any())
        verify(useCase, never()).syncFromDatabaseToToothbrush(any(), any())
    }

    @Test
    fun `chooseSynchronizationCompletable calls syncFromDatabaseToToothbrush when local data is newer`() {
        val profileId = 1986L

        val dbMode = BrushingMode.Slow
        val dbState = ProfileBrushingMode(profileId, dbMode, TrustedClock.getNowOffsetDateTime())
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbMode = BrushingMode.Strong
        val tbState = ProfileBrushingMode(
            profileId,
            tbMode,
            TrustedClock.getNowOffsetDateTime().minusDays(3L)
        )
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), tbMode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase, never()).syncFromToothbrushToDatabase(any())
        verify(useCase).syncFromDatabaseToToothbrush(any(), any())
    }

    @Test
    fun `chooseSynchronizationCompletable chooses syncFromToothbrushToDatabase when toothbrush data is newer`() {
        val profileId = 1986L

        val dbMode = BrushingMode.Slow
        val dbState = ProfileBrushingMode(
            profileId,
            dbMode,
            TrustedClock.getNowOffsetDateTime().minusDays(3L)
        )
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbMode = BrushingMode.Strong
        val tbState = ProfileBrushingMode(profileId, tbMode, TrustedClock.getNowOffsetDateTime())
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())
        doReturn(Completable.complete()).whenever(useCase).syncFromToothbrushToDatabase(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), tbMode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase).syncFromToothbrushToDatabase(any())
        verify(useCase, never()).syncFromDatabaseToToothbrush(any(), any())
    }

    // Insure that the DB is synced with the TB again if the DB dateTime is inconsistent with the current device clock
    @Test
    fun `chooseSynchronizationCompletable chooses syncFromToothbrushToDatabase when database dateTime is after current dateTime`() {
        val profileId = 1986L

        val dbMode = BrushingMode.Slow
        val dbDateTime = TrustedClock.getNowOffsetDateTime().plusDays(30)
        val dbState = ProfileBrushingMode(profileId, dbMode, dbDateTime)
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbMode = BrushingMode.Strong
        val tbState = ProfileBrushingMode(
            profileId,
            tbMode,
            TrustedClock.getNowOffsetDateTime().minusDays(3L)
        )
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), tbMode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase).syncFromToothbrushToDatabase(any())
        verify(useCase, never()).syncFromDatabaseToToothbrush(any(), any())
    }

    /*
    This will never happen in real life but we are serious people

    Note: It happened !
     */
    @Test
    fun `chooseSynchronizationCompletable chooses syncFromToothbrushToDatabase when both set at the same time`() {
        val profileId = 1986L
        val lastUpdateTime = TrustedClock.getNowOffsetDateTime()

        val dbMode = BrushingMode.Slow
        val dbState = ProfileBrushingMode(profileId, dbMode, lastUpdateTime)
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        val tbMode = BrushingMode.Strong
        val tbState = ProfileBrushingMode(profileId, tbMode, lastUpdateTime)
        doReturn(Single.just(tbState)).whenever(useCase).fetchToothbrushProfileBrushingMode(any())
        doReturn(Completable.complete()).whenever(useCase).syncFromToothbrushToDatabase(any())

        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode(listOf(), tbMode)
            .build()

        useCase
            .chooseSynchronizationCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase).syncFromToothbrushToDatabase(any())
        verify(useCase, never()).syncFromDatabaseToToothbrush(any(), any())
    }

    /*
    synchronizeBrushingMode
     */

    @Test
    fun `synchronizeBrushingMode does nothing when isCandidateForSync emits false`() {
        doReturn(Single.just(false)).whenever(useCase).isCandidateForSync(any())

        useCase
            .synchronizeBrushingMode(mock())
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase, never()).chooseSynchronizationCompletable(any())
    }

    @Test
    fun `synchronizeBrushingMode calls chooseSynchronizationCompletable when isCandidateForSync emits true`() {
        doReturn(Single.just(true)).whenever(useCase).isCandidateForSync(any())
        doReturn(Completable.complete()).whenever(useCase).chooseSynchronizationCompletable(any())

        useCase
            .synchronizeBrushingMode(mock())
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(useCase).chooseSynchronizationCompletable(any())
    }

    @Test
    fun `syncLocalBrushingModeToToothbrush sync brushing mode from DB to TB`() {
        val profileId = 123L
        val connection = getConnection(profileId)
        val lastUpdateTime = TrustedClock.getNowOffsetDateTime()
        val dbMode = BrushingMode.defaultMode()
        val dbState = ProfileBrushingMode(profileId, dbMode, lastUpdateTime)
        doReturn(Single.just(dbState)).whenever(useCase).fetchDatabaseProfileBrushingMode(any())

        useCase
            .syncLocalBrushingModeToToothbrush(connection)
            .test()
            .assertComplete().assertNoErrors()

        verify(useCase).fetchDatabaseProfileBrushingMode(connection)
        verify(useCase).syncFromDatabaseToToothbrush(connection, dbState)
    }

    @Test
    fun `initBrushingModeForToothbrushAndProfile invokes setDefaultModeForProfile and setDefaultModeForToothBrush`() {
        val profileId = 123L
        val connection = getConnection(profileId)
        useCase
            .initBrushingModeForToothbrushAndProfile(connection)
            .test()
            .assertComplete().assertNoErrors()

        verify(useCase).setDefaultModeForProfile(connection)
        verify(useCase).setDefaultModeForToothBrush(connection)
    }

    @Test
    fun `initBrushingModeForProfile invokes brushingModeRepository setForProfile to save default brushing mode`() {
        val profileId = 123L
        useCase
            .initBrushingModeForProfile(profileId)
            .test()
            .assertComplete().assertNoErrors()

        verify(brushingModeRepository).setForProfile(profileId, BrushingMode.defaultMode())
    }

    @Test
    fun `setDefaultModeForToothBrush save default brushing mode to toothbrush`() {
        val profileId = 123L
        val connection = getConnection(profileId)
        useCase
            .setDefaultModeForToothBrush(connection)
            .test()
            .assertComplete().assertNoErrors()

        verify(connection.brushingMode()).set(BrushingMode.defaultMode())
    }

    @Test
    fun `setDefaultModeForProfile calls initBrushingModeForProfile with profile id `() {
        val profileId = 123L
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .build()

        useCase
            .setDefaultModeForProfile(connection)
            .test()
            .assertComplete().assertNoErrors()

        verify(useCase).initBrushingModeForProfile(profileId)
    }

    private fun getConnection(profileId: Long): KLTBConnection {
        return KLTBConnectionBuilder
            .createAndroidLess()
            .withOwnerId(profileId)
            .withBrushingMode()
            .build()
    }
}
