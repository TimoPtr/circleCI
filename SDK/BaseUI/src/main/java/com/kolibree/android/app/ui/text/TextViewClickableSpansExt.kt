/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.text

import android.graphics.Typeface
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

@Keep
fun TextView.addLink(link: ClickableStringResource) = addLink(
    ClickableText(
        text = context.resources.getString(link.stringResource),
        textPaintModifiers = link.textPaintModifiers,
        onClickListener = link.onClickListener
    )
)

@Keep
fun TextView.addLink(link: ClickableText) {
    val startIndexOfLink = this.text.toString().indexOf(link.text)
    if (startIndexOfLink != -1 && startIndexOfLink + link.text.length <= this.text.length) {
        val spannableString = SpannableString(this.text)

        applyClickableSpan(spannableString, link, startIndexOfLink)
        link.textPaintModifiers?.isBoldText?.let {
            applyBoldSpan(spannableString, link, startIndexOfLink)
        }

        this.movementMethod = LinkMovementMethod.getInstance()
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    } else {
        FailEarly.fail("Link span could not be applied")
    }
}

private fun applyBoldSpan(
    spannableString: SpannableString,
    text: ClickableText,
    startIndexOfBold: Int
) {
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        startIndexOfBold,
        startIndexOfBold + text.text.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

private fun applyClickableSpan(
    spannableString: SpannableString,
    link: ClickableText,
    startIndexOfLink: Int
) {
    spannableString.setSpan(
        ClickableLinkSpan(link),
        startIndexOfLink,
        startIndexOfLink + link.text.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

private class ClickableLinkSpan(private val link: ClickableText) : ClickableSpan() {

    override fun onClick(view: View) {
        Selection.setSelection((view as TextView).text as Spannable, 0)
        view.invalidate()
        link.onClickListener.onClick(view)
    }

    override fun updateDrawState(ds: TextPaint) {
        link.textPaintModifiers?.let { modifiers ->
            modifiers.bgColor?.let { ds.bgColor = it }
            modifiers.baselineShift?.let { ds.baselineShift = it }
            modifiers.color?.let { ds.color = it }
            modifiers.linkColor?.let { ds.linkColor = it }
            modifiers.isUnderlineText?.let { ds.isUnderlineText = it }
            modifiers.density?.let { ds.density = it }
        }
    }
}
