/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import org.jetbrains.uast.UDeclaration

internal fun UDeclaration.hasKeep(): Boolean {
    if (annotations.isEmpty()) {
        return false
    }

    return annotations.any { annotation -> annotation.qualifiedName == "androidx.annotation.Keep" }
}

internal fun UDeclaration.hasVisibleForApp(): Boolean {
    if (annotations.isEmpty()) {
        return false
    }

    return annotations.any { annotation -> annotation.qualifiedName == "com.kolibree.android.annotation.VisibleForApp" }
}
