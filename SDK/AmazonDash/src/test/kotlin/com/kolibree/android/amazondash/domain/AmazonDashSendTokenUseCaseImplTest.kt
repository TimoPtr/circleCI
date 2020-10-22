/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.data.model.AmazonDashException
import com.kolibree.android.amazondash.data.model.AmazonDashSendTokenRequest
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.mocks.createAccountInternal
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class AmazonDashSendTokenUseCaseImplTest : BaseUnitTest() {

    private val accountDataStore: AccountDatastore = mock()
    private val amazonDashApi: AmazonDashApi = mock()
    private val synchronizator: Synchronizator = mock()

    private lateinit var useCase: AmazonDashSendTokenUseCase

    override fun setup() {
        super.setup()
        useCase = AmazonDashSendTokenUseCaseImpl(
            accountDataStore,
            amazonDashApi,
            synchronizator
        )

        whenever(synchronizator.delaySynchronizeCompletable())
            .thenReturn(Completable.complete())
    }

    @Test
    fun `returns default error if account not available`() {
        whenever(accountDataStore.getAccountMaybe())
            .thenReturn(Maybe.empty())

        val testObserver = useCase.sendToken("token").test()

        verifyNoMoreInteractions(amazonDashApi)
        testObserver.assertError(AmazonDashException(R.string.amazon_dash_connect_error_unknown))
    }

    @Test
    fun `returns default error if unknown network error`() {
        val testToken = "token"
        val testAccount = createAccountInternal()
        val testResponse = Response.error<ResponseBody>(
            HTTP_INTERNAL_ERROR,
            errorResponse(ApiErrorCode.UNKNOWN_ERROR)
        )

        whenever(accountDataStore.getAccountMaybe())
            .thenReturn(Maybe.just(createAccountInternal()))

        whenever(amazonDashApi.sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken)))
            .thenReturn(Single.just(testResponse))

        val testObserver = useCase.sendToken(testToken).test()

        verify(amazonDashApi).sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken))
        testObserver.assertError(ApiError::class.java)
    }

    @Test
    fun `returns timeout error when there was a timeout`() {
        val testToken = "token"
        val testError = "Test error"
        val testAccount = createAccountInternal()
        val testResponse = Response.error<ResponseBody>(
            HttpURLConnection.HTTP_CLIENT_TIMEOUT,
            errorResponse(ApiErrorCode.AMAZON_DRS_UNABLE_TO_CONNECT, testError)
        )

        whenever(accountDataStore.getAccountMaybe())
            .thenReturn(Maybe.just(createAccountInternal()))

        whenever(amazonDashApi.sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken)))
            .thenReturn(Single.just(testResponse))

        val testObserver = useCase.sendToken(testToken).test()

        verify(amazonDashApi).sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken))
        testObserver.assertError(ApiError::class.java)
    }

    @Test
    fun `returns backend error message if request was forbidden`() {
        val testToken = "token"
        val testError = "Test error"
        val testAccount = createAccountInternal()
        val testResponse = Response.error<ResponseBody>(
            HTTP_FORBIDDEN,
            errorResponse(ApiErrorCode.AMAZON_DRS_AUTHENTICATION_FAILED, testError)
        )

        whenever(accountDataStore.getAccountMaybe())
            .thenReturn(Maybe.just(createAccountInternal()))

        whenever(amazonDashApi.sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken)))
            .thenReturn(Single.just(testResponse))

        val testObserver = useCase.sendToken(testToken).test()

        verify(amazonDashApi).sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken))
        testObserver.assertError(ApiError::class.java)
    }

    @Test
    fun `returns success if upload was successful`() {
        val testToken = "token"
        val testAccount = createAccountInternal()
        val testResponse = Response.success<ResponseBody>(
            ResponseBody.create(null, "")
        )

        whenever(accountDataStore.getAccountMaybe())
            .thenReturn(Maybe.just(createAccountInternal()))

        whenever(amazonDashApi.sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken)))
            .thenReturn(Single.just(testResponse))

        val testObserver = useCase.sendToken(testToken).test()

        verify(amazonDashApi).sendToken(testAccount.id, AmazonDashSendTokenRequest(testToken))
        verify(synchronizator).delaySynchronizeCompletable()
        testObserver.assertComplete()
    }

    private fun errorResponse(
        internalErrorCode: Int,
        message: String = ""
    ): ResponseBody {
        return ResponseBody.create(
            null,
            "{\"internal_error_code\":$internalErrorCode,\"display_message\":\"$message\"}"
        )
    }
}
