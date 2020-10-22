/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.BrushingCardData
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.BrushingTopSectionInteraction
import com.kolibree.android.homeui.hum.databinding.ItemBrushingDataBinding

internal class BrushingTopSectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), BrushingCardDataItemClick {

    private val activitiesAdapter = BrushingsTopSectionAdapter(this)

    private var interaction: BrushingTopSectionInteraction? = null

    init {
        adapter = activitiesAdapter
        isNestedScrollingEnabled = false
        addItemDecoration(FirstLastMarginDecoration(context))
    }

    override fun onClick(position: Int, item: BrushingCardData) {
        interaction?.onTopBrushingItemClick(position, item)
    }

    fun refresh(items: List<BrushingCardData>) {
        activitiesAdapter.refresh(items)
    }

    fun interaction(interaction: BrushingTopSectionInteraction) {
        this.interaction = interaction
    }
}

internal interface BrushingCardDataItemClick {
    fun onClick(position: Int, item: BrushingCardData)
}

internal class BrushingsTopSectionAdapter(
    private val itemClick: BrushingCardDataItemClick
) : RecyclerView.Adapter<BrushingsTopSectionAdapter.ViewHolder>() {

    private val items = mutableListOf<BrushingCardData>()

    class ViewHolder(
        private val binding: ItemBrushingDataBinding,
        private val itemClick: BrushingCardDataItemClick
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, item: BrushingCardData) {
            binding.item = item
            binding.itemClick = itemClick
            binding.position = position
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBrushingDataBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, items[position])
    }

    override fun getItemCount() = items.size

    fun refresh(newItems: List<BrushingCardData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

@BindingAdapter("cardItems")
internal fun BrushingTopSectionView.bindCardItems(items: List<BrushingCardData>) {
    refresh(items)
}

@BindingAdapter("interaction")
internal fun BrushingTopSectionView.bindInteraction(interaction: BrushingTopSectionInteraction) {
    interaction(interaction)
}
