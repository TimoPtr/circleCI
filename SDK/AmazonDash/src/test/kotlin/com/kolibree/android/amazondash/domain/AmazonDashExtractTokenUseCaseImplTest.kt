/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import android.content.Intent
import android.net.Uri
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.data.model.AmazonDashException
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCaseImpl.Companion.RESPONSE_ERROR_KEY
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCaseImpl.Companion.RESPONSE_STATE_KEY
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCaseImpl.Companion.RESPONSE_TOKEN_KEY
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class AmazonDashExtractTokenUseCaseImplTest : BaseUnitTest() {

    private val verifyStateUseCase: AmazonDashVerifyStateUseCase = mock()

    private lateinit var useCase: AmazonDashExtractTokenUseCase

    override fun setup() {
        super.setup()
        useCase = AmazonDashExtractTokenUseCaseImpl(verifyStateUseCase)
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @Test
    fun `returns empty if intent does not contain uri`() {
        val testIntent = mockIntent()

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        testObserver.assertValueCount(0)
        testObserver.assertComplete()
    }

    @Test
    fun `returns empty if intent has wrong action`() {
        val testUri = mockUri()
        val testIntent = mockIntent(action = Intent.ACTION_ANSWER, uri = testUri)

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        testObserver.assertValueCount(0)
        testObserver.assertComplete()
    }

    @Test
    fun `returns fail if link is not valid`() {
        val testState = "test_state"
        val testUri = mockUri(state = testState)
        val testIntent = mockIntent(uri = testUri)

        whenever(verifyStateUseCase.verifyAndClear(testState))
            .thenReturn(Single.just(false))

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        verify(verifyStateUseCase).verifyAndClear(testState)

        testObserver.assertError(AmazonDashException(R.string.amazon_dash_connect_error_invalid_link))
    }

    @Test
    fun `returns fail if error and token are missing`() {
        val testState = "test_state"
        val testUri = mockUri(state = testState)
        val testIntent = mockIntent(uri = testUri)

        whenever(verifyStateUseCase.verifyAndClear(testState))
            .thenReturn(Single.just(true))

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        verify(verifyStateUseCase).verifyAndClear(testState)

        testObserver.assertError(AmazonDashException(R.string.amazon_dash_connect_error_unknown))
    }

    @Test
    fun `returns success if is valid`() {
        val testState = "test_state"
        val testToken = "test_token"
        val testUri = mockUri(state = testState, token = testToken)
        val testIntent = mockIntent(uri = testUri)

        whenever(verifyStateUseCase.verifyAndClear(testState))
            .thenReturn(Single.just(true))

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        verify(verifyStateUseCase).verifyAndClear(testState)

        testObserver.assertValue(testToken)
        testObserver.assertComplete()
    }

    @Test
    fun `returns fail if link contains errors`() {
        val testState = "test_state"
        val testError = "test_error"
        val testUri = mockUri(state = testState, error = testError)
        val testIntent = mockIntent(uri = testUri)

        whenever(verifyStateUseCase.verifyAndClear(testState))
            .thenReturn(Single.just(true))

        val testObserver = useCase.extractTokenFrom(testIntent).test()

        verify(verifyStateUseCase).verifyAndClear(testState)

        testObserver.assertError(AmazonDashException(testError))
    }

    private fun mockIntent(
        action: String = Intent.ACTION_VIEW,
        uri: Uri? = null
    ): Intent {
        return mock<Intent>().apply {
            whenever(this.action).thenReturn(action)
            whenever(this.data).thenReturn(uri)
        }
    }

    private fun mockUri(
        state: String? = null,
        error: String? = null,
        token: String? = null
    ): Uri {
        return mock<Uri>().apply {
            whenever(getQueryParameter(RESPONSE_STATE_KEY)).thenReturn(state)
            whenever(getQueryParameter(RESPONSE_ERROR_KEY)).thenReturn(error)
            whenever(getQueryParameter(RESPONSE_TOKEN_KEY)).thenReturn(token)
        }
    }
}
