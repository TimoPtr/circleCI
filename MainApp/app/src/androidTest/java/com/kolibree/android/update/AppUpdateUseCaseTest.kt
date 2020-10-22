/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.update

import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.kolibree.android.app.update.AppUpdateUseCase
import com.kolibree.android.app.update.AppUpdateUseCaseImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unfortunately we can't test all the cases since the FakeAppUpdateManager doesn't
 * send the result intent
 */
class AppUpdateUseCaseTest {

    private lateinit var appUpdateManager: FakeAppUpdateManager
    private lateinit var appUpdateUseCase: AppUpdateUseCase

    @Before
    fun setup() {
        appUpdateManager = spy(FakeAppUpdateManager(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        ))
        appUpdateUseCase =
            AppUpdateUseCaseImpl(appUpdateManager)
    }

    @Test
    fun no_update_available_completes() {
        appUpdateManager.setUpdateNotAvailable()

        appUpdateUseCase.checkForUpdateAndMaybeUpdate().test().await().assertComplete()
    }

    @Test
    fun update_available_shows_immediate_update_flow() {
        appUpdateManager.setUpdateAvailable(9999)
        appUpdateManager.setUpdatePriority(5)

        appUpdateUseCase.checkForUpdateAndMaybeUpdate().test().assertNotComplete()

        // Add a small delay for the callback due to the impl of the FakeUpdateManager
        Thread.sleep(100)

        verify(appUpdateManager).startUpdateFlowForResult(any(), any(), any<IntentSenderForResultStarter>(), any())

        assertTrue(appUpdateManager.isImmediateFlowVisible)
    }

    @Test
    fun update_available_restart_app_once_installed() {
        appUpdateManager.setUpdateAvailable(9999)
        appUpdateManager.setUpdatePriority(5)

        appUpdateUseCase.checkForUpdateAndMaybeUpdate().test().assertNotComplete()

        // Add a small delay for the callback due to the impl of the FakeUpdateManager
        Thread.sleep(100)

        verify(appUpdateManager).startUpdateFlowForResult(any(), any(), any<IntentSenderForResultStarter>(), any())

        assertFalse(appUpdateManager.isInstallSplashScreenVisible)

        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()
        appUpdateManager.completeUpdate()

        assertTrue(appUpdateManager.isInstallSplashScreenVisible)
    }
}
