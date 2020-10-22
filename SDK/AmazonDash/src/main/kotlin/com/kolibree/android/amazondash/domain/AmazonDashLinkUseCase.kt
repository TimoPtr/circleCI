/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import androidx.annotation.VisibleForTesting
import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.network.toParsedResponseSingle
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

internal interface AmazonDashLinkUseCase {

    /**
     * Emits link that can be use for establishing
     * connection with Amazon Alexa.
     */
    fun getLink(): Single<String>
}

internal class AmazonDashLinkUseCaseImpl @Inject constructor(
    private val checkAlexaUseCase: AmazonDashCheckAlexaUseCase,
    private val verifyStateUseCase: AmazonDashVerifyStateUseCase,
    private val amazonDashApi: AmazonDashApi
) : AmazonDashLinkUseCase {

    override fun getLink(): Single<String> {
        return amazonDashApi
            .getLinks()
            .toParsedResponseSingle()
            .selectLink()
            .includeState()
            .doOnSuccess { link ->
                Timber.d("Link generated: $link")
            }
    }

    private fun Single<AmazonDashGetLinkResponse>.selectLink(): Single<String> {
        return flatMap { amazonLink ->
            checkAlexaUseCase
                .isAlexaAppAvailable()
                .map { isAvailable ->
                    if (isAvailable) amazonLink.appUrl
                    else amazonLink.fallbackUrl
                }
        }
    }

    private fun Single<String>.includeState(): Single<String> {
        return flatMap { link ->
            verifyStateUseCase
                .createNewState()
                .map { state ->
                    link.addQueryParameter(STATE_QUERY, state)
                }
        }
    }

    private fun String.addQueryParameter(key: String, value: String): String {
        val query = "${if (contains('?')) '&' else '?'}$key=$value"
        return "$this$query"
    }

    @VisibleForTesting
    internal companion object {
        const val STATE_QUERY = "state"
    }
}
