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
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
class KolibreeDialogInputText(
    private val context: Context
) : KolibreeDialogBuilder<KolibreeDialogInputText> {

    var hintText: String? = null
        private set

    var valueText: String? = null
        private set

    /**
     * Sets the hint of the input text to the specified string
     */
    fun hintText(hintText: String) {
        this.hintText = hintText
    }

    /**
     * Sets the hint of the input text to the specified string resourcew
     */
    fun hintText(@StringRes hintTextId: Int) {
        this.hintText = context.getString(hintTextId)
    }

    /**
     * Sets the initial value of the input text to the specified string
     */
    fun valueText(valueText: String) {
        this.valueText = valueText
    }

    override fun build(): KolibreeDialogInputText = this
}
