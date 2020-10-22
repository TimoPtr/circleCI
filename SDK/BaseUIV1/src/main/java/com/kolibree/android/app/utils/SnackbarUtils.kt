/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.baseui.v1.R

class SnackbarUtils private constructor() {
    companion object {
        @JvmStatic
        @JvmOverloads
        @SuppressLint("WrongConstant")
        fun create(
            context: Context,
            view: View,
            message: String,
            @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
            @ColorRes backgroundColor: Int = 0,
            @ColorRes textColor: Int = 0
        ): Snackbar {
            val snackMessage = createMessage(textColor, message, context)

            val snackbar = Snackbar.make(view, snackMessage, duration)

            if (backgroundColor != 0)
                snackbar.view.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))

            return snackbar
        }

        @JvmStatic
        @SuppressLint("WrongConstant")
        fun create(
            context: Context,
            view: View,
            @StringRes messageResId: Int,
            @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
            @ColorRes backgroundColor: Int = 0,
            @ColorRes textColor: Int = 0
        ): Snackbar {
            return create(context, view, context.getString(messageResId), duration, backgroundColor, textColor)
        }

        @JvmStatic
        fun createError(
            context: Context,
            view: View,
            message: String
        ): Snackbar {
            return create(
                context, view, message,
                backgroundColor = R.color.welcome_validate_error,
                textColor = android.R.color.white
            )
        }

        @JvmStatic
        fun createError(
            context: Context,
            view: View,
            @StringRes messageResId: Int
        ): Snackbar {
            return createError(context, view, context.getString(messageResId))
        }

        private fun createMessage(@ColorRes textColor: Int, message: String, context: Context): CharSequence {
            if (textColor != 0) {
                val ssb = SpannableStringBuilder(message)
                val foregroundColor = ContextCompat.getColor(context, textColor)
                ssb.setSpan(
                    ForegroundColorSpan(foregroundColor), 0, message.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                return ssb
            }

            return message
        }
    }
}
