/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.kolibree.android.guidedbrushing.tips.databinding.ItemGuidedBrushingTipBinding

internal class GuidedBrushingTipsAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val items = mutableListOf<BrushingTipsData>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemGuidedBrushingTipBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, items[position])
    }

    override fun getItemCount() = items.size

    fun refresh(newItems: List<BrushingTipsData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

internal class ViewHolder(
    private val binding: ItemGuidedBrushingTipBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, item: BrushingTipsData) {
        binding.item = item
        binding.position = position
        binding.executePendingBindings()
    }
}

@BindingAdapter("tipsData")
internal fun ViewPager2.bindTipsData(data: List<BrushingTipsData>) {
    val currentAdapter = adapter
    if (currentAdapter is GuidedBrushingTipsAdapter) {
        currentAdapter.refresh(data)
    }
}
