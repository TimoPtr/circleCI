/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.migration.MigrationUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.app.ui.settings.secret.persistence.AppSessionFlags
import com.kolibree.android.app.update.AppUpdateUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManager
import com.kolibree.android.test.extensions.withFixedInstant
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LauncherViewModelTest : BaseUnitTest() {
    private val navigator: LauncherNavigator = mock()
    private val connector: IKolibreeConnector = mock()
    private val memoryManager: MemoryManager = mock()
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase = mock()
    private val sessionFlags: AppSessionFlags = mock()
    private val appUpdateUseCase: AppUpdateUseCase = mock()
    private val migrationUseCase: MigrationUseCase = mock()

    private lateinit var viewModel: LauncherViewModel

    override fun setup() {
        super.setup()

        viewModel = LauncherViewModel(
            navigator = navigator,
            connector = connector,
            memoryManager = memoryManager,
            userExpectsSmilesUseCase = userExpectsSmilesUseCase,
            sessionFlags = sessionFlags,
            appUpdateUseCase = appUpdateUseCase,
            migrationUseCase = migrationUseCase
        )
    }

    @Test
    fun `onResume navigates to home on login success`() {
        onResumeWithLoginResult(isLoginSuccessful = true)

        verify(navigator).openHomeScreen()
    }

    @Test
    fun `onResume navigates to onboarding on login failure`() {
        onResumeWithLoginResult(isLoginSuccessful = false)

        verify(navigator).openOnboarding()
    }

    @Test
    fun `onResume invokes userExpectsSync on login success`() = withFixedInstant {
        onResumeWithLoginResult(isLoginSuccessful = true)

        verify(userExpectsSmilesUseCase).onUserExpectsPoints(TrustedClock.getNowInstant())
    }

    @Test
    fun `onResume invokes getMigrationsCompletable and subscribes to it`() = withFixedInstant {
        val migrationCompletable = Completable.complete()

        onResumeWithLoginResult(isLoginSuccessful = true, migrationCompletable = migrationCompletable)

        verify(migrationUseCase).getMigrationsCompletable()
        migrationCompletable.test()
            .assertSubscribed()
    }

    @Test
    fun `onResume invokes userExpectsSync on login failure`() = withFixedInstant {
        onResumeWithLoginResult(isLoginSuccessful = false)

        verify(userExpectsSmilesUseCase).onUserExpectsPoints(TrustedClock.getNowInstant())
    }

    @Test
    fun `onResume push when there is an update available and never navigates to onboarding`() {
        val testObserver = viewModel.actionsObservable.test()

        onResumeWithLoginResult(isLoginSuccessful = true, isUpdateAvailable = true)

        testObserver.assertValue { it is LauncherActions.OnUpdateNeeded }

        verify(navigator, never()).openOnboarding()
    }

    private fun onResumeWithLoginResult(
        isLoginSuccessful: Boolean,
        isUpdateAvailable: Boolean = false,
        migrationCompletable: Completable = Completable.complete()
    ) {
        val syncSubject = SingleSubject.create<Boolean>()
        whenever(connector.syncAndNotify()).thenReturn(syncSubject)

        val upperJawSubject = CompletableSubject.create()
        whenever(memoryManager.preloadFromAssets(Kolibree3DModel.HUM_UPPER_JAW))
            .thenReturn(upperJawSubject)

        val lowerJawSubject = CompletableSubject.create()
        whenever(memoryManager.preloadFromAssets(Kolibree3DModel.HUM_LOWER_JAW))
            .thenReturn(lowerJawSubject)

        if (isUpdateAvailable) {
            whenever(appUpdateUseCase.checkForUpdateAndMaybeUpdate()).thenReturn(Maybe.just(mock()))
        } else {
            whenever(appUpdateUseCase.checkForUpdateAndMaybeUpdate()).thenReturn(Maybe.empty())
        }

        whenever(migrationUseCase.getMigrationsCompletable()).thenReturn(migrationCompletable)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(syncSubject.hasObservers())
        syncSubject.onSuccess(isLoginSuccessful)

        assertTrue(upperJawSubject.hasObservers())
        upperJawSubject.onComplete()

        assertTrue(lowerJawSubject.hasObservers())
        lowerJawSubject.onComplete()
    }
}
