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
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID

@Keep
class KolibreeDialogDrawable(
    private val context: Context
) : KolibreeDialogBuilder<Drawable?> {

    var drawable: Drawable? = null
        private set

    @DrawableRes
    var drawableId: Int = UNDEFINED_RESOURCE_ID
        private set

    var scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
        private set

    @ColorInt
    var tintColor: Int = UNDEFINED_RESOURCE_ID
        private set

    /**
     * Sets the image to the specified drawable
     */
    fun drawable(featureImage: Drawable) {
        this.drawable = featureImage.mutate()
    }

    /**
     * Sets the image to the specified drawable resource
     */
    fun drawable(@DrawableRes drawableId: Int) {
        this.drawableId = drawableId
    }

    /**
     * Sets the image tint to the specified color value
     */
    fun tint(@ColorInt tintColor: Int) {
        this.tintColor = tintColor
    }

    /**
     * Sets the image tint color to the specified drawable resource
     */
    fun scaleType(scaleType: ImageView.ScaleType) {
        this.scaleType = scaleType
    }

    override fun build(): Drawable? =
        (drawable ?: ContextCompat.getDrawable(context, drawableId))?.apply {
            tintColor.takeIf { it != UNDEFINED_RESOURCE_ID }?.also { setTint(it) }
        }
}
