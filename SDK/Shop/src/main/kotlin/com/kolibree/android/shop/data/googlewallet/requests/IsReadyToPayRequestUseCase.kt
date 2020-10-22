/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet.requests

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.wallet.PaymentsClient
import com.kolibree.android.shop.data.googlewallet.GoogleWalletRequestProvider
import io.reactivex.Single
import io.reactivex.SingleEmitter
import timber.log.Timber

internal interface IsReadyToPayRequestUseCase {
    /**
     * Determines if the user can make payments using the Google Pay API
     *
     * This API checks the following minimum requirements to finish a transaction using the Google Pay API:
     * - Device is running on a supported Android system version and also has a supported version of
     * Google Play services installed.
     * - Google Pay API has launched in the user's country.
     * - User either has or can add a card in flow according to the specifications given in the
     * IsReadyToPayRequest.
     *
     * Note that the requirements mentioned above are non-exhaustive and may change over time.
     *
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#isReadyToPay(com.google.android.gms.wallet.IsReadyToPayRequest)">IsReadyToPayRequest</a>
     */
    fun isReadyToPayRequest(): Single<Boolean>
}

internal class IsReadyToPayRequestUseCaseImpl constructor(
    private val paymentsClient: PaymentsClient,
    private val walletRequestProvider: GoogleWalletRequestProvider
) : IsReadyToPayRequestUseCase {
    override fun isReadyToPayRequest(): Single<Boolean> {
        return walletRequestProvider.isReadyToPayRequest()
            .flatMap { request ->
                Single.create<Boolean> { emitter ->
                    paymentsClient.isReadyToPay(request)
                        .addOnCompleteListener { completedTask ->
                            emitter.callGetResultSafely {
                                completedTask.getResult(ApiException::class.java)
                            }
                        }
                }
            }
    }

    /**
     * See https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#getResult(java.lang.Class%3CX%3E)
     * for error handling
     */
    private inline fun <T : Any?> SingleEmitter<T>.callGetResultSafely(block: () -> T) {
        try {
            block()?.let { result -> onSuccess(result) }
                ?: throw RuntimeExecutionException(IllegalStateException("result was null"))
        } catch (exception: IllegalStateException) {
            Timber.w(exception, "Task is not yet complete")
            onError(exception)
        } catch (exception: RuntimeExecutionException) {
            Timber.w(exception, "Task failed with unexpected exception")
            onError(exception)
        } catch (exception: ApiException) {
            Timber.w(exception, "Task failed")
            onError(exception)
        }
    }
}
