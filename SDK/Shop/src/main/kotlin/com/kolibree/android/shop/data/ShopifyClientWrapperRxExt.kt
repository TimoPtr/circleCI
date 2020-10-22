/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import com.kolibree.android.shop.data.request.GraphClientMutationRequest
import com.kolibree.android.shop.data.request.GraphClientPollingRequest
import com.kolibree.android.shop.data.request.GraphClientRequest
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphCallResultCallback
import com.shopify.buy3.GraphClient
import com.shopify.buy3.RetryHandler
import com.shopify.buy3.Storefront
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import timber.log.Timber

internal fun <T : Any> GraphClient.executeRxQuery(
    request: GraphClientRequest<T>
): Single<T> = Single.create<T> { emitter ->
    val requestQuery = request.buildQuery()
    try {
        queryGraph(requestQuery).enqueue(
            callback = request.queryResultCallback(emitter)
        )
    } catch (e: RuntimeException) {
        Timber.w(e)
        emitter.tryOnError(e)
    }
}.subscribeOn(Schedulers.io())

internal fun <T : Any> GraphClient.executeRxPollingQuery(
    request: GraphClientPollingRequest<T>
): Single<T> = Single.create<T> { emitter ->
    val requestQuery = request.buildQuery()

    queryGraph(requestQuery).enqueue(
        callback = request.queryResultCallback(emitter),
        retryHandler = createRetryHandler(request.retryCondition()),
        callbackHandler = null
    )
}.subscribeOn(Schedulers.io())

internal fun <T : Any> GraphClient.executeRxMutationQuery(
    request: GraphClientMutationRequest<T>
): Single<T> = Single.create<T> { emitter ->
    val requestQuery = request.buildQuery()

    mutateGraph(requestQuery).enqueue(
        callback = requestCallback(request, emitter),
        retryHandler = createMutationRetryHandler(request.isSuccess())
    )
}.subscribeOn(Schedulers.io())

private fun <T : Any> requestCallback(
    request: GraphClientMutationRequest<T>,
    emitter: SingleEmitter<T>
): GraphCallResultCallback<Storefront.Mutation> {
    return object : GraphCallResultCallback<Storefront.Mutation> {
        override fun invoke(result: GraphCallResult<Storefront.Mutation>) {
            try {
                when (result) {
                    is GraphCallResult.Success -> {
                        val requestResponse = request.buildResponse(result.response)
                        emitter.onSuccess(requestResponse)
                    }
                    is GraphCallResult.Failure -> emitter.tryOnError(result.error)
                }
            } catch (ex: Exception) {
                emitter.tryOnError(ex)
            }
        }
    }
}

internal fun GraphClient.executeRxMutationQueryCompletable(
    request: GraphClientMutationRequest<Unit>
): Completable = Completable.create { subscriber ->
    val requestQuery = request.buildQuery()

    mutateGraph(requestQuery).enqueue(
        callback = object : GraphCallResultCallback<Storefront.Mutation> {
            override fun invoke(result: GraphCallResult<Storefront.Mutation>) {
                try {
                    when (result) {
                        is GraphCallResult.Success -> {
                            request.buildResponse(result.response)
                            subscriber.onComplete()
                        }
                        is GraphCallResult.Failure -> subscriber.tryOnError(result.error)
                    }
                } catch (ex: Exception) {
                    subscriber.tryOnError(ex)
                }
            }
        },
        retryHandler = createMutationRetryHandler(request.isSuccess())
    )
}.subscribeOn(Schedulers.io())

private const val RETRY_MAX_COUNT = 10
private const val RETRY_DELAY_MILLIS = 500L
private const val RETRY_MULTIPLIER = 1.2f

private inline fun createRetryHandler(crossinline retryCondition: (Storefront.QueryRoot) -> Boolean) =
    RetryHandler.Companion.build<Storefront.QueryRoot>(
        delay = RETRY_DELAY_MILLIS,
        timeUnit = TimeUnit.MILLISECONDS,
        configure = {
            exponentialBackoff(RETRY_MULTIPLIER)

            retryWhen { result ->
                if (result is GraphCallResult.Success<Storefront.QueryRoot>) {
                    result.response.data?.let { data -> !retryCondition(data) } ?: false
                } else {
                    false
                }
            }

            maxAttempts(RETRY_MAX_COUNT)
        }
    )

private inline fun createMutationRetryHandler(
    crossinline isSuccess: (Storefront.Mutation?) -> Boolean
) =
    RetryHandler.Companion.build<Storefront.Mutation>(
        delay = RETRY_DELAY_MILLIS,
        timeUnit = TimeUnit.MILLISECONDS,
        configure = {
            exponentialBackoff(RETRY_MULTIPLIER)

            retryWhen { result ->
                if (result is GraphCallResult.Success<Storefront.Mutation>) {
                    !isSuccess(result.response.data).also(logStatus(result))
                } else {
                    false
                }
            }

            maxAttempts(RETRY_MAX_COUNT)
        }
    )

private fun logStatus(result: GraphCallResult.Success<*>): (Boolean) -> Unit = { isSuccess ->
    val responseData = result.response.data?.responseData?.keys
    if (isSuccess) {
        Timber.i("Shopify Request Successful : $responseData")
    } else {
        val errorMessage = result.response.formattedErrorMessage
        Timber.e("Shopify Request Failed : $errorMessage\n - $responseData")
    }
}

private fun <T : Any> GraphClientRequest<T>.queryResultCallback(
    emitter: SingleEmitter<T>
): GraphCallResultCallback<Storefront.QueryRoot> {
    return object : GraphCallResultCallback<Storefront.QueryRoot> {
        override fun invoke(result: GraphCallResult<Storefront.QueryRoot>) {
            try {
                when (result) {
                    is GraphCallResult.Success -> {
                        val requestResponse = buildResponse(result.response)
                        emitter.onSuccess(requestResponse)
                    }
                    is GraphCallResult.Failure -> emitter.tryOnError(result.error)
                }
            } catch (ex: Exception) {
                emitter.tryOnError(ex)
            }
        }
    }
}
