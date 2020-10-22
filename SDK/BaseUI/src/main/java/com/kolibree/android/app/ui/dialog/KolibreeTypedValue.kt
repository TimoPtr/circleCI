/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.StyleRes
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.failearly.FailEarly

@KolibreeExperimental
private sealed class KolibreeTypedValue<T>(
    private val types: List<Int>
) {

    fun valueFor(context: Context, @AttrRes resId: Int): T {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        if (typedValue.type == TypedValue.TYPE_NULL) {
            FailEarly.fail("Resource not found: ${context.theme.resources.getResourceName(resId)}")
        } else if (!types.contains(typedValue.type)) {
            FailEarly.fail("Incorrect type")
        }
        return typedValue.resolveValue(context)
    }

    abstract fun TypedValue.resolveValue(context: Context): T

    internal class Dimension : KolibreeTypedValue<kotlin.Float>(listOf(TypedValue.TYPE_DIMENSION)) {

        override fun TypedValue.resolveValue(context: Context): kotlin.Float =
            getDimension(context.resources.displayMetrics)
    }

    internal class ColorInt : KolibreeTypedValue<Int>(
        listOf(
            TypedValue.TYPE_INT_COLOR_ARGB8,
            TypedValue.TYPE_INT_COLOR_RGB8,
            TypedValue.TYPE_INT_COLOR_ARGB4,
            TypedValue.TYPE_INT_COLOR_RGB4
        )
    ) {
        override fun TypedValue.resolveValue(context: Context): Int =
            data
    }

    internal class Boolean :
        KolibreeTypedValue<kotlin.Boolean>(listOf(TypedValue.TYPE_INT_BOOLEAN)) {
        override fun TypedValue.resolveValue(context: Context): kotlin.Boolean =
            data != 0
    }

    internal class Float : KolibreeTypedValue<kotlin.Float>(listOf(TypedValue.TYPE_FLOAT)) {
        override fun TypedValue.resolveValue(context: Context): kotlin.Float =
            float
    }

    internal class Style : KolibreeTypedValue<Int>(listOf(TypedValue.TYPE_REFERENCE)) {
        override fun TypedValue.resolveValue(context: Context): Int = resourceId
    }
}

internal fun dimensionFromAttribute(context: Context, @AttrRes resId: Int): Float =
    KolibreeTypedValue.Dimension().valueFor(context, resId)

internal fun dimensionIntFromAttribute(context: Context, @AttrRes resId: Int): Int =
    dimensionFromAttribute(context, resId).toInt()

@Keep
@ColorInt
fun colorIntFromAttribute(context: Context, @AttrRes resId: Int): Int =
    KolibreeTypedValue.ColorInt().valueFor(context, resId)

@Keep
fun booleanFromAttribute(context: Context, @AttrRes resId: Int): Boolean =
    KolibreeTypedValue.Boolean().valueFor(context, resId)

@Keep
fun floatFromAttribute(context: Context, @AttrRes resId: Int): Float =
    KolibreeTypedValue.Float().valueFor(context, resId)

@Keep
@StyleRes
fun styleResFromAttribute(context: Context, @AttrRes resId: Int): Int =
    KolibreeTypedValue.Style().valueFor(context, resId)
