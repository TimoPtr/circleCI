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
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@Keep
@BindingAdapter("error")
fun TextInputLayout.bindErrorMessage(previousError: String?, error: String?) {
    when {
        previousError == error -> {
            /* no-op */
        }
        error.isNullOrBlank() -> {
            isErrorEnabled = false
            this.error = null
        }
        else -> {
            isErrorEnabled = true
            this.error = error
        }
    }
}

@Keep
@BindingAdapter("error")
fun TextInputLayout.bindErrorMessage(@StringRes previousError: Int?, @StringRes error: Int?) {
    when {
        previousError == error -> {
            /* no-op */
        }
        error == null -> {
            isErrorEnabled = false
            this.error = null
        }
        else -> {
            isErrorEnabled = true
            this.error = resources.getString(error)
        }
    }
}
