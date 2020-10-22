/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.text

import androidx.annotation.ColorInt
import androidx.annotation.Keep

@Keep
data class TextPaintModifiers(
    @ColorInt val bgColor: Int? = null,
    val baselineShift: Int? = null,
    @ColorInt val color: Int? = null,
    @ColorInt val linkColor: Int? = null,
    val isUnderlineText: Boolean? = null,
    val isBoldText: Boolean? = null,
    val density: Float? = null
) {

    class Builder {
        @ColorInt
        private var bgColor: Int? = null
        private var baselineShift: Int? = null
        @ColorInt
        private var color: Int? = null
        @ColorInt
        private var linkColor: Int? = null
        private var isUnderlineText: Boolean? = null
        private var isBoldText: Boolean? = null
        private var density: Float? = null

        fun withBgColor(@ColorInt bgColor: Int): Builder {
            this.bgColor = bgColor
            return this
        }

        fun withBaselineShift(baselineShift: Int): Builder {
            this.baselineShift = baselineShift
            return this
        }

        fun withColor(@ColorInt color: Int): Builder {
            this.color = color
            return this
        }

        fun withLinkColor(@ColorInt linkColor: Int): Builder {
            this.linkColor = linkColor
            return this
        }

        fun withUnderlineText(isUnderlineText: Boolean): Builder {
            this.isUnderlineText = isUnderlineText
            return this
        }

        fun withDensity(density: Float): Builder {
            this.density = density
            return this
        }

        fun withBoldText(boldText: Boolean): Builder {
            this.isBoldText = boldText
            return this
        }

        fun build() = TextPaintModifiers(
            bgColor = this.bgColor,
            baselineShift = this.baselineShift,
            color = this.color,
            linkColor = this.linkColor,
            isUnderlineText = this.isUnderlineText,
            isBoldText = this.isBoldText,
            density = this.density
        )
    }
}
