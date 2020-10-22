/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet.exceptions

/**
 * Google Pay returned RESULT_OK but it didn't contain valid data from which to extract the token
 */
internal class GooglePayIllegalResponseException
constructor(message: String? = null, cause: Exception? = null) : Exception(message, cause)
