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
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID
import com.kolibree.android.baseui.R

@Keep
sealed class KolibreeDialogButton<T>(
    protected val context: Context,
    @AttrRes val styleId: Int,
    val initialValue: T,
    private val actionDelegate: KolibreeActionDelegate<T> = KolibreeActionDelegateImpl()
) : KolibreeDialogCommon<MaterialButton, T>(initialValue),
    KolibreeActionDelegate<T> by actionDelegate {

    protected var icon: Drawable? = null

    class ContainedButton<T>(context: Context, initialValue: T) :
        KolibreeDialogButton<T>(context, R.attr.materialButtonStyle, initialValue)

    class OutlinedButton<T>(context: Context, initialValue: T) :
        KolibreeDialogButton<T>(context, R.attr.materialButtonOutlinedStyle, initialValue)

    @Keep
    sealed class KolibreeDialogButtonWithIcon<T>(context: Context, @AttrRes styleId: Int, initialValue: T) :
        KolibreeDialogButton<T>(context, styleId, initialValue) {

        fun icon(@DrawableRes iconId: Int) {
            icon = ContextCompat.getDrawable(context, iconId)
        }

        fun icon(icon: Drawable) {
            this.icon = icon
        }

        class IconContainedButton<T>(context: Context, initialValue: T) :
            KolibreeDialogButtonWithIcon<T>(context, R.attr.materialButtonIconStyle, initialValue)

        class IconOutlinedButton<T>(context: Context, initialValue: T) :
            KolibreeDialogButtonWithIcon<T>(context, R.attr.materialButtonOutlinedIconStyle, initialValue)
    }

    class TextButton<T>(
        context: Context,
        initialValue: T,
        styleId: Int = R.attr.materialButtonTextStyle
    ) : KolibreeDialogButton<T>(context, styleId, initialValue)

    override fun build() =
        MaterialButton(context, null, styleId).apply {
            title?.also { text = it }
            titleId.takeIf { it != UNDEFINED_RESOURCE_ID }?.also { text = context.getText(it) }
            setOnClickListener { action.invoke(currentValue) }
            this@KolibreeDialogButton.icon?.let { icon = it }
        }
}
