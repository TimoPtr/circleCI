/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.validator

internal object BrushNameValidator {
    fun isValid(name: String?): Boolean = when {
        name.isNullOrBlank() -> false
        else -> {
            name.toByteArray().size <= TOOTHBRUSH_NAME_MAX_LENGTH
        }
    }
}

private const val TOOTHBRUSH_NAME_MAX_LENGTH = 19
