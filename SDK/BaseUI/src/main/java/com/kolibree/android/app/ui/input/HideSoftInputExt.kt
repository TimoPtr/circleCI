/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.input

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

@Keep
fun View.hideSoftInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
        if (windowToken != null) it.hideSoftInputFromWindow(windowToken, 0)
    } ?: FailEarly.fail("INPUT_METHOD_SERVICE could not be fetched from ${javaClass.canonicalName}")
}
