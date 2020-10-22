/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.toUElement

internal fun UParameter.toKolibreeClass(): UClass? {
    val clazz = PsiTypesUtil.getPsiClass(type).toUElement(UClass::class.java)
    return if (clazz?.qualifiedName?.startsWith("com.kolibree") == false) null
    else clazz
}
