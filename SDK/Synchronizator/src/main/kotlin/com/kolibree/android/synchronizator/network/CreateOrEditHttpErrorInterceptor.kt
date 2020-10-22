/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.network

import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.data.usecases.DeleteByUuidUseCase
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.network.InterceptAction.DELETE_LOCAL
import com.kolibree.android.synchronizator.network.InterceptAction.FLAG_AS_PENDING
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException
import timber.log.Timber

/**
 * https://kolibree.atlassian.net/browse/KLTB002-9824
 */
internal class CreateOrEditHttpErrorInterceptor @Inject constructor(
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase,
    private val deleteByUuidUseCase: DeleteByUuidUseCase
) {
    @Throws(Throwable::class)
    fun intercept(
        throwable: Throwable,
        wrapper: SynchronizableItemWrapper,
        bundle: SynchronizableItemBundle
    ) {
        /*
        Detect if we can recover the error later. If so, hide the error

        Otherwise, throw

        https://kolibree.atlassian.net/browse/KLTB002-6076
         */
        when (actionFromException(throwable)) {
            FLAG_AS_PENDING -> flagAsPending(wrapper, bundle)
            DELETE_LOCAL -> delete(wrapper, bundle)
        }

        Timber.w(throwable, "Http error occurred but we are able to recover.")
    }

    private fun delete(
        wrapper: SynchronizableItemWrapper,
        bundle: SynchronizableItemBundle
    ) = deleteByUuidUseCase.delete(wrapper.uuid, bundle)

    private fun flagAsPending(
        wrapper: SynchronizableItemWrapper,
        bundle: SynchronizableItemBundle
    ) {
        updateUploadStatusUseCase.update(
            wrapper = wrapper,
            newUploadStatus = UploadStatus.PENDING,
            bundle = bundle
        )
    }

    @Throws(Throwable::class)
    private fun actionFromException(throwable: Throwable): InterceptAction {
        return when {
            throwable is ApiError -> intercept(throwable)
            throwable.cause is ApiError -> intercept(throwable.cause as ApiError)
            throwable is SocketTimeoutException -> intercept(throwable)
            throwable is UnknownHostException -> intercept(throwable)
            throwable is HttpException -> intercept(throwable)
            else -> throw throwable
        }
    }

    private fun intercept(apiError: ApiError): InterceptAction = when (apiError.internalErrorCode) {
        in recoverableApiErrorCodes -> FLAG_AS_PENDING
        else -> DELETE_LOCAL
    }

    private fun intercept(httpException: HttpException): InterceptAction {
        val httpErrorCode = httpException.code()
        return when {
            httpErrorCode in recoverableExceptions -> FLAG_AS_PENDING
            httpErrorCode >= HTTP_INTERNAL_ERROR -> FLAG_AS_PENDING
            else -> DELETE_LOCAL
        }
    }

    private fun intercept(httpException: SocketTimeoutException): InterceptAction = FLAG_AS_PENDING

    private fun intercept(httpException: UnknownHostException): InterceptAction = FLAG_AS_PENDING
}

private enum class InterceptAction {
    FLAG_AS_PENDING, DELETE_LOCAL;
}

internal const val HTTP_TOO_MANY_REQUESTS = 429
internal const val HTTP_LOCKED = 423
internal const val HTTP_TOO_EARLY = 425

/*
See https://kolibree.atlassian.net/browse/KLTB002-9824
 */
private val recoverableExceptions = arrayOf(
    /*
    A future refresh token might solve the issue
     */
    HTTP_UNAUTHORIZED,
    /*
    A future refresh token might solve the issue
     */
    HTTP_FORBIDDEN,
    /*
    A future synchronization will resolve the conflict

    Rerunning RemoteCreateOrEdit will run into the same http error code
     */
    HTTP_CONFLICT,
    /*
    A future retry under better network conditions might solve the issue
     */
    HTTP_CLIENT_TIMEOUT,
    /*
    A future retry might not receive this error code
     */
    HTTP_LOCKED,
    /*
    Issues with TLS handshake
     */
    HTTP_TOO_EARLY,
    /*
    Our nb ofrequests will be below the limit in the future
     */
    HTTP_TOO_MANY_REQUESTS
)

private val recoverableApiErrorCodes = arrayOf(
    ApiErrorCode.NETWORK_ERROR,
    ApiErrorCode.INVALID_ACCESS_TOKEN,
    ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED
)
