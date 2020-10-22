/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.snackbar

import android.graphics.drawable.InsetDrawable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.failearly.FailEarly

@Keep
@CheckResult(suggest = "#show()")
fun snackbar(mainView: View, lambda: KolibreeSnackbarDsl.() -> Unit): Snackbar =
    KolibreeSnackbarDsl(mainView).apply(lambda).build()

@Keep
class KolibreeSnackbarDsl(private val mainView: View) {
    @BaseTransientBottomBar.Duration private var duration = Snackbar.LENGTH_SHORT
    @StringRes private var messageRes: Int = 0
    private var message: String? = null
    @DrawableRes private var iconRes: Int = 0
    @StringRes private var actionTitleRes = 0
    private var action: () -> Unit = {}
    private var anchorView: View? = null

    fun action(@StringRes title: Int, action: () -> Unit = {}) {
        this.actionTitleRes = title
        this.action = action
    }

    fun duration(@BaseTransientBottomBar.Duration duration: Int) {
        this.duration = duration
    }

    fun message(message: String) {
        if (this.messageRes != 0) FailEarly.fail("Use either message or message resource")
        this.message = message
    }

    fun message(@StringRes message: Int) {
        if (this.message != null) FailEarly.fail("Use either message or message resource")
        this.messageRes = message
    }

    fun icon(@DrawableRes icon: Int) {
        this.iconRes = icon
    }

    fun anchor(anchorView: View) {
        this.anchorView = anchorView
    }

    fun build(): Snackbar {
        validate()

        val snackbar = message?.let { Snackbar.make(mainView, it, duration) }
            ?: Snackbar.make(mainView, messageRes, duration)
        if (anchorView != null) {
            snackbar.anchorView = anchorView
        }
        if (actionTitleRes != 0) {
            snackbar.setAction(actionTitleRes) {
                action()
            }
        }
        if (iconRes != 0) {
            addIcon(snackbar)
        }
        return snackbar
    }

    private fun addIcon(snackbar: Snackbar) {
        val snackbarLayout = snackbar.view
        val textView = snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        val iconDrawable = ContextCompat.getDrawable(mainView.context, iconRes)
        val bottomInset = resources().getDimensionPixelOffset(R.dimen.dot_half)
        val centeredDrawable = InsetDrawable(iconDrawable, 0, 0, 0, bottomInset)
        textView.setCompoundDrawablesWithIntrinsicBounds(centeredDrawable, null, null, null)
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.compoundDrawablePadding = resources().getDimensionPixelOffset(R.dimen.dot)
    }

    private fun resources() = mainView.context.resources

    private fun validate() {
        if (messageRes == 0 && message == null)
            throw IllegalStateException("Snackbar needs to have message set!")
    }
}
