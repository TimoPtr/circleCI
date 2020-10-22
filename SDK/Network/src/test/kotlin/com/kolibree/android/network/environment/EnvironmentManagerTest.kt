/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.network.environment

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock

internal class EnvironmentManagerTest : BaseUnitTest() {
    @Mock
    lateinit var context: Context

    @Mock
    lateinit var defaultEnvironment: DefaultEnvironment

    @Mock
    lateinit var handler: Handler

    lateinit var environmentManager: EnvironmentManagerImpl

    @Test
    fun `setEnvironment returns false if we attempt to set the same environment`() {
        initDefaultManager()

        val environment = Environment.DEV
        doReturn(environment).whenever(environmentManager).endpoint()

        assertFalse(environmentManager.setEnvironment(environment))
    }

    @Test
    fun `setEnvironment returns setEndpoint when we attempt to set a different environment`() {
        initDefaultManager()

        doReturn(Environment.CHINA).whenever(environmentManager).endpoint()

        val environment = Environment.DEV
        doReturn(true).whenever(environmentManager).setEndpoint(environment)

        assertTrue(environmentManager.setEnvironment(environment))

        verify(environmentManager).setEndpoint(environment)
    }

    /*
    SET CUSTOM ENVIRONMENT
     */

    @Test
    fun `setCustomEnvironment returns false if we attempt to set a url in Environment`() {
        initDefaultManager()

        val environment = CustomEnvironment("da")
        doReturn(true).whenever(environmentManager).endpointUrlAlreadyExists(environment)

        assertFalse(environmentManager.setCustomEnvironment(environment))
    }

    @Test
    fun `setCustomEnvironment returns setEndpoint when we set an environment`() {
        initDefaultManager()

        val environment = CustomEnvironment("da")
        doReturn(false).whenever(environmentManager).endpointUrlAlreadyExists(environment)
        doReturn(true).whenever(environmentManager).setEndpoint(environment)

        assertTrue(environmentManager.setCustomEnvironment(environment))

        verify(environmentManager).setEndpoint(environment)
    }

    /*
    ENDPOINT URL ALREADY EXISTS
     */
    @Test
    fun `endpointUrlAlreadyExists returns true if the endpoint url is in Environment`() {
        initDefaultManager()

        Environment.values().filter { it != Environment.CUSTOM }.forEach {
            assertTrue(environmentManager.endpointUrlAlreadyExists(CustomEnvironment(it.url())))
        }
    }

    @Test
    fun `endpointUrlAlreadyExists returns false if the endpoint url is not in Environment`() {
        initDefaultManager()

        assertFalse(environmentManager.endpointUrlAlreadyExists(CustomEnvironment("random url")))
    }

    /*
    ENVIRONMENT
     */
    @Test
    fun `environment returns CUSTOM if url is none of the values in the enum`() {
        initDefaultManager()

        val randomUrl = "random"
        doReturn(CustomEnvironment(randomUrl)).whenever(environmentManager).endpoint()

        assertEquals(Environment.CUSTOM, environmentManager.environment())
    }

    @Test
    fun `environment returns expected Environment if url is one of the values in the enum`() {
        initDefaultManager()

        Environment.values().filter { it != Environment.CUSTOM }.forEach {
            doReturn(it).whenever(environmentManager).endpoint()

            assertEquals(it, environmentManager.environment())
        }
    }

    /*
    ENDPOINT
     */
    @Test
    fun `endpoint returns defaultEnvironment if stored is not different from default`() {
        initDefaultManager()

        val environment = Environment.DEV
        doReturn(environment).whenever(environmentManager).storedEndpoint()
        doReturn(environment).whenever(environmentManager).defaultEndpoint()

        assertEquals(environment, environmentManager.endpoint())
    }

    @Test
    fun `endpoint returns stored environment if stored is different from default and app hasn't been updated`() {
        initDefaultManager()

        val storedEndpoint = Environment.DEV
        doReturn(storedEndpoint).whenever(environmentManager).storedEndpoint()
        doReturn(Environment.STAGING).whenever(environmentManager).defaultEndpoint()

        doReturn(false).whenever(environmentManager).appHasBeenUpdatedSinceEnvironmentSwapped()

        assertEquals(storedEndpoint, environmentManager.endpoint())
    }

    @Test
    fun `endpoint returns default environment if stored is different from default and app has been updated`() {
        initDefaultManager()

        val storedEndpoint = Environment.DEV
        val defaultEndpoint = Environment.STAGING
        doReturn(storedEndpoint).whenever(environmentManager).storedEndpoint()
        doReturn(defaultEndpoint).whenever(environmentManager).defaultEndpoint()

        doReturn(true).whenever(environmentManager).appHasBeenUpdatedSinceEnvironmentSwapped()

        doNothing().whenever(environmentManager).showLogoutMessageAndKillApp(defaultEndpoint, storedEndpoint)

        assertEquals(defaultEndpoint, environmentManager.endpoint())
    }

    @Test
    fun `endpoint invokes showLogoutMessageAndKillApp if stored is different from default and app has been updated`() {
        initDefaultManager()

        val storedEndpoint = Environment.DEV
        val defaultEndpoint = Environment.STAGING
        doReturn(storedEndpoint).whenever(environmentManager).storedEndpoint()
        doReturn(defaultEndpoint).whenever(environmentManager).defaultEndpoint()

        doReturn(true).whenever(environmentManager).appHasBeenUpdatedSinceEnvironmentSwapped()

        doNothing().whenever(environmentManager).showLogoutMessageAndKillApp(defaultEndpoint, storedEndpoint)

        environmentManager.endpoint()

        verify(environmentManager).showLogoutMessageAndKillApp(defaultEndpoint, storedEndpoint)
    }

    @Test
    fun `endpoint returns PRODUCTION if stored is different from default, app has been updated and a NameException is thrown`() {
        initDefaultManager()

        val storedEndpoint = Environment.DEV
        doReturn(storedEndpoint).whenever(environmentManager).storedEndpoint()

        doThrow(PackageManager.NameNotFoundException()).whenever(environmentManager)
            .defaultEndpoint()

        assertEquals(Environment.PRODUCTION, environmentManager.endpoint())
    }

    /*
    SHOW LOGOUT MESSAGE AND KILL APP
     */
    @Test
    fun `showLogoutMessageAndKillApp invokes showLogoutMessage`() {
        initDefaultManager()
        val storedEndpoint = Environment.DEV
        val defaultEndpoint = Environment.STAGING

        doNothing().whenever(environmentManager).showLogoutMessage(defaultEndpoint, storedEndpoint)

        environmentManager.showLogoutMessageAndKillApp(defaultEndpoint, storedEndpoint)

        verify(environmentManager).showLogoutMessage(defaultEndpoint, storedEndpoint)
    }

    @Test
    fun `showLogoutMessageAndKillApp enqueues a Runnable that will execute clearUserDataAndKillApp after a delay`() {
        val expectedDelay = 1L
        initDefaultManager(delay = expectedDelay)

        val storedEndpoint = Environment.DEV
        val defaultEndpoint = Environment.STAGING

        doNothing().whenever(environmentManager).showLogoutMessage(defaultEndpoint, storedEndpoint)

        environmentManager.showLogoutMessageAndKillApp(defaultEndpoint, storedEndpoint)

        val captor = ArgumentCaptor.forClass(Runnable::class.java)

        verify(handler).postDelayed(captor.capture(), eq(expectedDelay))

        doNothing().whenever(environmentManager).clearUserDataAndKillApp()

        captor.value.run()

        verify(environmentManager).clearUserDataAndKillApp()
    }

    /*
    CLEAR USER DATA AND KILL APP
     */
    @Test
    fun `clearUserDataAndKillApp invokes clearApplicationUserData`() {
        val activityManager = mock<ActivityManager>()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager)

        initDefaultManager()

        environmentManager.clearUserDataAndKillApp()

        verify(activityManager).clearApplicationUserData()
    }

    /*
    DEFAULT ENDPOINT
     */
    @Test
    fun `defaultEndpoint returns constructor defaultEnvironment if it's not null`() {
        val expectedEnvironment = Environment.STAGING
        whenever(defaultEnvironment.environment).thenReturn(expectedEnvironment)

        initDefaultManager()

        assertEquals(expectedEnvironment, environmentManager.defaultEndpoint())
    }

    @Test
    fun `defaultEndpoint returns STAGING if defaultEnvironment is null and it's a staging build`() {
        initDefaultManager(_defaultEnvironment = null)

        doReturn(true).whenever(environmentManager).isStagingBuild()

        assertEquals(Environment.STAGING, environmentManager.defaultEndpoint())
    }

    @Test
    fun `defaultEndpoint returns PRODUCTION if defaultEnvironment is null and it's not a staging build`() {
        initDefaultManager(_defaultEnvironment = null)

        doReturn(false).whenever(environmentManager).isStagingBuild()

        assertEquals(Environment.PRODUCTION, environmentManager.defaultEndpoint())
    }

    private fun initDefaultManager(_defaultEnvironment: DefaultEnvironment? = defaultEnvironment, delay: Long = 0L) {
        whenever(context.applicationContext).thenReturn(context)
        environmentManager = spy(EnvironmentManagerImpl(context, _defaultEnvironment, delay, handler))
    }
}
