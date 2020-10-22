/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.network

import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.synchronizator.data.usecases.DeleteByUuidUseCase
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.UploadStatus
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.random.Random.Default.nextInt
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class CreateOrEditHttpErrorInterceptorTest : BaseUnitTest() {
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase = mock()
    private val deleteByUuidUseCase: DeleteByUuidUseCase = mock()

    private val interceptor =
        CreateOrEditHttpErrorInterceptor(updateUploadStatusUseCase, deleteByUuidUseCase)

    @Test
    fun `when there's an exception that's not HttpException, SocketTimeoutException or UnknownHostException, then intercept rethrows the exceptio`() {
        val wrapper = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()
        listOf(Exception(), Throwable(), IllegalArgumentException(), RuntimeException())
            .forEach { throwable ->
                var exceptionThrown = false
                try {
                    interceptor.intercept(throwable, wrapper, bundle)
                } catch (ignore: Throwable) {
                    exceptionThrown = true
                }

                assertTrue(exceptionThrown)
            }
    }

    @Test
    fun `when there's a SocketTimeoutException, then the Wrapper is flagged as pending`() {
        val wrapper = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()

        interceptor.intercept(SocketTimeoutException(), wrapper, bundle = bundle)

        verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
    }

    @Test
    fun `when there's a UnknownHostException, then the Wrapper is flagged as pending`() {
        val wrapper = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()

        interceptor.intercept(UnknownHostException(), wrapper, bundle = bundle)

        verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
    }

    @Test
    fun `when there's a HttpException with error code present in recoverableApiErrorCodes, then the Wrapper is flagged as pending`() {
        recoverableExceptions.forEach { httpErrorCode ->
            val wrapper = synchronizableItemWrapper()
            val bundle = synchronizableItemBundle()

            val response = Response.error<SynchronizableItem>(
                httpErrorCode,
                ResponseBody.create(null, "ignored")
            )

            interceptor.intercept(HttpException(response), wrapper, bundle = bundle)

            verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
        }
    }

    @Test
    fun `when there's a HttpException with error code equal or greater than 500, then the Wrapper is flagged as pending`() {
        (1 until 10)
            .map { nextInt(from = 504, until = 9999) }
            .plus(arrayOf(500, 501, 503))
            .forEach { httpErrorCode ->
                val wrapper = synchronizableItemWrapper()
                val bundle = synchronizableItemBundle()

                val response = Response.error<SynchronizableItem>(
                    httpErrorCode,
                    ResponseBody.create(null, "ignored")
                )

                interceptor.intercept(
                    HttpException(response),
                    wrapper,
                    bundle = bundle
                )

                verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
            }
    }

    @Test
    fun `when there's an ApiError with internal error code present in recoverableApiErrorCodes, then the Wrapper is flagged as pending`() {
        (recoverableApiErrorCodes.first()..recoverableApiErrorCodes.last())
            .filter { recoverableApiErrorCodes.contains(it) }
            .forEach { httpErrorCode ->
                val wrapper = synchronizableItemWrapper()
                val bundle = synchronizableItemBundle()

                interceptor.intercept(
                    ApiError("Test Api Error", httpErrorCode, "test error message"),
                    wrapper,
                    bundle = bundle
                )

                verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
            }

        verify(deleteByUuidUseCase, never()).delete(any(), any())
    }

    @Test
    fun `when there's an ApiError wrapped in another exception with internal error code present in recoverableApiErrorCodes, then the Wrapper is flagged as pending`() {
        (recoverableApiErrorCodes.first()..recoverableApiErrorCodes.last())
            .filter { recoverableApiErrorCodes.contains(it) }
            .forEach { httpErrorCode ->
                val wrapper = synchronizableItemWrapper()
                val bundle = synchronizableItemBundle()

                interceptor.intercept(
                    RuntimeException(
                        "Test api exception found!",
                        ApiError("Test Api Error", httpErrorCode, "test error message")
                    ),
                    wrapper,
                    bundle = bundle
                )

                verify(updateUploadStatusUseCase).update(wrapper, bundle, UploadStatus.PENDING)
            }

        verify(deleteByUuidUseCase, never()).delete(any(), any())
    }

    @Test
    fun `when there's a HttpException with error code NOT present in recoverableApiErrorCodes, then the Wrapper is deleted`() {
        (1 until 10)
            .map { nextInt(from = 400, until = 499) }
            .filter { randomErrorCode -> (randomErrorCode in recoverableExceptions).not() }
            .forEach { httpErrorCode ->
                val wrapper = synchronizableItemWrapper()
                val bundle = synchronizableItemBundle()

                val response = Response.error<SynchronizableItem>(
                    httpErrorCode,
                    ResponseBody.create(null, "ignored")
                )

                interceptor.intercept(
                    HttpException(response),
                    wrapper,
                    bundle = bundle
                )

                verify(deleteByUuidUseCase).delete(wrapper.uuid, bundle)
            }
    }

    @Test
    fun `when there's an ApiErrorinternal with error code NOT present in recoverableApiErrorCodes, then the Wrapper is deleted`() {
        val item = synchronizableItemWrapper()

        (recoverableApiErrorCodes.first()..recoverableApiErrorCodes.last())
            .filter { !recoverableApiErrorCodes.contains(it) }
            .forEach { httpErrorCode ->
                val bundle = synchronizableItemBundle()

                interceptor.intercept(
                    ApiError("Test Api Error", httpErrorCode, "test error message"),
                    item,
                    bundle = bundle
                )

                verify(deleteByUuidUseCase).delete(item.uuid, bundle)
            }
    }
}

private val recoverableExceptions = arrayOf(
    HTTP_UNAUTHORIZED,
    HTTP_FORBIDDEN,
    HTTP_CONFLICT,
    HTTP_CLIENT_TIMEOUT,
    HTTP_LOCKED,
    HTTP_TOO_EARLY,
    HTTP_TOO_MANY_REQUESTS
)

private val recoverableApiErrorCodes = arrayOf(
    ApiErrorCode.NETWORK_ERROR,
    ApiErrorCode.INVALID_ACCESS_TOKEN,
    ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED
).also { it.sort() }
