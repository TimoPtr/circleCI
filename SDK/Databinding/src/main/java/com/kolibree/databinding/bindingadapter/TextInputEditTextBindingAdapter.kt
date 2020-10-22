/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.kolibree.android.app.ui.input.hideSoftInput

@Keep
@BindingAdapter("onImeAction")
fun TextInputEditText.onImeAction(imeActionHandler: OnEditorActionListener) {
    setOnEditorActionListener { _, actionId, _ ->
        val consumed = imeActionHandler.onEditorAction(actionId)
        if (consumed) {
            hideSoftInput()
            clearFocus()
        }
        consumed
    }
}

@Keep
interface OnEditorActionListener {

    fun onEditorAction(actionId: Int): Boolean
}
