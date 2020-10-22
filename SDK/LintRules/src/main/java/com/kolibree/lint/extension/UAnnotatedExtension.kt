/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import org.jetbrains.uast.UAnnotated

internal fun UAnnotated.hasKeep(): Boolean {
    if (annotations.isEmpty()) {
        return false
    }

    return annotations.any { annotation -> annotation.qualifiedName == "androidx.annotation.Keep" }
}
