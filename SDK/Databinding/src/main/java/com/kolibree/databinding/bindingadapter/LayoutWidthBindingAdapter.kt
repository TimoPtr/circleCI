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
 * Allows updating layout width via databinding, instead of using `android:layout_width` attribute,
 * which has to be set immediately when the view is inflated.
 *
 * Example:
 *
 * <Button
 *      android:layout_width="match_parent"
 *      android:layout_height="wrap_content"
 *      bindWidth="@{viewModel.bindCustomHeight(context)}" />
 */
@Keep
@BindingAdapter("bindWidth")
fun View.updateLayoutWidth(width: Int?) {
    width?.let {
        layoutParams.width = it
        this.layoutParams = layoutParams
    }
}
