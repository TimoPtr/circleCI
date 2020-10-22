/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.view.View
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter

/**
 * Allows updating layout height via databinding, instead of using `android:layout_height` attribute,
 * which has to be set immediately when the view is inflated.
 *
 * Example:
 *
 * <Button
 *      android:layout_width="match_parent"
 *      android:layout_height="wrap_content"
 *      bindHeight="@{viewModel.bindCustomHeight(context)}" />
 */
@Keep
@BindingAdapter("bindHeight")
fun View.updateLayoutHeight(height: Int?) {
    height?.let {
        layoutParams.height = it
        this.layoutParams = layoutParams
    }
}
