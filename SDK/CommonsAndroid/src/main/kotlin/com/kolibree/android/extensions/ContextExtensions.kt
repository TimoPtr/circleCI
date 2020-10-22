/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Keep

@Keep
@ColorInt
fun Context.getColorFromAttr(@AttrRes attrColor: Int): Int = resolveAttributeData(attrColor)

@Keep
@AnyRes
fun Context.resolveAttribute(@AttrRes attr: Int): Int = resolveTypedValue(attr).resourceId

@Keep
fun Context.resolveAttributeData(@AttrRes attr: Int): Int = resolveTypedValue(attr).data

@Keep
fun Context.appName(): String = applicationInfo.loadLabel(packageManager).toString()

private fun Context.resolveTypedValue(@AttrRes attr: Int): TypedValue {
    return TypedValue().also { theme.resolveAttribute(attr, it, true) }
}
