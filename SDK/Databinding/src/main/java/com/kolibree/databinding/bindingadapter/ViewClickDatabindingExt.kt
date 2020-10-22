/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.view.View
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.ui.input.hideSoftInput
import com.kolibree.android.extensions.setOnDebouncedClickListener
import org.threeten.bp.Duration

/**
 * !!! Warning !!! If you use android:* in the value of binding for View,
 * you should only have one parameter for the lambda.
 * Otherwise you will get a missing setter error.
 *
 * Take a look to bindOnDebouncedClickListener and bindOnDebouncedClickListenerWithValue
 * (databinding version 3.5.2)
 */

/**
 * Every click listeners bound by our code will be automatically debounced.
 */
@Keep
@BindingAdapter("android:onClick")
fun View.bindOnDebouncedClickListener(
    onClickListener: View.OnClickListener?
) = setOnDebouncedClickListener { v -> onClickListener?.onClick(v) }

@Keep
@BindingAdapter(value = ["debounceDuration", "onClickDebounce"], requireAll = false)
fun View.bindOnDebouncedClickListenerWithValue(
    debounceDuration: Duration?,
    onClickDebounce: View.OnClickListener?
) = setOnDebouncedClickListener(
    debounceDuration
) { v -> onClickDebounce?.onClick(v) }

/**
 * Long click listener has to return boolean - otherwise databinding compiler crashes with stack overflow.
 * This adapter contains a way to wrap an arbitrary function in a correct way.
 */
@Keep
@BindingAdapter("onLongClickFunction")
inline fun View.setOnLongClickFunction(crossinline onLongClickFunction: () -> Unit) {
    setOnLongClickListener {
        onLongClickFunction()
        true
    }
}

/**
 * Close the opened keyboard, if there is one, and follow the click to the debounce click function
 */
@Keep
@BindingAdapter("onClickCloseInput")
fun View.setOnClickCloseInputListener(onClickFunction: () -> Unit) {
    setOnDebouncedClickListener {
        hideSoftInput()
        onClickFunction()
    }
}
