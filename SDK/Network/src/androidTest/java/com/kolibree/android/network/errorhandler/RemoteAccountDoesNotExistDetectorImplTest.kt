/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.errorhandler

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.commons.NoOpExceptionLogger
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.test.SharedTestUtils
import io.reactivex.schedulers.TestScheduler
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RemoteAccountDoesNotExistDetectorImplTest {
    private val testScheduler = TestScheduler()

    private val accountDoesNotExistDetector = RemoteAccountDoesNotExistDetectorImpl(
        testScheduler,
        NoOpExceptionLogger
    )

    /*
    accept response
     */
    @Test
    fun acceptResponse_does_nothing_if_Response_is_successful() {
        val response = createResponse(200)

        assertTrue(response.isSuccessful)

        assertAccountDoesNotExistStreamDoesNotEmit(response)
    }

    @Test
    fun acceptResponse_does_nothing_if_Response_is_error_but_not_404() {
        arrayOf(HTTP_INTERNAL_ERROR, HTTP_UNAUTHORIZED).forEach { errorCode ->
            val response = createResponse(errorCode)

            assertFalse(response.isSuccessful)

            assertAccountDoesNotExistStreamDoesNotEmit(response)
        }
    }

    @Test
    fun acceptResponse_throwsApiError_if_acceptBody_emitsApiError() {
        val response = createResponse(HttpURLConnection.HTTP_NOT_FOUND)

        assertFalse(response.isSuccessful)

        assertAccountDoesNotExistStreamDoesNotEmit(response)
    }

    @Test
    fun acceptRespons_does_nothing_if_Response_is_404_but_body_is_empty() {
        val response = createResponse(404)

        assertFalse(response.isSuccessful)

        assertAccountDoesNotExistStreamDoesNotEmit(response)
    }

    @Test
    fun acceptRespons_does_nothing_if_Response_is_404_but_internal_error_is_not_4() {
        val response = createResponse(404, "json/expired_access_token.json")

        assertFalse(response.isSuccessful)

        assertAccountDoesNotExistStreamDoesNotEmit(response)
    }

    @Test
    fun acceptRespons_does_nothing_if_Response_is_404_but_body_is_larger_than_MAX_BODY_SIZE() {
        val largeString = String(CharArray((MAX_BODY_SIZE + 1).toInt()))

        val response = responseBuilder(404)
            .body(ResponseBody.create(MediaType.parse("application/json"), largeString))
            .build()

        assertFalse(response.isSuccessful)

        assertAccountDoesNotExistStreamDoesNotEmit(response)
    }

    @Test(expected = ApiError::class)
    fun acceptRespons_throws_ApiError_if_Response_is_404_and_internal_error_is_4() {
        val response = createResponse(404, "json/error_account_does_not_exist.json")

        assertFalse(response.isSuccessful)

        accountDoesNotExistDetector.accept(response)
    }

    @Test(expected = ApiError::class)
    fun accept_accountDoesNotExistStream_emits_value_if_Response_is_404_and_internal_error_is_4() {
        val response = createResponse(404, "json/error_account_does_not_exist.json")

        assertFalse(response.isSuccessful)

        val observer = accountDoesNotExistDetector.accountDoesNotExistStream.test()

        observer.assertEmpty()

        accountDoesNotExistDetector.accept(response)

        advanceTimeForDebouncedResults()

        observer.assertValueCount(1).assertValue(true)
    }

    /*
    accept ApiError
     */

    @Test
    fun acceptApiError_returns_false_if_apiError_is_null() {
        assertFalse(accountDoesNotExistDetector.acceptApiError(apiError = null))
    }

    @Test
    fun acceptApiError_returns_false_if_apiError_is_networkError() {
        assertFalse(accountDoesNotExistDetector.acceptApiError(ApiError.generateNetworkError()))
    }

    @Test
    fun acceptApiError_returns_false_if_apiError_is_unknownError() {
        assertFalse(accountDoesNotExistDetector.acceptApiError(ApiError.generateUnknownError()))
    }

    @Test
    fun acceptApiError_returns_false_if_apiError_is_CODE_EXPIRED_ACCESS_TOKEN() {
        val apiError = ApiError("da", ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED, "da")
        assertFalse(accountDoesNotExistDetector.acceptApiError(apiError))
    }

    @Test
    fun acceptApiError_returns_true_if_apiError_is_CODE_ACCOUNT_NOT_EXIST() {
        val apiError = ApiError("da", ApiErrorCode.ACCOUNT_DOES_NOT_EXIST, "da")
        assertTrue(accountDoesNotExistDetector.acceptApiError(apiError))
    }

    /*
    acceptErrorBody
     */

    @Test
    fun acceptErrorBody_returns_null_if_body_is_null() {
        assertNull(accountDoesNotExistDetector.acceptErrorBody(null))
    }

    @Test
    fun acceptErrorBody_returns_null_if_body_is_empty() {
        assertNull(accountDoesNotExistDetector.acceptErrorBody(""))
    }

    @Test
    fun acceptErrorBody_returns_null_if_body_is_malformed() {
        val responseBody = "{[[}"

        assertNull(accountDoesNotExistDetector.acceptErrorBody(responseBody))
    }

    @Test
    fun acceptErrorBody_returns_null_if_internal_error_is_not_4() {
        val responseBody = SharedTestUtils.getJson("json/expired_access_token.json")

        assertNull(accountDoesNotExistDetector.acceptErrorBody(responseBody))
    }

    @Test
    fun acceptErrorBody_returns_ApiError_if_internal_error_is_4() {
        val responseBody = SharedTestUtils.getJson("json/error_account_does_not_exist.json")

        val apiError: ApiError = accountDoesNotExistDetector.acceptErrorBody(responseBody)!!

        assertEquals(ApiErrorCode.ACCOUNT_DOES_NOT_EXIST, apiError.internalErrorCode)
    }

    @Test
    fun acceptErrorBody_stream_emitsTrue_if_internal_error_is_4() {
        val responseBody = SharedTestUtils.getJson("json/error_account_does_not_exist.json")

        val observer = accountDoesNotExistDetector.accountDoesNotExistStream.test()

        observer.assertEmpty()

        accountDoesNotExistDetector.acceptErrorBody(responseBody)

        advanceTimeForDebouncedResults()

        observer.assertValueCount(1).assertValue(true)
    }

    /*
    accountDoesNotExistStream
     */
    @Test
    fun accountDoesNotExistStream_only_emits_results_happening_more_than_2_seconds_from_previous() {
        val observer = accountDoesNotExistDetector.accountDoesNotExistStream.test()

        observer.assertEmpty()

        accountDoesNotExistDetector.accountDoesNotExistRelay.accept(true)

        observer.assertEmpty()

        val totalDebounceTime = DEBOUNCE_DETECTIONS_SECONDS

        advanceTimeForDebouncedResults(totalDebounceTime / 2)

        observer.assertEmpty()

        accountDoesNotExistDetector.accountDoesNotExistRelay.accept(true)
        accountDoesNotExistDetector.accountDoesNotExistRelay.accept(true)

        advanceTimeForDebouncedResults(totalDebounceTime)

        observer.assertValueCount(1).assertValue(true)
    }

    /*
    UTILS
     */

    private fun assertAccountDoesNotExistStreamDoesNotEmit(response: Response) {
        val observer = accountDoesNotExistDetector.accountDoesNotExistStream.test()

        observer.assertEmpty()

        accountDoesNotExistDetector.accept(response)

        advanceTimeForDebouncedResults()

        observer.assertEmpty()
    }

    private fun advanceTimeForDebouncedResults(advanceBy: Long = DEBOUNCE_DETECTIONS_SECONDS) {
        testScheduler.advanceTimeBy(advanceBy, TimeUnit.SECONDS)
    }

    private fun createResponse(code: Int, responseFilePath: String? = null): Response {
        var responseBuilder = responseBuilder(code)

        responseFilePath?.let { path ->
            val responseBody = SharedTestUtils.getJson(path)

            responseBuilder = responseBuilder
                .body(ResponseBody.create(MediaType.parse("application/json"), responseBody))
        }

        return responseBuilder.build()
    }

    private fun responseBuilder(code: Int): Response.Builder {
        return Response.Builder()
            .request(Request.Builder().url("http://www.example.com").build())
            .protocol(Protocol.HTTP_2)
            .message("")
            .code(code)
    }
}
