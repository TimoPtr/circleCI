/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kolibree.databinding.BR
import com.kolibree.databinding.CollectionBindingListener
import com.kolibree.databinding.OnItemClickListener
import com.kolibree.databinding.OnValueToggledListener
import com.kolibree.databinding.R

// Based on https://medium.com/google-developers/android-data-binding-list-tricks-ef3d5630555e
internal fun <T> LinearLayout.bindCollectionOfEntries(
    entries: List<T>?,
    @LayoutRes layoutId: Int?,
    listener: CollectionBindingListener?
) {
    if (entries == null || listener == null) return

    if (childCount == 0) {
        val inflater = LayoutInflater.from(context)
        for (entry in entries) {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                inflater,
                layoutId ?: R.layout.item_default_collection_binding,
                this,
                true
            )
            binding.setVariable(BR.data, entry)
            binding.setVariable(BR.listener, listener)
        }
    } else {
        for (childIndex in 0 until childCount) {
            val binding = DataBindingUtil.getBinding<ViewDataBinding>(getChildAt(childIndex))
            binding?.setVariable(BR.data, entries[childIndex])
            binding?.setVariable(BR.listener, listener)
        }
    }
}

@Keep
@BindingAdapter(value = ["entries", "layout", "onItemClick"], requireAll = false)
fun <T> LinearLayout.bindCollectionOfClickableEntries(
    entries: List<T>?,
    @LayoutRes layoutId: Int?,
    listener: OnItemClickListener<T>?
) = bindCollectionOfEntries(entries, layoutId, listener)

@Keep
@BindingAdapter(value = ["entries", "layout", "onItemToggled"], requireAll = false)
fun <T> LinearLayout.bindCollectionOfToggleableEntries(
    entries: List<T>?,
    @LayoutRes layoutId: Int?,
    listener: OnValueToggledListener<T>?
) = bindCollectionOfEntries(entries, layoutId, listener)
