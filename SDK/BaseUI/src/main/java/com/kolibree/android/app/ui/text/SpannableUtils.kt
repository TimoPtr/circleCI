/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.text

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.failearly.FailEarly

@Keep
@Suppress("LongMethod")
fun highlightString(fullText: String, highlight: String): Spannable {
    val startIndex = fullText.indexOf(highlight, ignoreCase = true)
    val spannable = SpannableStringBuilder(fullText)

    if (startIndex >= 0) {
        val endIndex = startIndex + highlight.length

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
    } else {
        FailEarly.fail(
            "Highlight string `$highlight` is not present in full string `$fullText`, " +
                "please check your translations!"
        )
    }
    return spannable
}

@Keep
@KolibreeExperimental
fun iconCenteredSpan(context: Context, @DrawableRes iconResId: Int): ImageSpan =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ImageSpan(context, iconResId, DynamicDrawableSpan.ALIGN_CENTER)
    } else {
        VerticalImageSpan(context, iconResId)
    }
