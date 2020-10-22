/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity.mvi

import android.content.Context
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.android.parcel.Parcelize
import org.junit.Test

class BaseUnityViewModelTest : BaseUnitTest() {

    private val viewState: TestUnityGameViewState = TestUnityGameViewState(progressVisible = true)
    private val applicationContext: ApplicationContext = mock()

    private val backgroundJobManagers: MutableSet<BackgroundJobManager> = mutableSetOf()

    private lateinit var viewModel: TestBaseUnityViewModel

    override fun setup() {
        super.setup()

        viewModel = TestBaseUnityViewModel(
            viewState = viewState,
            applicationContext = applicationContext,
            backgroundJobManager = backgroundJobManagers
        )
    }

    @Test
    fun `onTearingDownProcess does nothing if background job manager set is empty`() {
        viewModel.onTearingDownProcess()
    }

    @Test
    fun `onTearingDownProcess invokes scheduleJob on every background job manager`() {
        val firstManager = FakeBackgroundJobManager()
        val secondManager = FakeBackgroundJobManager()

        backgroundJobManagers.add(firstManager)
        backgroundJobManagers.add(secondManager)

        assertFalse(firstManager.scheduleInvoked)
        assertFalse(secondManager.scheduleInvoked)

        viewModel.onTearingDownProcess()

        assertTrue(firstManager.scheduleInvoked)
        assertTrue(secondManager.scheduleInvoked)
    }
}

private class TestBaseUnityViewModel(
    viewState: TestUnityGameViewState,
    applicationContext: ApplicationContext,
    backgroundJobManager: Set<BackgroundJobManager>
) : BaseUnityViewModel<TestUnityGameViewState>(
    viewState,
    applicationContext,
    backgroundJobManager
)

@Parcelize
private data class TestUnityGameViewState(override val progressVisible: Boolean) :
    UnityGameViewState

private class FakeBackgroundJobManager : BackgroundJobManager {
    var scheduleInvoked: Boolean = false
    var cancelInvoked: Boolean = false

    override fun scheduleJob(context: Context) {
        scheduleInvoked = true
    }

    override fun cancelJob() {
        cancelInvoked = true
    }
}
