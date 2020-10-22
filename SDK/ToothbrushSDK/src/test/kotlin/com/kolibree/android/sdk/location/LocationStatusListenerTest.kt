/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.location

import android.content.Context
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.NoAction
import com.kolibree.android.location.RequestPermission
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test

class LocationStatusListenerTest {

    private val context: Context = mock()

    private val locationStatus: LocationStatus = mock()

    private lateinit var locationStatusListener: LocationStatusListenerImpl

    @Before
    fun setup() {
        whenever(context.applicationContext).thenReturn(context)

        locationStatusListener = LocationStatusListenerImpl(context, locationStatus)
    }

    @Test
    fun `subscriptors dispose nullifies locationActionStream when all subscribers dispose`() {
        whenever(context.registerReceiver(any(), any())).thenReturn(null)

        whenever(locationStatus.getLocationAction()).thenReturn(RequestPermission)

        assertNull(locationStatusListener.locationActionStream)

        val observer1 = locationStatusListener.locationActionStream().test()
        val observer2 = locationStatusListener.locationActionStream().test()

        assertNotNull(locationStatusListener.locationActionStream)

        observer1.dispose()

        assertNotNull(locationStatusListener.locationActionStream)

        observer2.dispose()

        assertNull(locationStatusListener.locationActionStream)
    }

    @Test
    fun `multiple subscriptors receive same observable instance`() {
        assertEquals(locationStatusListener.locationActionStream(), locationStatusListener.locationActionStream())
    }

    @Test
    fun `multiple subscriptors only register receiver once`() {
        whenever(context.registerReceiver(any(), any())).thenReturn(null)

        whenever(locationStatus.getLocationAction()).thenReturn(NoAction)

        locationStatusListener.locationActionStream().test()

        verify(context).registerReceiver(any(), any())

        locationStatusListener.locationActionStream().test()

        verify(context, times(1)).registerReceiver(any(), any())
    }

    /*
    ON RECEIVE
     */

    @Test
    fun `status changed emits action from locationStatus`() {
        val expectedAction = NoAction
        whenever(locationStatus.getLocationAction()).thenReturn(expectedAction)

        val observer = locationStatusListener.locationActionStream().test()

        observer.assertEmpty()

        locationStatusListener.locationReceiver.onReceive(context, mock())

        observer.assertValues(expectedAction)
    }
}
