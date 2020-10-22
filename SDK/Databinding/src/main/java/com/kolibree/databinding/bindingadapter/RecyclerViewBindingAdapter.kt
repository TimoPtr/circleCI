/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.databinding.bindingadapter.RecyclerSnapType.LINEAR
import com.kolibree.databinding.bindingadapter.RecyclerSnapType.PAGER

@Keep
@BindingAdapter("snapItems")
fun RecyclerView.snapItems(type: RecyclerSnapType) {
    when (type) {
        LINEAR -> LinearSnapHelper().attachToRecyclerView(this)
        PAGER -> PagerSnapHelper().attachToRecyclerView(this)
    }
}

@Keep
enum class RecyclerSnapType {
    LINEAR, PAGER
}
