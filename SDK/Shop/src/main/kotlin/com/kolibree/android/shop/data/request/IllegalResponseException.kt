/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request

internal class IllegalResponseException : RuntimeException {

    constructor(reason: Throwable) : super(reason)

    constructor(message: String) : super(message)
}
