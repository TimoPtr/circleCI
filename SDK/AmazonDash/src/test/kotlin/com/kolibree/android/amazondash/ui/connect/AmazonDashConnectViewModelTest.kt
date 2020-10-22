/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCase
import com.kolibree.android.amazondash.domain.AmazonDashLinkUseCase
import com.kolibree.android.amazondash.domain.AmazonDashSendTokenUseCase
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode.AMAZON_DRS_AUTHENTICATION_FAILED
import com.kolibree.android.network.api.ApiErrorCode.AMAZON_DRS_UNABLE_TO_CONNECT
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AmazonDashConnectViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: AmazonDashConnectViewModel

    private val navigator: AmazonDashConnectNavigator = mock()
    private val linkUseCase: AmazonDashLinkUseCase = mock()
    private val sendUseCase: AmazonDashSendTokenUseCase = mock()
    private val extractTokenUseCase: AmazonDashExtractTokenUseCase = mock()

    @Test
    fun `finishes after dismiss click`() {
        mockViewModel()

        viewModel.onDismissClick()
        verify(navigator).finish()
        verify(eventTracker).sendEvent(AnalyticsEvent("AmazonDash_NotNow"))
    }

    @Test
    fun `shows loading and disables buttons when processing response`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.never())

        viewModel.onNewIntent(mockIntent)

        verify(extractTokenUseCase).extractTokenFrom(mockIntent)

        val viewState = viewModel.getViewState()!!
        assertTrue(viewState.isLoading)
        assertFalse(viewState.confirmationButtonEnabled)
        assertFalse(viewState.dismissButtonEnabled)
    }

    @Test
    fun `sends token if response is valid`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))

        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.complete())

        viewModel.onNewIntent(mockIntent)

        verify(extractTokenUseCase).extractTokenFrom(mockIntent)
        verify(sendUseCase).sendToken(mockToken)
    }

    @Test
    fun `does not send token if response is invalid`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.error(IllegalStateException("error")))

        viewModel.onNewIntent(mockIntent)

        verify(extractTokenUseCase).extractTokenFrom(mockIntent)
        verifyNoMoreInteractions(sendUseCase)
    }

    @Test
    fun `opens link after confirmation`() {
        val mockLink = "www.mock.link/test"
        mockViewModel(link = mockLink)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onConfirmClick()

        verify(navigator).open(mockLink)
        verify(eventTracker).sendEvent(AnalyticsEvent("AmazonDash_Allow"))
        verifyNoMoreInteractions(navigator)
    }

    @Test
    fun `shows success when token sent`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))
        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.complete())

        var viewState = viewModel.getViewState()!!
        assertFalse(viewState.isLoading)
        assertFalse(viewState.isSuccess)

        viewModel.onNewIntent(mockIntent)

        viewState = viewModel.getViewState()!!
        assertFalse(viewState.isLoading)
        assertTrue(viewState.isSuccess)
        verify(eventTracker).sendEvent(AnalyticsEvent("AmazonDash_Congrats"))
        verifyNoMoreInteractions(navigator)
    }

    @Test
    fun `sets result when token sent`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))
        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.complete())

        val testObserver = viewModel.actionsObservable.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.onNewIntent(mockIntent)

        testObserver.assertValue { it is AmazonDashConnectAction.SetResult }
    }

    @Test
    fun `does not show success when token not sent`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))
        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.error(IllegalStateException("test")))

        var viewState = viewModel.getViewState()!!
        assertFalse(viewState.isLoading)
        assertFalse(viewState.isSuccess)

        viewModel.onNewIntent(mockIntent)

        viewState = viewModel.getViewState()!!
        assertFalse(viewState.isLoading)
        assertFalse(viewState.isSuccess)
        verifyNoMoreInteractions(navigator)
    }

    @Test
    fun `closes after confirmation`() {
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))
        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.complete())

        viewModel.onNewIntent(mockIntent)
        viewModel.onConfirmClick()

        verify(navigator).finish()
        verify(eventTracker).sendEvent(AnalyticsEvent("AmazonDash_Congrats_NotNow"))
        verifyNoMoreInteractions(navigator)
    }

    @Test
    fun `shows error when unable to authorize link`() {
        val mockError = "mock_error"
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.error(ApiError(mockError, AMAZON_DRS_AUTHENTICATION_FAILED, "")))

        val testObserver = viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.onNewIntent(mockIntent)
        verifyNoMoreInteractions(sendUseCase)

        testObserver.assertValue(AmazonDashConnectAction.ShowError(Error.from(mockError)))
    }

    @Test
    fun `shows error when sending token fails`() {
        val mockError = "mock_error"
        val mockToken = "mock_token"
        val mockIntent = mockIntent(token = mockToken)
        mockViewModel(link = "www.mock.link/test")

        whenever(extractTokenUseCase.extractTokenFrom(mockIntent))
            .thenReturn(Maybe.just(mockToken))

        whenever(sendUseCase.sendToken(mockToken))
            .thenReturn(Completable.error(ApiError(mockError, AMAZON_DRS_UNABLE_TO_CONNECT, "")))

        val testObserver = viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.onNewIntent(mockIntent)

        verify(sendUseCase).sendToken(mockToken)
        testObserver.assertValue(AmazonDashConnectAction.ShowError(Error.from(mockError)))
    }

    private fun mockViewModel(link: String? = null) {
        whenever(linkUseCase.getLink())
            .thenReturn(Single.just(link.orEmpty()))

        viewModel = AmazonDashConnectViewModel(
            AmazonDashConnectViewState.initial(),
            navigator,
            linkUseCase,
            sendUseCase,
            extractTokenUseCase
        )
    }

    private fun mockIntent(
        action: String = Intent.ACTION_VIEW,
        token: String? = null
    ): Intent {
        val mockUri = mock<Uri>().apply {
            whenever(getQueryParameter("token")).thenReturn(token)
        }

        return mock<Intent>().apply {
            whenever(this.action).thenReturn(action)
            whenever(this.data).thenReturn(mockUri)
        }
    }
}
