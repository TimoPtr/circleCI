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
import android.view.Gravity
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID

@Keep
class KolibreeDialogHeadline(
    private val context: Context
) : KolibreeDialogBuilder<HeadlineAttrs> {

    var headlineText: String? = null
        private set

    @StringRes
    var headlineTextId: Int = UNDEFINED_RESOURCE_ID
        private set

    var gravity: Int = Gravity.START
        private set

    /**
     * Sets the image to the specified drawable
     */
    fun text(headlineText: String) {
        this.headlineText = headlineText
    }

    /**
     * Sets the image to the specified drawable resource
     */
    fun text(@StringRes headlineTextId: Int) {
        this.headlineTextId = headlineTextId
    }

    fun gravity(gravity: Int) {
        this.gravity = gravity
    }

    override fun build(): HeadlineAttrs {
        return HeadlineAttrs(
            if (headlineTextId != UNDEFINED_RESOURCE_ID) context.getString(
                headlineTextId
            ) else headlineText, gravity
        )
    }
}

@Keep
data class HeadlineAttrs(val text: String?, val gravity: Int)
