/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.OtaUpdateNavigator
import com.kolibree.android.app.ui.ota.OtaUpdateParams
import com.kolibree.android.app.ui.ota.OtaUpdateSharedViewModel
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class StartOtaViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: StartOtaViewModel

    private val navigator: OtaUpdateNavigator = mock()

    private val sharedViewModel: OtaUpdateSharedViewModel = mock()

    private val otaUpdateParams: OtaUpdateParams =
        OtaUpdateParams(false, MAC, ToothbrushModel.CONNECT_B1)

    private val checkOtaUpdatePrerequisitesUseCase: CheckOtaUpdatePrerequisitesUseCase = mock()

    private val otaChecker: OtaChecker = mock()

    private val timeScheduler = TestScheduler()

    private val connection =
        KLTBConnectionBuilder.createAndroidLess().withMac(MAC).withModel(ToothbrushModel.CONNECT_B1)
            .build()

    override fun setup() {
        super.setup()

        val otaForConnection: OtaForConnection = mock()
        whenever(otaForConnection.connection).thenReturn(connection)
        whenever(otaChecker.otaForConnectionsOnce()).thenReturn(Observable.just(otaForConnection))

        viewModel = createViewModel()
    }

    @Test
    fun `is rechargeable brush match the parameters given at the construction`() {
        assertFalse(
            createViewModel(
                OtaUpdateParams(false, MAC, ToothbrushModel.CONNECT_B1)
            ).isRechargeableBrush
        )

        assertTrue(
            createViewModel(
                OtaUpdateParams(
                    false,
                    MAC,
                    ToothbrushModel.CONNECT_E1
                )
            ).isRechargeableBrush
        )
    }

    @Test
    fun `is mandatory update  match the parameters given at the construction`() {
        assertFalse(
            createViewModel(
                OtaUpdateParams(
                    false,
                    MAC,
                    ToothbrushModel.CONNECT_B1
                )
            ).isRechargeableBrush
        )

        assertTrue(
            createViewModel(
                OtaUpdateParams(true, MAC, ToothbrushModel.CONNECT_E1)
            ).isRechargeableBrush
        )
    }

    @Test
    fun `when the user click on upgrade then app sends analytics event`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.never()
        )

        viewModel.onUpgradeClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_StartUpdate_Update"))
    }

    @Test
    fun `when the user click on upgrade it check the prerequisite and navigates to next screen if no errors after minimum time`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.just(
                emptyList()
            )
        )

        viewModel.onUpgradeClick()

        verify(checkOtaUpdatePrerequisitesUseCase).otaUpdateBlockersOnce(connection)
        verify(navigator, never()).navigatesToInProgress()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(navigator).navigatesToInProgress()
    }

    @Test
    fun `when the user click on upgrade it check the prerequisite and show error is there are not satisfy`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.just(
                listOf(OtaUpdateBlocker.CONNECTION_NOT_ACTIVE)
            )
        )

        viewModel.onUpgradeClick()

        verify(checkOtaUpdatePrerequisitesUseCase).otaUpdateBlockersOnce(connection)
        verify(navigator, never()).navigatesToInProgress()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(navigator, never()).navigatesToInProgress()

        verify(sharedViewModel).showError(any())
    }

    @Test
    fun `when the user click on upgrade it show a progress`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.just(
                listOf(OtaUpdateBlocker.CONNECTION_NOT_ACTIVE)
            )
        )

        viewModel.onUpgradeClick()

        verify(sharedViewModel).showProgress(true)
        verify(sharedViewModel, never()).showProgress(false)

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(sharedViewModel).showProgress(true)
    }

    @Test
    fun `when there is a blocker it maps it to an error`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.just(
                listOf(
                    OtaUpdateBlocker.CONNECTION_NOT_ACTIVE,
                    OtaUpdateBlocker.NOT_CHARGING
                )
            )
        )

        viewModel.onUpgradeClick()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(sharedViewModel).showError(Error.from(messageId = R.string.ota_blocker_not_active_connection))
    }

    @Test
    fun `when there is no internet connection it display a specific error`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.error(NetworkNotAvailableException())
        )

        viewModel.onUpgradeClick()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(sharedViewModel).showError(Error.from(messageId = R.string.ota_blocker_no_internet))
    }

    @Test
    fun `when connection is not found it display a specific error`() {
        whenever(checkOtaUpdatePrerequisitesUseCase.otaUpdateBlockersOnce(connection)).thenReturn(
            Single.error(ConnectionNotFoundException(""))
        )

        viewModel.onUpgradeClick()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(sharedViewModel).showError(Error.from(messageId = R.string.ota_blocker_not_active_connection))
    }

    @Test
    fun `when user click on start update it hide potential error`() {
        viewModel.onUpgradeClick()

        verify(sharedViewModel).hideError()
    }

    @Test
    fun `when the user click on cancel then the app sends analytics event`() {
        viewModel.onCancelClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_StartUpdate_Cancel"))
    }

    @Test
    fun `when the user click on cancel it close the screen`() {
        viewModel.onCancelClick()

        verify(navigator).finishScreen()
    }

    private fun createViewModel(params: OtaUpdateParams = otaUpdateParams): StartOtaViewModel =
        StartOtaViewModel(
            sharedViewModel,
            navigator,
            params,
            checkOtaUpdatePrerequisitesUseCase,
            otaChecker,
            timeScheduler
        )
}

private const val MAC = "mac"
