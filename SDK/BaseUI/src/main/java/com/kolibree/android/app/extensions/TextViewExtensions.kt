/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

@file:JvmName("TextViewUtils")

package com.kolibree.android.app.extensions

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.Keep
import androidx.core.content.ContextCompat

@Keep
fun TextView.setUnderlineText(fullText: String, @ColorRes textColor: Int) {
    val spannable = SpannableString(fullText)
    val endInx = fullText.length
    val color = ContextCompat.getColor(context!!, textColor)
    spannable.setSpan(ForegroundColorSpan(color), 0, endInx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannable.setSpan(UnderlineSpan(), 0, endInx, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    text = spannable
}

@Keep
@SuppressWarnings("LongMethod")
fun TextView.setPartUnderlineText(
    fullText: String,
    underlinePart: String,
    @ColorRes underlineTextColor: Int,
    onClick: () -> Unit = {}
) {
    val spannable = SpannableString(fullText)
    val startInx = fullText.indexOf(underlinePart)
    if (startInx >= 0) {
        val endInx = fullText.length
        val color = ContextCompat.getColor(context!!, underlineTextColor)
        spannable.setSpan(UnderlineSpan(), startInx, endInx, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.setColor(color)
            }
        }
        spannable.setSpan(clickableSpan, startInx, endInx, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        text = spannable
        movementMethod = LinkMovementMethod.getInstance()
    }
}

@Keep
fun TextView.setPartColoredText(
    fullText: String,
    coloredText: String,
    @ColorRes textColor: Int
) {
    val spannable = SpannableString(fullText)
    val startInx = fullText.indexOf(coloredText)
    if (startInx >= 0) {
        val endInx = startInx + coloredText.length
        val color = ContextCompat.getColor(context!!, textColor)
        spannable.setSpan(ForegroundColorSpan(color), startInx, endInx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startInx, endInx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = spannable
    }
}
