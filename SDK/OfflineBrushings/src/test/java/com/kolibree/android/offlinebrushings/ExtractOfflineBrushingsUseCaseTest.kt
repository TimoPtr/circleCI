/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableInternal
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ExtractOfflineBrushingsUseCaseTest : BaseUnitTest() {

    private val connector: IKolibreeConnector = mock()
    private val connectionPool: KLTBConnectionPool = mock()
    private val offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer = mock()
    private val lastSyncObservable: LastSyncObservableInternal = mock()

    private var useCase = ExtractOfflineBrushingsUseCaseImpl(
        connector,
        connectionPool,
        createOfflineBrushingsConsumerProvider(),
        lastSyncObservable
    )

    /*
    extractOfflineBrushings
     */
    @Test
    fun `extractOfflineBrushings emits the total number of brushings and flags the item as finished`() {
        spy()
        val expectedNbBrushing = 10

        doReturn(Single.just(expectedNbBrushing)).whenever(useCase)
            .registerOfflineBrushingsConsumer(offlineBrushingConsumer)

        val brushingsSubject = PublishSubject.create<BrushingSyncedResult>()

        whenever(offlineBrushingConsumer.profileSyncedOfflineBrushings())
            .thenReturn(brushingsSubject)

        brushingsSubject.onComplete()

        val firstValue = ExtractionProgress.withBrushingProgress(emptyList(), expectedNbBrushing)
        val lastValue = firstValue.withCompleted()

        useCase.extractOfflineBrushings().test().assertValues(firstValue, lastValue)
    }

    @Test
    fun `extractOfflineBrushings emits updated list of brushing on new brushing synced`() {
        spy()
        val expectedNbBrushing = 10
        val brushing1 = createOfflineBrushingSyncedResult()
        val brushing2 = createOfflineBrushingSyncedResult()

        doReturn(Single.just(expectedNbBrushing)).whenever(useCase)
            .registerOfflineBrushingsConsumer(offlineBrushingConsumer)

        val brushingsSubject = PublishSubject.create<List<BrushingSyncedResult>>()

        doReturn(brushingsSubject).whenever(useCase).getSyncedBrushings(offlineBrushingConsumer)

        val testObserver = useCase.extractOfflineBrushings().test()

        val firstSyncedBrushings = listOf(brushing1)
        brushingsSubject.onNext(firstSyncedBrushings)

        val firstValue =
            ExtractionProgress.withBrushingProgress(firstSyncedBrushings, expectedNbBrushing)

        testObserver.assertValue(firstValue)

        val secondSyncedBrushings = listOf(brushing1, brushing2)
        brushingsSubject.onNext(secondSyncedBrushings)

        val secondValue =
            ExtractionProgress.withBrushingProgress(secondSyncedBrushings, expectedNbBrushing)

        testObserver.assertValues(firstValue, secondValue).assertNotComplete()

        brushingsSubject.onComplete()

        testObserver
            .assertComplete()
            .assertValues(firstValue, secondValue, secondValue.withCompleted())
    }

    @Test
    fun `extractOfflineBrushings completes when no brushings`() {
        spy()
        val expectedNbBrushing = 0

        doReturn(Single.just(expectedNbBrushing)).whenever(useCase)
            .registerOfflineBrushingsConsumer(offlineBrushingConsumer)

        val brushingsSubject =
            PublishSubject.create<List<BrushingSyncedResult>>()

        doReturn(brushingsSubject).whenever(useCase).getSyncedBrushings(offlineBrushingConsumer)

        val testObserver = useCase.extractOfflineBrushings().test()

        brushingsSubject.onComplete()

        testObserver.assertComplete().assertNoValues()
    }

    @Test
    fun `extractOfflineBrushings subscribes to registerOfflineBrushingsConsumer and profileSyncedOfflineBrushings`() {
        spy()

        val registerConsumerSubject = SingleSubject.create<Int>()
        doReturn(registerConsumerSubject).whenever(useCase)
            .registerOfflineBrushingsConsumer(offlineBrushingConsumer)

        val brushingsSubject =
            PublishSubject.create<BrushingSyncedResult>()
        whenever(offlineBrushingConsumer.profileSyncedOfflineBrushings()).thenReturn(
            brushingsSubject
        )

        useCase.extractOfflineBrushings().test()

        assertTrue(registerConsumerSubject.hasObservers())
        assertTrue(brushingsSubject.hasObservers())
    }

    @Test
    fun `extractOfflineBrushings passes a new offlineBrushingConsumer for each invocation`() {
        val subjects = mutableListOf<PublishSubject<BrushingSyncedResult>>()
        val mocks = mutableListOf<MultiConnectionOfflineBrushingConsumer>()
        useCase = ExtractOfflineBrushingsUseCaseImpl(
            connector,
            connectionPool,
            createOfflineBrushingsConsumerProvider {
                val localOfflineBrushingConsumer = mock<MultiConnectionOfflineBrushingConsumer>()

                mocks.add(localOfflineBrushingConsumer)

                val brushingsSubject =
                    PublishSubject.create<BrushingSyncedResult>()
                whenever(localOfflineBrushingConsumer.profileSyncedOfflineBrushings())
                    .thenReturn(brushingsSubject)

                subjects.add(brushingsSubject)

                return@createOfflineBrushingsConsumerProvider localOfflineBrushingConsumer
            },
            lastSyncObservable
        )

        spy()

        doReturn(SingleSubject.create<Int>())
            .whenever(useCase).registerOfflineBrushingsConsumer(any())

        useCase.extractOfflineBrushings().test()

        useCase.extractOfflineBrushings().test()

        assertEquals(2, mocks.size)
        assertEquals(2, subjects.size)

        mocks.forEach {
            verify(useCase).registerOfflineBrushingsConsumer(it)
        }

        subjects.forEach {
            assertTrue(it.hasObservers())
        }
    }

    /*
    getSyncedBrushings
     */
    @Test
    fun `getSyncedBrushings emits empty list if profileSyncedOfflineBrushings without emitting`() {
        spy()

        val brushingsSubject =
            PublishSubject.create<BrushingSyncedResult>()
        whenever(offlineBrushingConsumer.profileSyncedOfflineBrushings()).thenReturn(
            brushingsSubject
        )

        val testObserver = useCase.getSyncedBrushings(offlineBrushingConsumer).test()

        brushingsSubject.onComplete()

        testObserver.assertValue { it.isEmpty() }.assertComplete()
    }

    @Test
    fun `getSyncedBrushings emits updated list of brushingsSynced`() {
        spy()

        val brushing1 = createOfflineBrushingSyncedResult()
        val brushing2 = createOrphanBrushingSyncedResult()

        val brushingsSubject =
            PublishSubject.create<BrushingSyncedResult>()
        whenever(offlineBrushingConsumer.profileSyncedOfflineBrushings()).thenReturn(
            brushingsSubject
        )

        val testObserver = useCase.getSyncedBrushings(offlineBrushingConsumer).test()

        testObserver.assertValueAt(0) { it.isEmpty() }

        brushingsSubject.onNext(brushing1)

        testObserver.assertValueAt(1) { it[0] == brushing1 }

        brushingsSubject.onNext(brushing2)

        testObserver.assertValueAt(2) {
            it[0] == brushing1 && it[1] == brushing2
        }
    }

    /*
    registerOfflineBrushingsConsumer
     */

    @Test
    fun `registerOfflineBrushingsConsumer invokes maybeLoadStoredBrushings with knownConnections from pool`() {
        spy()

        val expectedList = listOf<KLTBConnection>()
        whenever(connectionPool.getKnownConnections()).thenReturn(expectedList)

        doReturn(Single.just(1)).whenever(useCase)
            .maybeLoadStoredBrushings(any(), eq(offlineBrushingConsumer))

        useCase.registerOfflineBrushingsConsumer(offlineBrushingConsumer).test()

        verify(useCase).maybeLoadStoredBrushings(expectedList, offlineBrushingConsumer)
    }

    @Test
    fun `registerOfflineBrushingsConsumer emits 0 after invoking maybeLoadStoredBrushings if no connection`() {
        spy()

        val expectedList = listOf<KLTBConnection>()
        whenever(connectionPool.getKnownConnections()).thenReturn(expectedList)

        doReturn(Single.just(0)).whenever(useCase)
            .maybeLoadStoredBrushings(any(), eq(offlineBrushingConsumer))

        useCase.registerOfflineBrushingsConsumer(offlineBrushingConsumer).test().assertValueCount(1)
            .assertComplete()
    }

    /*
    maybeLoadStoredBrushings
     */

    @Test
    fun `maybeLoadStoredBrushings invokes maybeLoadStoredBrushings on every connection and subscribes to all completables`() {
        spy()

        val connection1 = mock<KLTBConnection>()
        val connection2 = mock<KLTBConnection>()

        val connection1Subject = SingleSubject.create<Int>()
        val connection2Subject = SingleSubject.create<Int>()
        doReturn(connection1Subject).whenever(useCase)
            .maybeLoadStoredBrushingsFromConnection(connection1, offlineBrushingConsumer)
        doReturn(connection2Subject).whenever(useCase)
            .maybeLoadStoredBrushingsFromConnection(connection2, offlineBrushingConsumer)

        useCase.maybeLoadStoredBrushings(listOf(connection1, connection2), offlineBrushingConsumer)
            .test()

        assertTrue(connection1Subject.hasObservers())
        assertTrue(connection2Subject.hasObservers())
    }

    @Test
    fun `maybeLoadStoredBrushings does not emit error if one connection throw an error`() {
        spy()

        val connection1 = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        val connection1Subject = SingleSubject.create<Int>()
        val connection2Subject = SingleSubject.create<Int>()

        val expectedValue = 10

        doReturn(connection1Subject).whenever(useCase)
            .maybeLoadStoredBrushingsFromConnection(connection1, offlineBrushingConsumer)
        doReturn(connection2Subject).whenever(useCase)
            .maybeLoadStoredBrushingsFromConnection(connection2, offlineBrushingConsumer)

        val testObserver = useCase.maybeLoadStoredBrushings(
            listOf(connection1, connection2),
            offlineBrushingConsumer
        )
            .test()

        connection1Subject.onError(IllegalStateException())

        connection2Subject.onSuccess(expectedValue)

        testObserver.assertComplete().assertValue(expectedValue)
    }

    /*
    maybeLoadStoredBrushingsFromConnection
     */

    @Test
    fun `maybeLoadStoredBrushingsFromConnection subscribes to maybeLoadStoredBrushings on connection if it supports offline brushings`() {
        spy()

        val connection = mock<KLTBConnection>()

        val subject = SingleSubject.create<Boolean>()
        doReturn(subject).whenever(useCase)
            .readFromKLTBConnection(connection, offlineBrushingConsumer)

        useCase.maybeLoadStoredBrushingsFromConnection(connection, offlineBrushingConsumer).test()

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `maybeLoadStoredBrushingsFromConnection completes if maybeLoadStoredBrushings emits true`() {
        spy()

        val connection = mock<KLTBConnection>()

        val subject = SingleSubject.create<Boolean>()
        doReturn(subject).whenever(useCase)
            .readFromKLTBConnection(connection, offlineBrushingConsumer)

        val observer =
            useCase.maybeLoadStoredBrushingsFromConnection(connection, offlineBrushingConsumer)
                .test()
                .assertNotComplete()

        subject.onSuccess(true)

        observer.assertComplete()
    }

    @Test
    fun `maybeLoadStoredBrushingsFromConnection completes if maybeLoadStoredBrushings emits false`() {
        spy()

        val connection = mock<KLTBConnection>()

        val subject = SingleSubject.create<Boolean>()
        doReturn(subject).whenever(useCase)
            .readFromKLTBConnection(connection, offlineBrushingConsumer)

        val observer =
            useCase.maybeLoadStoredBrushingsFromConnection(connection, offlineBrushingConsumer)
                .test()
                .assertNotComplete()

        subject.onSuccess(false)

        observer.assertComplete()
    }

    /*
    readFromKLTBConnection
     */

    @Test
    fun `readFromKLTBConnection shouldLoadOfflineBrushings False emits 0`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        doReturn(Single.just(false)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()
            .assertValue(0)
    }

    @Test
    fun `readFromKLTBConnection emits 0 even if shouldLoadOfflineBrushings emits error`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        doReturn(Single.error<Boolean>(TestForcedException())).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()
            .assertValue(0)
    }

    @Test
    fun `readFromKLTBConnection emits 0 even if count offline brushings emits error`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(0)
            .build()

        whenever(connection.brushing().recordCount).thenReturn(Single.error(TestForcedException()))

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()
            .assertValue(0)
    }

    @Test
    fun `readFromKLTBConnection emits 0 when shouldLoadOfflineBrushings true and 0 UnsyncedBrushings`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(0)
            .build()

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()
            .assertValue(0)
    }

    @Test
    fun `readFromKLTBConnection shouldLoadOfflineBrushings True zero UnsynchedBrushings never invokes FetchOfflineBrushings`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(0)
            .build()

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()

        verify(useCase, never()).fetchOfflineBrushings(any(), eq(offlineBrushingConsumer))
    }

    @Test
    fun `readFromKLTBConnection shouldLoadOfflineBrushings True zero UnsynchedBrushings  updatesLastSync`() {
        spy()

        val now = ZonedDateTime.of(LocalDate.MIN, LocalTime.MIDNIGHT, ZoneId.of("UTC"))
        TrustedClock.setFixedDate(now)

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(0)
            .build()

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()

        verify(lastSyncObservable).send(LastSyncDate(connection.mac()!!, now))
    }

    @Test
    fun `readFromKLTBConnection emits 1 when shouldLoadOfflineBrushings True and 1 UnsynchedBrushings`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(1)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test().assertValue(1)
    }

    @Test
    fun `readFromKLTBConnection shouldLoadOfflineBrushings True 1 UnsynchedBrushings invokes FetchOfflineBrushings`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(1)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        doReturn(Single.just(true)).whenever(useCase)
            .shouldLoadOfflineBrushings(connection)

        useCase.readFromKLTBConnection(connection, offlineBrushingConsumer).test()

        verify(useCase).fetchOfflineBrushings(connection, offlineBrushingConsumer)
        verify(lastSyncObservable, never()).send(any())
    }

    /*
    fetchOfflineBrushings
    */

    @Test
    fun `fetchOfflineBrushings invokes pullRecord`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOfflineBrushings(0)
            .build()

        useCase.fetchOfflineBrushings(connection, offlineBrushingConsumer).test()

        verify(connection.brushing()).pullRecords(offlineBrushingConsumer)
    }

    /*
    shouldLoadOfflineBrushings
   */
    @Test
    fun shouldLoadOfflineBrushings_bootloaderTrue_returnsFalse() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withBootloader(true).build()

        useCase.shouldLoadOfflineBrushings(connection).test().assertValue(false)
    }

    @Test
    fun shouldLoadOfflineBrushings_notActive_returnsFalse() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(false)
            .withState(KLTBConnectionState.TERMINATED)
            .build()

        useCase.shouldLoadOfflineBrushings(connection).test().assertValue(false)
    }

    @Test
    fun shouldLoadOfflineBrushings_bootloaderFalse_activeConnection_isMultimode_returnsTrue() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withBootloader(false)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        useCase.shouldLoadOfflineBrushings(connection).test().assertValue(true)
    }

    @Test
    fun shouldLoadOfflineBrushings_bootloaderFalse_multimodeFalse_accountKnowsOwnerId_returnsTrue() {
        val ownerId: Long = 43
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOwnerId(ownerId)
            .withBootloader(false)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        whenever(connector.doesCurrentAccountKnow(ownerId)).thenReturn(true)

        useCase.shouldLoadOfflineBrushings(connection).test().assertValue(true)
    }

    @Test
    fun shouldLoadOfflineBrushings_bootloaderFalse_multimodeFalse_accountDoesNotKnowOwnerId_returnsFalse() {
        val ownerId: Long = 43
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withOwnerId(ownerId)
            .withBootloader(false)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        whenever(connector.doesCurrentAccountKnow(ownerId)).thenReturn(false)

        useCase.shouldLoadOfflineBrushings(connection).test().assertValue(false)
    }

    /*
    utils
     */
    private fun spy() {
        useCase = spy(useCase)
    }

    private fun createOfflineBrushingsConsumerProvider(
        offlineBrushingConsumerFunction: () -> MultiConnectionOfflineBrushingConsumer = { offlineBrushingConsumer }
    ): Provider<MultiConnectionOfflineBrushingConsumer> {
        return Provider {
            offlineBrushingConsumerFunction.invoke()
        }
    }
}

internal fun createExtractionProgress(
    brushingSyncedResult: List<BrushingSyncedResult> = emptyList(),
    totalBrushings: Int = 0
) = ExtractionProgress.withBrushingProgress(brushingSyncedResult, totalBrushings)
