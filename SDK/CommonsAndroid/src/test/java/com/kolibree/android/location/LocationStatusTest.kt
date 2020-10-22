
/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.location

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class LocationStatusTest {
    private val context: Context = mock()

    private lateinit var locationEnabledChecker: LocationStatusImpl

    @Before
    fun setup() {
        whenever(context.applicationContext).thenReturn(context)

        locationEnabledChecker = spy(LocationStatusImpl(context))
    }

    /*
    GET ENABLE LOCATION ACTION
     */
    @Test
    fun `getLocationAction returns NoLocationAction if askForPermission and shouldEnableLocation return false`() {
        doReturn(false).whenever(locationEnabledChecker).shouldEnableLocation()
        doReturn(false).whenever(locationEnabledChecker).shouldAskPermission()

        val action = locationEnabledChecker.getLocationAction()

        assertTrue(action is NoAction)

        assertFalse(action.requestEnableLocation)
        assertFalse(action.requestPermission)
    }

    @Test
    fun `getLocationAction returns RequestPermission if askForPermission and shouldEnableLocation return true`() {
        doReturn(true).whenever(locationEnabledChecker).shouldEnableLocation()
        doReturn(true).whenever(locationEnabledChecker).shouldAskPermission()

        val action = locationEnabledChecker.getLocationAction()

        assertTrue(action is RequestPermission)

        assertTrue(action.requestPermission)
        assertFalse(action.requestEnableLocation)
    }

    @Test
    fun `getLocationAction returns RequestPermission if askForPermission returns true and shouldEnableLocation returns false`() {
        doReturn(false).whenever(locationEnabledChecker).shouldEnableLocation()
        doReturn(true).whenever(locationEnabledChecker).shouldAskPermission()

        val action = locationEnabledChecker.getLocationAction()

        assertTrue(action is RequestPermission)

        assertTrue(action.requestPermission)
        assertFalse(action.requestEnableLocation)
    }

    @Test
    fun `getLocationAction returns EnableLocation if askForPermission returns false and shouldEnableLocation returns true`() {
        doReturn(true).whenever(locationEnabledChecker).shouldEnableLocation()
        doReturn(false).whenever(locationEnabledChecker).shouldAskPermission()

        val action = locationEnabledChecker.getLocationAction()

        assertEquals(EnableLocation, action)

        assertFalse(action.requestPermission)
        assertTrue(action.requestEnableLocation)
    }

    /*
    SHOULD ENABLE LOCATION
     */
    @Test
    fun `shouldEnableLocation returns false for android version below M`() {
        doReturn(Build.VERSION_CODES.M - 1).whenever(locationEnabledChecker).currentAndroidVersion()

        assertFalse(locationEnabledChecker.shouldEnableLocation())
    }

    @Test
    fun `shouldEnableLocation checks LocationManager in Android P and above`() {
        doReturn(Build.VERSION_CODES.P).whenever(locationEnabledChecker).currentAndroidVersion()

        val locationManager = mock<LocationManager>()
        whenever(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)

        whenever(locationManager.isLocationEnabled).thenReturn(true)

        // since location is enabled, we don't need to enable it
        assertFalse(locationEnabledChecker.shouldEnableLocation())

        verify(locationManager).isLocationEnabled
    }

    @Test
    fun `shouldEnableLocation returns true if LOCATION_MODE_OFF and is below Android P and Android M`() {
        doReturn(Build.VERSION_CODES.M).whenever(locationEnabledChecker).currentAndroidVersion()

        @Suppress("DEPRECATION")
        doReturn(Settings.Secure.LOCATION_MODE_OFF).whenever(locationEnabledChecker).getLocationMode()

        assertTrue(locationEnabledChecker.shouldEnableLocation())
    }

    @Test
    fun `shouldEnableLocation returns false if LOCATION_MODE_HIGH_ACCURACY and is below Android P and Android M`() {
        doReturn(Build.VERSION_CODES.M).whenever(locationEnabledChecker).currentAndroidVersion()

        @Suppress("DEPRECATION")
        doReturn(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY).whenever(locationEnabledChecker).getLocationMode()

        assertFalse(locationEnabledChecker.shouldEnableLocation())
    }
}
