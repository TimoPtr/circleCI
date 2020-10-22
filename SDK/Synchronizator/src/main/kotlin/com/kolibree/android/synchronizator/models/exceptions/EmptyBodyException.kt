/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.synchronizator.models.exceptions

import androidx.annotation.Keep
import retrofit2.HttpException
import retrofit2.Response

@Keep
class EmptyBodyException(response: Response<*>) : HttpException(response)
