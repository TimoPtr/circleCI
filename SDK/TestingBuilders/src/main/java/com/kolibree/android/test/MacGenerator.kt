/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
object MacGenerator {
    private val allowedChars = ('A'..'F') + (0..9)
    private const val SEPARATOR = ":"

    fun generate(): String = (1..6)
        .map { "${allowedChars.random()}${allowedChars.random()}" }
        .joinToString(SEPARATOR)
}
