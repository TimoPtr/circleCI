/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.core

import com.kolibree.android.annotation.VisibleForApp
import javax.inject.Inject
import okhttp3.OkHttpClient
import timber.log.Timber

@VisibleForApp
interface CancelHttpRequestsUseCase {
    fun cancelAll()
}

internal class CancelHttpRequestsUseCaseImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : CancelHttpRequestsUseCase {
    override fun cancelAll() = okHttpClient.dispatcher().cancelAll()
        .also { Timber.w("Canceled all OkHttp requests") }
}
