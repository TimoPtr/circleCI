/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization

import androidx.annotation.Keep
import okhttp3.Request
import okio.Timeout
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Keep
fun <T> OngoingStubbing<Call<T>>.thenReturnResponse(
    response: Response<T>,
    timeout: Timeout = Timeout.NONE
) {
    thenReturn(object : Call<T> {
        private var isExecuted = false
        private var isCanceled = false
        override fun enqueue(callback: Callback<T>) {
            // no-op
        }

        override fun isExecuted(): Boolean {
            return isExecuted
        }

        override fun clone(): Call<T> {
            return this
        }

        override fun isCanceled(): Boolean {
            return isCanceled
        }

        override fun cancel() {
            isCanceled = true
        }

        override fun request(): Request {
            TODO("not implemented")
        }

        override fun execute(): Response<T> {
            isExecuted = true

            return response
        }

        override fun timeout(): Timeout {
            return timeout
        }
    })
}
