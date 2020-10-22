/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.BluetoothDisabled
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.ConnectionAllowed
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationPermissionNotGranted
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationServiceDisabled
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.extensions.assertLastValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CheckConnectionPrerequisitesUseCase] unit tests */
internal class CheckConnectionPrerequisitesUseCaseImplTest : BaseUnitTest() {

    private val bluetoothUtils: IBluetoothUtils = mock()

    private val locationStatus: LocationStatus = mock()

    private val timeScheduler = TestScheduler()

    private val useCase = CheckConnectionPrerequisitesUseCaseImpl(
        bluetoothUtils,
        locationStatus,
        timeScheduler
    )

    /*
    checkOnceAndStream
     */

    @Test
    fun `checkOnceAndStream emits BluetoothDisabled right after subscription if bluetooth is off`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        checkOnceAndStreamObservable().assertValue(BluetoothDisabled).assertNotComplete()
    }

    @Test
    fun `checkOnceAndStream returns LocationServiceDisabled right after subscription when location service is disabled`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        checkOnceAndStreamObservable().assertValue(LocationServiceDisabled).assertNotComplete()
    }

    @Test
    fun `checkOnceAndStream returns LocationPermissionNotGranted right after subscription when location permission is not granted and location service is disabled`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(true)

        checkOnceAndStreamObservable().assertValue(LocationPermissionNotGranted)
            .assertNotComplete()
    }

    @Test
    fun `checkOnceAndStream returns ConnectionAllowed right after subscription when all the prerequisites are satisfied`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        checkOnceAndStreamObservable().assertValue(ConnectionAllowed).assertNotComplete()
    }

    @Test
    fun `checkOnceAndStream returns BluetoothDisabled after CHECK_PREREQUISITES_INTERVAL if bluetooth turns off`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true, false)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        val observer = checkOnceAndStreamObservable().assertValueCount(1)

        timeScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

        observer.assertValueCount(1)

        // now we expect the check
        timeScheduler.advanceTimeBy(600, TimeUnit.MILLISECONDS)

        observer.assertValueCount(2)
        observer.assertLastValue(BluetoothDisabled)
    }

    @Test
    fun `checkOnceAndStream returns multiple times the same item`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        val observer = checkOnceAndStreamObservable().assertValueCount(1).assertNotComplete()

        timeScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        observer.assertValueCount(31).assertNotComplete()
    }

    @Test
    fun `checkOnceAndStream returns LocationPermissionNotGranted if user revokes permission during the subscription`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false, true)

        val observer =
            checkOnceAndStreamObservable().assertValue(ConnectionAllowed).assertNotComplete()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertValueCount(2).assertLastValue(LocationPermissionNotGranted)
    }

    @Test
    fun `checkOnceAndStream returns LocationServiceDisabled if user revokes permission during the subscription`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false, true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        val observer =
            checkOnceAndStreamObservable().assertValue(ConnectionAllowed).assertNotComplete()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertValueCount(2).assertLastValue(LocationServiceDisabled)
    }

    @Test
    fun `checkOnceAndStream returns ConnectionAllowed when prerequisites are met`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false, true, false)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        val observer = checkOnceAndStreamObservable().assertValueCount(1).assertNotComplete()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertValueCount(2).assertValueCount(2)

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertValueCount(3).assertLastValue(ConnectionAllowed)
    }

    /*
    check
     */

    @Test
    fun `check returns BluetoothDisabled when bluetooth is off`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        assertEquals(
            BluetoothDisabled,
            useCase.checkConnectionPrerequisites()
        )
    }

    @Test
    fun `check returns LocationServiceDisabled when location service is disabled`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        assertEquals(
            LocationServiceDisabled,
            useCase.checkConnectionPrerequisites()
        )
    }

    @Test
    fun `check returns LocationPermissionNotGranted when location permission is not granted and location service is disabled`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(true)

        assertEquals(
            LocationPermissionNotGranted,
            useCase.checkConnectionPrerequisites()
        )
    }

    @Test
    fun `check returns ConnectionAllowed when all the prerequisites are satisfied`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false)
        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        assertEquals(
            ConnectionAllowed,
            useCase.checkConnectionPrerequisites()
        )
    }

    @Test
    fun `check prioritizes bluetooth over location`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(true)

        assertEquals(
            BluetoothDisabled,
            useCase.checkConnectionPrerequisites()
        )
    }

    @Test
    fun `check prioritizes LocationPermission over location disabled`() {
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)
        whenever(locationStatus.shouldAskPermission()).thenReturn(true)

        assertEquals(
            LocationPermissionNotGranted,
            useCase.checkConnectionPrerequisites()
        )
    }

    /*
    Utils
     */

    private fun checkOnceAndStreamObservable() = useCase.checkOnceAndStream().test()
        .also { timeScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS) }
}
