/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.errorhandler

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.api.ApiError
import okhttp3.Response

/**
 * SDK global network error handler
 */
@VisibleForApp
interface NetworkErrorHandler {
    /**
     * Analyzes [response] in search for errors it can handle
     *
     * @throws ApiError if an error in [response] has been handled
     * @return the same [response] parameter, unmodified and with body not consumed
     */
    @Throws(ApiError::class)
    fun accept(response: Response): Response

    /**
     * Analyzes [apiError] in search for an error it knows how to handle
     *
     * @return true if the error was handled, false otherwise
     */
    fun acceptApiError(apiError: ApiError?): Boolean
}
