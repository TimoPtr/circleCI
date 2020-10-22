/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.validator

internal object EventNameValidator {

    fun isValidEventName(name: String): Boolean = isValidLength(name)

    private fun isValidLength(name: String) = name.length <= EVENT_NAME_MAX_LENGTH
}

private const val EVENT_NAME_MAX_LENGTH = 40
