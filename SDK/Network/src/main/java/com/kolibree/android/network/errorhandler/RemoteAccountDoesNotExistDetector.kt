/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.errorhandler

import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode.ACCOUNT_DOES_NOT_EXIST
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import okhttp3.Response
import org.json.JSONException
import timber.log.Timber

@VisibleForApp
interface RemoteAccountDoesNotExistDetector {
    /**
     * Returns an Observable<[Boolean]> that will emit true each time we detect a Response that
     * signals that an account does not exist.
     *
     * In order to avoid overflowing consumers, it'll buffer emissions in 2 seconds windows
     */
    val accountDoesNotExistStream: Observable<Boolean>
}

/**
 * Analyzes an incoming Response and throws [ApiError] if the body contains an "Account does not exist"
 * error
 */
@AppScope
internal class RemoteAccountDoesNotExistDetectorImpl
@VisibleForTesting
constructor(
    scheduler: Scheduler,
    private val exceptionLogger: ExceptionLogger
) : NetworkErrorHandler, RemoteAccountDoesNotExistDetector {

    @Inject
    constructor(exceptionLogger: ExceptionLogger) : this(Schedulers.io(), exceptionLogger)

    @VisibleForTesting
    val accountDoesNotExistRelay: PublishRelay<Boolean> = PublishRelay.create()

    override val accountDoesNotExistStream: Observable<Boolean> = accountDoesNotExistRelay
        .debounce(DEBOUNCE_DETECTIONS_SECONDS, TimeUnit.SECONDS, scheduler)
        .publish()
        .refCount()

    override fun acceptApiError(apiError: ApiError?): Boolean {
        if (apiError?.internalErrorCode == ACCOUNT_DOES_NOT_EXIST) {
            notifyAccountDoesNotExistDetected()

            return true
        } else {
            apiError?.let(exceptionLogger::logException)
        }

        return false
    }

    @Throws(ApiError::class)
    override fun accept(response: Response): Response {
        if (mightBeAccountDoesNotExistError(response)) {
            acceptErrorBody(extractBody(response))?.let { throw it }
        }

        return response
    }

    @VisibleForTesting
    fun acceptErrorBody(errorBody: String?): ApiError? {
        try {
            val apiError = ApiError(errorBody)

            if (acceptApiError(apiError)) {
                return apiError
            }
        } catch (jsonException: JSONException) {
            Timber.w(jsonException)
        }

        return null
    }

    @VisibleForTesting
    fun notifyAccountDoesNotExistDetected() {
        accountDoesNotExistRelay.accept(true)
    }

    private fun mightBeAccountDoesNotExistError(response: Response) =
        !response.isSuccessful && response.code() == HttpURLConnection.HTTP_NOT_FOUND

    private fun extractBody(response: Response): String? {
        return try {
            val responseBody = response.peekBody(MAX_BODY_SIZE)

            responseBody.string()
        } catch (npe: NullPointerException) {
            null
        }
    }
}

@VisibleForTesting
const val MAX_BODY_SIZE = 1048576L // 1MB to Bytes

@VisibleForTesting
const val DEBOUNCE_DETECTIONS_SECONDS = 2L
