/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.app.job.JobParameters
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BaseDaggerJobServiceTest : BaseUnitTest() {
    private val jobService = TestBaseDaggerJobService()

    /*
    onCreate
     */
    @Test
    fun `onCreate invokes injectSelf and proceeds on success`() {
        jobService.succeedOnInject = true

        safeInvokeOnCreate()

        assertTrue(jobService.injectSelfInvoked)
        assertTrue(jobService.daggerIsReady)
    }

    @Test
    fun `onCreate invokes injectSelf and proceeds on exception thrown`() {
        jobService.succeedOnInject = false

        safeInvokeOnCreate()

        assertTrue(jobService.injectSelfInvoked)
        assertFalse(jobService.daggerIsReady)
    }

    /*
    onStartJob
     */

    @Test
    fun `onStartJob invokes safeAndroidInjection when isDaggerReady returns false`() {
        jobService.daggerIsReady = false

        jobService.onStartJob(mock())

        assertTrue(jobService.injectSelfInvoked)
    }

    @Test
    fun `onStartJob never invokes safeAndroidInjection when isDaggerReady returns true`() {
        jobService.daggerIsReady = true

        jobService.onStartJob(mock())

        assertFalse(jobService.injectSelfInvoked)
    }

    @Test
    fun `onStartJob DIRECTLY returns false when members were never injected`() {
        jobService.daggerIsReady = false
        jobService.succeedOnInject = false

        assertFalse(jobService.onStartJob(mock()))

        assertFalse(jobService.internalOnStartJobInvoked)
    }

    @Test
    fun `onStartJob invokes internalOnStartJobInvoked and returns the value if dagger injection worked`() {
        jobService.succeedOnInject = true
        jobService.internalJobValue = true

        assertTrue(jobService.onStartJob(mock()))

        assertTrue(jobService.internalOnStartJobInvoked)
    }

    /*
    Utils
     */

    private fun safeInvokeOnCreate() {
        try {
            jobService.onCreate()
        } catch (re: RuntimeException) {
            // ignore. Thrown because we invoke a non-stubbed android class
        }
    }
}

private class TestBaseDaggerJobService : BaseDaggerJobService() {
    var isDaggerReadyInvoked: Boolean = false
    var injectSelfInvoked: Boolean = false
    var internalOnStartJobInvoked: Boolean = false

    var daggerIsReady = false
    var internalJobValue = false
    var succeedOnInject = false

    override fun isDaggerReady(): Boolean {
        isDaggerReadyInvoked = true

        return daggerIsReady
    }

    override fun injectSelf() {
        injectSelfInvoked = true

        if (succeedOnInject) {
            daggerIsReady = true
        } else {
            throw RuntimeException()
        }
    }

    override fun internalOnStartJob(params: JobParameters): Boolean {
        internalOnStartJobInvoked = true

        return internalJobValue
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}
