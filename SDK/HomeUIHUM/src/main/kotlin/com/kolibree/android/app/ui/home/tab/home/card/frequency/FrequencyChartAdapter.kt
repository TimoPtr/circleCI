/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.kolibree.android.homeui.hum.databinding.ItemFrequencyChartViewBinding

internal class FrequencyChartAdapter : RecyclerView.Adapter<FrequencyChartAdapter.ViewHolder>() {

    private val items = mutableListOf<FrequencyChartViewState>()
    var interaction: FrequencyChartInteraction? = null

    class ViewHolder(
        private val binding: ItemFrequencyChartViewBinding,
        private val interaction: FrequencyChartInteraction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewState: FrequencyChartViewState) {
            binding.viewState = viewState
            binding.interaction = interaction
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFrequencyChartViewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, interaction)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun refresh(newItems: List<FrequencyChartViewState>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

@Suppress("LongMethod", "LongParameterList")
@BindingAdapter(
    value = ["frequencyAdapter", "frequencyItems", "frequencyCurrentItem", "frequencyInteraction"],
    requireAll = true
)
internal fun ViewPager2.bindFrequencyAdapter(
    frequencyAdapter: FrequencyChartAdapter?,
    items: List<FrequencyChartViewState>?,
    currentItem: Int?,
    interaction: FrequencyChartInteraction?
) {
    isUserInputEnabled = false
    if (adapter == null) {
        adapter = frequencyAdapter
    }

    val adapterWasEmpty = adapter?.itemCount == 0
    val viewPagerAdapter = adapter

    if (viewPagerAdapter is FrequencyChartAdapter) {
        viewPagerAdapter.interaction = interaction
        items?.let { viewPagerAdapter.refresh(it) }
    }

    if (currentItem != null) {
        val withSmoothScroll = !adapterWasEmpty && currentItem != this.currentItem
        post { setCurrentItem(currentItem, withSmoothScroll) }
    }
}
