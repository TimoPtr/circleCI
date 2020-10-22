/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import org.jetbrains.uast.UField

internal fun UField.declarationContainsKeyword(keyword: String) = try {
    text.contains(" $keyword ")
} catch (e: RuntimeException) {
    false
}
