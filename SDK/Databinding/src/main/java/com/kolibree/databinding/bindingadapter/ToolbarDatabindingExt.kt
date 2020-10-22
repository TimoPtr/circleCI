/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import com.kolibree.android.failearly.FailEarly

@Keep
@BindingAdapter("enableBackNavigation")
fun Toolbar.enableBackNavigation(enable: Boolean) {
    if (enable) {
        withValidActivity { activity ->
            setNavigationOnClickListener { activity.onBackPressed() }
        }
    } else setNavigationOnClickListener { /* no-op */ }
}

@Keep
@BindingAdapter("onBackNavigationClick")
fun Toolbar.setOnBackNavigationListener(onClickListener: View.OnClickListener) {
    setNavigationOnClickListener(onClickListener)
}

private inline fun Toolbar.withValidActivity(execute: (Activity) -> Unit) {
    with(context) {
        when {
            this is Activity -> execute(this)
            (this as? ContextWrapper)?.baseContext is Activity ->
                execute(((this as ContextWrapper).baseContext as Activity))
            else -> FailEarly.fail("Back navigation could not be executed!")
        }
    }
}
