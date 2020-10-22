/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

@Keep
fun SpannableStringBuilder.setSpan(phrase: String, span: Any) {
    val startIndex = indexOf(phrase)
    val endIndex = startIndex + phrase.length
    if (endIndex <= startIndex) {
        FailEarly.fail("Unable to set span! Start index: $startIndex, end index: $endIndex")
        return
    }

    setSpan(span, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}
