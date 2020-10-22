package com.kolibree.android.network

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

/** Single<T> NetworkExtensions tests */
@RunWith(AndroidJUnit4::class)
class NetworkExtensionsTest {

    /*
    prepareUrlForSignatureCalculation
     */

    @Test
    fun prepareUrlForSignatureCalculation_replacesHttp_withHttps() {
        assertEquals(
            "https://example.com",
            "http://example.com".prepareUrlForSignatureCalculation()
        )
    }

    @Test
    fun prepareUrlForSignatureCalculation_leavesHttps_asIs() {
        assertEquals(
            "https://example.com",
            "https://example.com".prepareUrlForSignatureCalculation()
        )
    }

    /*
    pickTheRightPort
    */

    @Test
    fun pickTheRightPort_replacesPort_ifItIsAvailable() {
        assertEquals(
            443,
            pickTheRightPort(
                HttpUrl.get("http://localhost:80"),
                Uri.parse("http://localhost:443")
            )
        )
    }

    @Test
    fun pickTheRightPort_leavesOriginal_ifOverrideHasMinusOne() {
        assertEquals(
            432,
            pickTheRightPort(
                HttpUrl.get("http://localhost:432"),
                Uri.parse("http://localhost")
            )
        )
    }

    /*
    errorResponseToApiError
     */

    @Test
    fun errorResponseToApiError_returnsUnknownApiError_whenThereIsNoErrorBody() {
        val response = Response.error<Void>(
            402,
            ResponseBody.create(MediaType.parse("application/json"), "")
        )

        val apiError = errorResponseToApiError(response)
        assertTrue(apiError.isUnknownError)
    }

    @Test
    fun errorResponseToApiError_returnsParsedApiError_whenThereIsABackendErrorBody() {
        val response = Response.error<Void>(
            402,
            ResponseBody.create(MediaType.parse("application/json"), ERROR_JSON)
        )

        val apiError = errorResponseToApiError(response)
        assertEquals(433, apiError.internalErrorCode)
    }

    @Test
    fun errorResponseToApiError_returnsRecoveredErrorWithBodyContentAndProperCode_whenTheErrorBodyIsInvalid() {
        val invalidJson = "{I'm invalid :(!]"
        val expectedCode = 404
        val response = Response.error<Void>(
            expectedCode,
            ResponseBody.create(MediaType.parse("application/json"), invalidJson)
        )

        val apiError = errorResponseToApiError(response)
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, apiError.internalErrorCode)
        assertEquals(expectedCode, apiError.httpCode)
        assertEquals(ApiError.generateUnknownError(expectedCode).toString(), apiError.toString())
    }

    /*
    toParsedResponseSingle
     */

    @Test
    fun toParsedResponseSingle_emitsResponseBody_whenTheRequestIsSuccessful() {
        val integerBody = 1986
        val response = Response.success(integerBody)

        val body = Single.just(response)
            .toParsedResponseSingle()
            .test()
            .assertNoErrors()
            .values()
            .first()

        assertEquals(integerBody, body)
    }

    @Test
    fun toParsedResponseSingle_emitsAParsedApiError_whenTheRequestIsNotSuccessful() {
        val response = Response.error<Int>(400, ResponseBody.create(null, ERROR_JSON))
        val apiError = Single.just(response)
            .toParsedResponseSingle()
            .test()
            .assertError(ApiError::class.java)
            .errors()
            .first()

        assertEquals(433, (apiError as ApiError).internalErrorCode)
    }

    /*
    toParsedResponseCompletable
     */

    @Test
    fun toParsedResponseCompletable_completes_whenTheRequestIsSuccessful() {
        val response = Response.success<Void>(null)
        Single.just(response)
            .toParsedResponseCompletable()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
    }

    @Test
    fun toParsedResponseCompletable_emitsAParsedApiError_whenTheRequestIsNotSuccessful() {
        val response = Response.error<Void>(400, ResponseBody.create(null, ERROR_JSON))

        val apiError = Single.just(response)
            .toParsedResponseCompletable()
            .test()
            .assertError(ApiError::class.java)
            .errors()
            .first()

        assertEquals(433, (apiError as ApiError).internalErrorCode)
    }

    companion object {

        const val ERROR_JSON = "{\n" +
            "    \"display_message\": \"Phone number already exists\",\n" +
            "    \"message\": \"E433: Phone number already exists.\",\n" +
            "    \"internal_error_code\": 433,\n" +
            "    \"detail\": \"An account with this phone number already exists.\",\n" +
            "    \"http_code\": 400\n" +
            "}"
    }
}
