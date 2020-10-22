/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import com.intellij.psi.PsiNamedElement
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.kotlin.KotlinUClass

internal fun UClass.isExposed(): Boolean =
    (visibility == UastVisibility.PUBLIC || visibility == UastVisibility.PROTECTED) &&
        (this !is KotlinUClass || !isInternal(this, this.name))

internal fun UClass.isParentInternal(): Boolean =
    parent != null && isInternal(parent, (parent as PsiNamedElement).name)

internal fun UClass.parentHasKeep(): Boolean =
    (uastParent as? UAnnotated)?.hasKeep() ?: false

internal fun UClass.isCompanionObject(): Boolean {
    if (this !is KotlinUClass) {
        return false
    }

    if (originalElement == null) {
        return false
    }

    return originalElement!!.text.contains("companion object")
}

internal fun UClass.isDaggerRelated(): Boolean {
    if (annotations.isEmpty()) {
        return false
    }

    return annotations.any { annotation -> annotation.qualifiedName?.startsWith("dagger.") ?: false }
}
