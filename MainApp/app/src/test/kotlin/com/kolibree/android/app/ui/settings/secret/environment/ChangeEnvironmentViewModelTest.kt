/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.environment

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.view.View
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.settings.secret.SecretSettingsNavigator
import com.kolibree.android.network.environment.CustomCredentialsManager
import com.kolibree.android.network.environment.CustomEnvironment
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.environment.EnvironmentManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class ChangeEnvironmentViewModelTest : BaseUnitTest() {

    private val environmentManager: EnvironmentManager = mock()
    private val customCredentialsManager: CustomCredentialsManager = mock()
    private val secretSettingsNavigator: SecretSettingsNavigator = mock()
    private val jobScheduler: JobScheduler = mock()
    private val clearUserContentJobInfo: JobInfo = mock()
    private lateinit var initialViewState: ChangeEnvironmentViewState
    private lateinit var viewModel: ChangeEnvironmentViewModel

    @Before
    fun setUp() {
        whenever(environmentManager.environment()).thenReturn(Environment.DEV)
        initialViewState = ChangeEnvironmentViewState(Environment.DEV)
        viewModel = ChangeEnvironmentViewModel(
            initialViewState,
            environmentManager,
            customCredentialsManager,
            secretSettingsNavigator,
            jobScheduler,
            clearUserContentJobInfo
        )
    }

    @Test
    fun `customEnvironmentVisibility is GONE when selectedEnvironment is not Custom`() {
        Environment.values().filter { it != Environment.CUSTOM }.forEach {
            viewModel.updateViewState { copy(selectedEnvironment = it) }
            viewModel.customEnvironmentVisibility.test().assertValue(View.GONE)
        }
    }

    @Test
    fun `customEnvironmentVisibility is VISIBLE when selectedEnvironment is Custom`() {
        viewModel.updateViewState { copy(selectedEnvironment = Environment.CUSTOM) }

        viewModel.customEnvironmentVisibility.test().assertValue(View.VISIBLE)
    }

    @Test
    fun `onSelectedEnvironment sets current env`() {
        viewModel.actionsObservable.test()
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)

        assertEquals(Environment.PRODUCTION, viewModel.getViewState()?.selectedEnvironment)
    }

    @Test
    fun `onSelectedEnvironment does not notify when the env did not changed`() {
        viewModel.actionsObservable.test()
        val testObserver = viewModel.environmentPosition.test()
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)

        testObserver.assertHistorySize(2)
    }

    @Test
    fun `onSelectedEnvironment does notify when the env changed and not custom env`() {
        val testObserver = viewModel.actionsObservable.test()
        viewModel.onSelectedEnvironment(Environment.PRODUCTION)

        testObserver.assertValue(ShowConfirmChangeEnvironmentAction(Environment.PRODUCTION))
    }

    @Test
    fun `onSelectedEnvironment does notify when the env changed and custom env`() {
        val testObserver = viewModel.actionsObservable.test()
        viewModel.onSelectedEnvironment(Environment.CUSTOM)

        testObserver.assertNoValues()
    }

    @Test
    fun `onSetCustomEnvironment emits ShowCustomEnvironmentMissingFieldErrorAction`() {
        val actionObservable = viewModel.actionsObservable.test()
        actionObservable.assertEmpty()

        viewModel.onSetCustomEnvironment()
        actionObservable.assertValue(ShowCustomEnvironmentMissingFieldErrorAction)
    }

    @Test
    fun `onSetCustomEnvironment emits ShowCustomEnvironmentUrlExistsAction when customEndpointUrl exists`() {
        val actionObservable = viewModel.actionsObservable.test()
        actionObservable.assertEmpty()

        whenever(environmentManager.endpointUrlAlreadyExists(any())).thenReturn(true)
        viewModel.updateViewState {
            copy(
                customEndpointUrl = Environment.DEV.url(),
                customClientId = "test client id",
                customClientSecret = "test client secret"
            )
        }

        viewModel.onSetCustomEnvironment()
        actionObservable.assertValue(ShowCustomEnvironmentUrlExistsAction)
    }

    @Test
    fun `onSetCustomEnvironment emits showConfirmChangeEnvironmentAction when custom fields are valid`() {
        val actionObservable = viewModel.actionsObservable.test()
        actionObservable.assertEmpty()

        whenever(environmentManager.endpointUrlAlreadyExists(any())).thenReturn(false)
        viewModel.updateViewState {
            copy(
                customEndpointUrl = "https://test.endpoint.url",
                customClientId = "test client id",
                customClientSecret = "test client secret"
            )
        }

        viewModel.onSetCustomEnvironment()
        actionObservable.assertValue(ShowConfirmChangeEnvironmentAction(Environment.CUSTOM))
    }

    @Test
    fun `onChangeEnvironmentConfirmed sets predefined endpoint when environment is not custom`() {
        val envs = Environment.values().filter { it != Environment.CUSTOM }
        envs.forEach {
            viewModel.onChangeEnvironmentConfirmed(it)
            verify(environmentManager).setEnvironment(it)
        }
        verifyOnEnvironmentSuccessfullyChanged(envs.size)
    }

    @Test
    fun `onChangeEnvironmentConfirmed sets custom endpoint when selected environment is custom`() {
        val testEndpointUrl = "https://test.endpoint.url"
        val testClientId = "test client id"
        val testClientSecret = "test client secret"
        viewModel.updateViewState {
            copy(
                customEndpointUrl = testEndpointUrl,
                customClientId = testClientId,
                customClientSecret = testClientSecret
            )
        }
        whenever(environmentManager.setCustomEnvironment(any())).thenReturn(true)
        viewModel.onChangeEnvironmentConfirmed(Environment.CUSTOM)

        verify(environmentManager).setCustomEnvironment(CustomEnvironment(testEndpointUrl))
        verify(customCredentialsManager).setCustomCredentials(testClientId, testClientSecret)
        verifyOnEnvironmentSuccessfullyChanged()
    }

    private fun verifyOnEnvironmentSuccessfullyChanged(times: Int = 1) {
        verify(jobScheduler, times(times)).schedule(any())
        verify(secretSettingsNavigator, times(times)).navigateToStartScreen()
    }

    @Test
    fun `onChangeEnvironmentCancelled resets selectedEnvironment to initialEnvironment`() {
        viewModel.updateViewState { copy(selectedEnvironment = Environment.PRODUCTION) }

        viewModel.onChangeEnvironmentCancelled()

        assertEquals(Environment.DEV, viewModel.getViewState()?.selectedEnvironment)
    }
}
