/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.bindingadapter

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter

@BindingAdapter("entries", "itemLayout", requireAll = true)
internal fun AutoCompleteTextView.bindAdapter(entries: Array<Any>, @LayoutRes itemLayout: Int) {
    val adapter = ArrayAdapter(context, itemLayout, entries)
    setAdapter(adapter)
}
