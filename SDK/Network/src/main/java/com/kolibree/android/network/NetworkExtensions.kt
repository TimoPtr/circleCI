/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network

import android.net.Uri
import androidx.annotation.Keep
import com.kolibree.android.network.api.ApiError
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.HttpUrl
import org.json.JSONException
import retrofit2.Response
import timber.log.Timber

/**
 * The signature is calculation on the server side is always computed for HTTPS, but for local
 * backend development our URL will be HTTP-based. We need to replace it before signature
 * calculation, otherwise it will always fail.
 */
@Keep
fun String.prepareUrlForSignatureCalculation(): String =
    replaceFirst("http://", "https://")

/**
 * Potentially overrides the port for request URL, when port is not -1 in the override URI.
 *
 * @return port from original URL of override doesn't provide one, otherwise - the one from overridden uri
 */
internal fun pickTheRightPort(originalUrl: HttpUrl, overriddenUri: Uri): Int =
    if (overriddenUri.port != -1) overriddenUri.port else originalUrl.port()

/**
 * This extension method provides a simple way to parse Backend errors so they can be exposed to
 * the clients.
 *
 * @return [Completable] that emits an [ApiError] if the error is a backend error.
 */
@Keep
fun <T> Single<Response<T>>.toParsedResponseCompletable(): Completable =
    flatMapCompletable {
        when {
            it.isSuccessful -> Completable.complete()
            else -> Completable.error(errorResponseToApiError(it))
        }
    }

/**
 * This extension method provides a simple way to parse Backend errors so they can be exposed to
 * the clients.
 *
 * @return [Single] that emits an [ApiError] if the error is a backend error.
 */
@Keep
inline fun <reified T> Single<Response<T>>.toParsedResponseSingle(): Single<T> =
    flatMap {
        when {
            it.isSuccessful -> Single.just(it.body())
            else -> Single.error<T>(errorResponseToApiError(it))
        }
    }

/**
 * This method parses a failed Retrofit response, and converts the error body to an [ApiError]
 *
 * @param response a Retrofit failed [Response]
 * @return a parsed [ApiError]
 */
@Keep
fun errorResponseToApiError(response: Response<*>): ApiError =
    when (val errorBody = response.errorBody()) {
        null -> ApiError.generateUnknownError()
        else -> {
            // /!\ errorBody().string() returns empty if called more than one time !
            val errorBodyString = errorBody.string()
            when (errorBodyString.isNullOrBlank()) {
                true -> ApiError.generateUnknownError(httpCode = response.code())
                else -> try {
                    ApiError(errorBodyString)
                } catch (parsingException: JSONException) { // Sometimes backend errors have a weird JSON syntax !
                    Timber.e(parsingException, "ApiError parsing failed: $errorBodyString")
                    ApiError.generateUnknownError(httpCode = response.code())
                }
            }
        }
    }
