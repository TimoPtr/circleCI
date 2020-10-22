/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import org.jetbrains.kotlin.asJava.classes.KtLightClassForFacade
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.kotlin.KotlinUClass
import org.jetbrains.uast.kotlin.declarations.KotlinUMethod

internal fun UMethod.isNotExposed(): Boolean =
    visibility != UastVisibility.PUBLIC &&
        visibility != UastVisibility.PROTECTED &&
        this !is KotlinUClass || isInternal(this, this.name)

internal fun UMethod.isRealFunction(): Boolean {
    return originalElement?.text?.contains("fun ") ?: false
}

internal fun UMethod.isExtensionFunction(): Boolean {
    if (this !is KotlinUMethod) {
        return false
    }

    return parent is KtLightClassForFacade
}
