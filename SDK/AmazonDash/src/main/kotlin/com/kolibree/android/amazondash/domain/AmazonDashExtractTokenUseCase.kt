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
import androidx.annotation.VisibleForTesting
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.data.model.AmazonDashException
import io.reactivex.Maybe
import javax.inject.Inject
import timber.log.Timber

internal interface AmazonDashExtractTokenUseCase {

    /**
     * Extracts Amazon response from the [intent].
     */
    fun extractTokenFrom(intent: Intent?): Maybe<String>
}

internal class AmazonDashExtractTokenUseCaseImpl @Inject constructor(
    private val verifyStateUseCase: AmazonDashVerifyStateUseCase
) : AmazonDashExtractTokenUseCase {

    override fun extractTokenFrom(intent: Intent?): Maybe<String> {
        return Maybe
            .fromCallable<Uri> { intent.extractData() }
            .verifyData()
            .extractToken()
            .doOnSuccess { response ->
                Timber.d("Received response: $response")
            }
    }

    private fun Intent?.extractData(): Uri? {
        Timber.d("Handle intent: $this data: ${this?.data}")

        return this?.takeIf { it.action == Intent.ACTION_VIEW }?.data ?: run {
            Timber.d("Intent doesn't contain response - rejecting")
            null
        }
    }

    private fun Maybe<Uri>.verifyData(): Maybe<Pair<Boolean, Uri>> {
        return flatMapSingleElement { data ->
            verifyStateUseCase
                .verifyAndClear(state = data.getQueryParameter(RESPONSE_STATE_KEY))
                .map { isValid -> isValid to data }
        }
    }

    private fun Maybe<Pair<Boolean, Uri>>.extractToken(): Maybe<String> {
        return map { (isValid, data) ->
            val token = data.getQueryParameter(RESPONSE_TOKEN_KEY)
            if (isValid && token != null) {
                return@map token
            }

            val error = data.getQueryParameter(RESPONSE_ERROR_KEY)
            throw when {
                !isValid -> AmazonDashException(R.string.amazon_dash_connect_error_invalid_link)
                error != null -> AmazonDashException(error)
                else -> AmazonDashException(R.string.amazon_dash_connect_error_unknown)
            }
        }
    }

    @VisibleForTesting
    internal companion object {
        const val RESPONSE_STATE_KEY = "state"
        const val RESPONSE_ERROR_KEY = "error_description"
        const val RESPONSE_TOKEN_KEY = "code"
    }
}
