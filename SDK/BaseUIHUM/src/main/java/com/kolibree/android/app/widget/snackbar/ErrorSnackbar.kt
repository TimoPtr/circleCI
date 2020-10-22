/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.snackbar

import android.content.res.Resources
import android.view.View
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.app.Error
import com.kolibree.android.app.Error.ErrorStyle
import com.kolibree.android.app.Error.ErrorStyle.AutoDismiss
import com.kolibree.android.app.Error.ErrorStyle.Indefinite
import com.kolibree.android.baseui.hum.R

@Keep
fun View.showErrorSnackbar(error: Error): Snackbar {
    return snackbar(this) {

        message(error, resources)
        duration(error.style)
        action(error)
    }.apply { show() }
}

private fun KolibreeSnackbarDsl.action(error: Error) {
    if (error.buttonTextId != null) {
        action(error.buttonTextId)
    } else if (error.style is Indefinite) {
        action(R.string.ok)
    }
}

private fun KolibreeSnackbarDsl.message(error: Error, resources: Resources) =
    message(
        when {
            error.messageId != null -> resources.getString(error.messageId)
            error.message != null -> error.message
            error.exception != null -> error.exception.message
                ?: resources.getString(R.string.something_went_wrong)
            else -> resources.getString(R.string.something_went_wrong)
        }
    )

private fun KolibreeSnackbarDsl.duration(errorStyle: ErrorStyle) =
    duration(
        when (errorStyle) {
            Indefinite -> Snackbar.LENGTH_INDEFINITE
            AutoDismiss -> Snackbar.LENGTH_SHORT
        }
    )
