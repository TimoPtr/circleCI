/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import android.content.Context
import android.content.pm.PackageManager
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository
import com.kolibree.android.offlinebrushings.sync.LastSyncData
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableInternal
import com.kolibree.android.offlinebrushings.sync.StartSync
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.LegacyProcessedBrushingFactory
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.BrushingBuilder.DEFAULT_PROFILE_ID
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.android.test.mocks.createOrphanBrushing
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.threeten.bp.OffsetDateTime

internal class MultiConnectionOfflineBrushingConsumerTest : BaseUnitTest() {
    private val lastSyncObservable: LastSyncObservableInternal = mock()
    private val context: Context = mock<Context>().apply {
        whenever(this.applicationContext).thenReturn(this)
    }
    private val connector: IKolibreeConnector = mock()
    private val orphanBrushingRepository: OrphanBrushingRepository = mock()
    private val synchronizator: Synchronizator = mock()

    /*
    Orphan brushing data mapper
     */
    private var processedBrushingFactory: LegacyProcessedBrushingFactory = mock()
    private var checkupCalculator: CheckupCalculator = mock()

    private var offlineBrushingConsumer = MultiConnectionOfflineBrushingConsumer(
        context,
        lastSyncObservable,
        connector,
        orphanBrushingRepository,
        createOrphanBrushingDataMapper(),
        synchronizator
    )

    /*
    onNewOfflineBrushing
     */

    @Test
    fun onNewOfflineBrushing_multiModeTrue_invokesCreateBrushingWithExpectedMapper() {
        spy()

        val macAddress = "mac"
        val serial = "serial"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withMac(macAddress)
            .withSerialNumber(serial)
            .build()

        doNothing().whenever(offlineBrushingConsumer).createBrushing(any())

        val processedBrushing = mock<OfflineBrushing>()
        offlineBrushingConsumer.onNewOfflineBrushing(connection, processedBrushing, 0)

        argumentCaptor<OfflineBrushingsDataMapper> {
            verify(offlineBrushingConsumer).createBrushing(capture())

            val mapper = firstValue

            assertNotNull(mapper)

            assertEquals(processedBrushing, mapper.offlineBrushing)
        }
    }

    @Test
    fun onNewOfflineBrushing_multiModeFalse_invokesCreateBrushingWithExpectedMapper() {
        spy()

        val macAddress = "mac"
        val serial = "serial"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(macAddress)
            .withSerialNumber(serial)
            .withOwnerId(5)
            .build()

        doNothing().whenever(offlineBrushingConsumer).createBrushing(any())

        val processedBrushing = mock<OfflineBrushing>()
        offlineBrushingConsumer.onNewOfflineBrushing(connection, processedBrushing, 0)

        argumentCaptor<OfflineBrushingsDataMapper> {
            verify(offlineBrushingConsumer).createBrushing(capture())

            val mapper = firstValue

            assertNotNull(mapper)

            assertEquals(processedBrushing, mapper.offlineBrushing)
        }
    }

    /*
    CREATE BRUSHING
    */
    @Test
    fun `createBrushing containsOrphanBrushing Returns False creates RemoteOfflineBrushing`() {
        spy()

        val expectedUserId = 54L
        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.ownerId()).thenReturn(expectedUserId)
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(false)
        whenever(offlineMapper.datetime()).thenReturn(TrustedClock.getNowOffsetDateTime())
        whenever(offlineMapper.toothbrushMac).thenReturn(DEFAULT_MAC)

        val profileWrapper = mock<ProfileWrapper>()
        whenever(connector.withProfileId(expectedUserId)).thenReturn(profileWrapper)

        val mockData = mock<CreateBrushingData>()
        doReturn(mockData).whenever(offlineMapper).createBrushingData(any())

        whenever(context.packageManager).thenAnswer { throw PackageManager.NameNotFoundException() }

        offlineBrushingConsumer.createBrushing(offlineMapper)

        verify(profileWrapper).createBrushingSync(mockData)
    }

    @Test
    fun `createBrushing containsOrphanBrushing Returns False invokes createOfflineBrushing`() {
        spy()

        val expectedUserId = 54L
        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.ownerId()).thenReturn(expectedUserId)
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(false)
        whenever(offlineMapper.toothbrushMac).thenReturn(DEFAULT_MAC)
        val expectedDateTime = TrustedClock.getNowOffsetDateTime()
        whenever(offlineMapper.datetime()).thenReturn(expectedDateTime)
        val mac = DEFAULT_MAC
        whenever(offlineMapper.toothbrushMac).thenReturn(mac)

        val profileWrapper = mock<ProfileWrapper>()
        whenever(connector.withProfileId(expectedUserId)).thenReturn(profileWrapper)

        val mockData = mock<CreateBrushingData>()
        doReturn(mockData).whenever(offlineMapper).createBrushingData(any())

        whenever(context.packageManager).thenAnswer { throw PackageManager.NameNotFoundException() }

        offlineBrushingConsumer.createBrushing(offlineMapper)

        verify(offlineBrushingConsumer).createOfflineBrushing(offlineMapper)
    }

    @Test
    fun `createBrushing containsOrphanBrushing Returns False emits OrphanBrushingSyncedResult`() {
        spy()

        val expectedUserId = 54L
        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.ownerId()).thenReturn(expectedUserId)
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(false)
        val expectedDateTime = TrustedClock.getNowOffsetDateTime()
        whenever(offlineMapper.datetime()).thenReturn(expectedDateTime)
        val mac = DEFAULT_MAC
        whenever(offlineMapper.toothbrushMac).thenReturn(mac)

        val profileWrapper = mock<ProfileWrapper>()
        whenever(connector.withProfileId(expectedUserId)).thenReturn(profileWrapper)

        val mockData = mock<CreateBrushingData>()
        doReturn(mockData).whenever(offlineMapper).createBrushingData(any())

        whenever(context.packageManager).thenAnswer { throw PackageManager.NameNotFoundException() }

        val testObserver = offlineBrushingConsumer.profileSyncedOfflineBrushings().test()

        offlineBrushingConsumer.createBrushing(offlineMapper)

        testObserver.assertValue {
            it is OfflineBrushingSyncedResult
        }.assertValue { it.dateTime == expectedDateTime }
            .assertValue { it.mac == mac }
            .assertValue { it.profileId == expectedUserId }
    }

    @Test
    fun `createBrushing containsOrphanBrushing Returns True createsLocalOrphanBrushing`() {
        spy()

        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(true)
        whenever(offlineMapper.toothbrushMac).thenReturn(DEFAULT_MAC)
        whenever(offlineMapper.ownerId()).thenReturn(DEFAULT_PROFILE_ID)
        whenever(offlineMapper.datetime()).thenReturn(TrustedClock.getNowOffsetDateTime())

        val orphanBrushing = mock<OrphanBrushing>()
        whenever(offlineMapper.createOrphanBrushing()).thenReturn(orphanBrushing)

        offlineBrushingConsumer.createBrushing(offlineMapper)

        verify(orphanBrushingRepository).insert(orphanBrushing)
    }

    @Test
    fun `createBrushing containsOrphanBrushing Returns True invokes createOrphanBrushing`() {
        spy()

        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(true)
        val expectedDateTime = TrustedClock.getNowOffsetDateTime()
        whenever(offlineMapper.datetime()).thenReturn(expectedDateTime)
        val mac = DEFAULT_MAC
        whenever(offlineMapper.toothbrushMac).thenReturn(mac)

        val orphanBrushing = createOrphanBrushing()
        whenever(offlineMapper.createOrphanBrushing()).thenReturn(orphanBrushing)

        offlineBrushingConsumer.createBrushing(offlineMapper)

        verify(offlineBrushingConsumer).createOrphanBrushing(offlineMapper)
    }

    @Test
    fun `createBrushing containsOrphanBrushing Returns True emits OrphanBrushingSyncedResult`() {
        spy()

        val offlineMapper = mock<OfflineBrushingsDataMapper>()
        whenever(offlineMapper.containsOrphanBrushing()).thenReturn(true)
        val expectedDateTime = TrustedClock.getNowOffsetDateTime()
        whenever(offlineMapper.datetime()).thenReturn(expectedDateTime)
        val mac = DEFAULT_MAC
        whenever(offlineMapper.toothbrushMac).thenReturn(mac)
        val expectedOwnerId = 42L
        whenever(offlineMapper.ownerId()).thenReturn(expectedOwnerId)

        val orphanBrushing = createOrphanBrushing()
        whenever(offlineMapper.createOrphanBrushing()).thenReturn(orphanBrushing)

        val testObserver = offlineBrushingConsumer.profileSyncedOfflineBrushings().test()

        offlineBrushingConsumer.createBrushing(offlineMapper)

        testObserver.assertValue {
            it is OrphanBrushingSyncedResult
        }.assertValue { it.dateTime == expectedDateTime }
            .assertValue { it.mac == mac }
            .assertValue { it.profileId == expectedOwnerId }
    }

    /*
    ON SUCCESS
   */

    @Test
    fun `onSuccess invokes sendLastSync Date independently of the number of records`() {
        spy()

        val activeProfileId = 2L
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withOwnerId(activeProfileId).build()

        offlineBrushingConsumer.onSuccess(connection, 1)

        verify(lastSyncObservable).send(any())

        offlineBrushingConsumer.onSuccess(connection, 0)

        verify(lastSyncObservable, times(2)).send(any())
    }

    /*
    ON SYNC START
   */

    @Test
    fun onSyncStart_invokes_synchronizator_pause() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.onSyncStart(connection)

        verify(synchronizator).standBy()
    }

    @Test
    fun `onSyncStart increments expectedSyncs`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(0, offlineBrushingConsumer.expectedSyncs)

        offlineBrushingConsumer.onSyncStart(connection)

        assertEquals(1, offlineBrushingConsumer.expectedSyncs)

        offlineBrushingConsumer.onSyncStart(connection)

        assertEquals(2, offlineBrushingConsumer.expectedSyncs)
    }

    @Test
    fun `onSyncStart invokes lastSyncObservable send`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.onSyncStart(connection)

        argumentCaptor<LastSyncData> {
            verify(lastSyncObservable).send(capture())

            assertEquals(StartSync(connection.toothbrush().mac), firstValue)
        }
    }

    /*
    ON SYNC END
   */

    /*
    If we don't do this, consumers can invoke .synchronize() on a Synchronizator with canSynchronize=false
     */
    @Test
    fun `onSyncEnd invokes synchronizator resume before completeIfNoSyncsPending`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.onSyncEnd(connection)

        inOrder(synchronizator, offlineBrushingConsumer) {
            verify(synchronizator).resume()

            verify(offlineBrushingConsumer).completeIfNoSyncsPending()
        }
    }

    @Test
    fun `onSyncEnd invokes lastSyncObservable send`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        TrustedClock.setFixedDate()

        offlineBrushingConsumer.onSyncEnd(connection)

        argumentCaptor<LastSyncData> {
            verify(lastSyncObservable).send(capture())

            assertEquals(LastSyncDate.now(connection.toothbrush().mac), firstValue)
        }
    }

    @Test
    fun `onSyncEnd invokes completeIfNoSyncsPending`() {
        spy()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.onSyncEnd(connection)

        verify(offlineBrushingConsumer).completeIfNoSyncsPending()
    }

    @Test
    fun `onSyncEnd decrements expectedSyncs`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.expectedSyncs = 2

        offlineBrushingConsumer.onSyncEnd(connection)

        assertEquals(1, offlineBrushingConsumer.expectedSyncs)

        offlineBrushingConsumer.onSyncEnd(connection)

        assertEquals(0, offlineBrushingConsumer.expectedSyncs)
    }

    /*
    onFailure
     */
    @Test
    fun `onFailure invokes completeIfNoSyncsPending`() {
        spy()
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        offlineBrushingConsumer.onFailure(connection, mock())

        verify(offlineBrushingConsumer).completeIfNoSyncsPending()
    }

    /*
    completeIfNoSyncsPending
     */
    @Test
    fun `completeIfNoSyncsPending does nothing if expectedSyncs is greater than 0`() {
        val observer = offlineBrushingConsumer.profileSyncedOfflineBrushings().test()
            .assertNotComplete()

        offlineBrushingConsumer.expectedSyncs = 2

        offlineBrushingConsumer.completeIfNoSyncsPending()

        observer.assertNotComplete()
    }

    @Test
    fun `completeIfNoSyncsPending completes flowable if expectedSyncs is 0`() {
        val observer = offlineBrushingConsumer.profileSyncedOfflineBrushings().test()
            .assertNotComplete()

        offlineBrushingConsumer.expectedSyncs = 0

        offlineBrushingConsumer.completeIfNoSyncsPending()

        observer.assertComplete()
    }

    /*
    Utils
     */

    private fun spy() {
        offlineBrushingConsumer = spy(offlineBrushingConsumer)
    }

    private fun createOrphanBrushingDataMapper(): Provider<OfflineBrushingsDataMapper.Builder> {
        val builder = OfflineBrushingsDataMapper.Builder(
            connector, processedBrushingFactory, checkupCalculator
        )

        return Provider { builder }
    }
}

internal fun createOfflineBrushingSyncedResult(
    profileId: Long = DEFAULT_PROFILE_ID,
    mac: String = DEFAULT_MAC,
    time: OffsetDateTime = TrustedClock.getNowOffsetDateTime()
): BrushingSyncedResult =
    OfflineBrushingSyncedResult(profileId, mac, time)

internal fun createOrphanBrushingSyncedResult(
    profileId: Long = DEFAULT_PROFILE_ID,
    mac: String = DEFAULT_MAC,
    time: OffsetDateTime = TrustedClock.getNowOffsetDateTime()
): BrushingSyncedResult =
    OrphanBrushingSyncedResult(profileId, mac, time)
