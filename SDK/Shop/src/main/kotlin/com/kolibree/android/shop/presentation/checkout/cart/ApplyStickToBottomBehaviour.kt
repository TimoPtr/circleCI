/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException
import kotlin.math.max

/**
 * Class which apply 'stick to bottom' effect.
 * The only requirement is that RecyclerView has to be using [LinearLayoutManager] as layout manager
 **/
internal class ApplyStickToBottomBehaviour(
    private val recycler: RecyclerView,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver, ViewTreeObserver.OnPreDrawListener {

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        recycler.viewTreeObserver.addOnPreDrawListener(this)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        recycler.viewTreeObserver.removeOnPreDrawListener(this)
    }

    override fun onPreDraw(): Boolean {
        stickToBottomIfNeeded()
        return true
    }

    private fun stickToBottomIfNeeded() {
        val items = itemCount()

        if (hasTheSameStickToBottomItemDecorator(recycler, items)) {
            return
        }

        if (isLastItemCompletelyVisible() && isLayoutManagerNotEmpty()) {
            calculateAndAddStickToBottomItemDecoration()
        } else if (!isLastItemVisible()) {
            removeStickToBottomItemDecoration()
        }
    }

    private fun itemCount() = recycler.adapter?.itemCount ?: 0

    private fun layoutManager(): LinearLayoutManager {
        if (recycler.layoutManager !is LinearLayoutManager) {
            throw IllegalStateException("StickToBottom is only supported by LinearLayoutManager")
        }
        return recycler.layoutManager as LinearLayoutManager
    }

    private fun calculateAndAddStickToBottomItemDecoration() {
        val items = itemCount()

        val total = calcItemsHeightInsideRecyclerView()

        val recyclerInsideHeight =
            recycler.height - recycler.paddingTop - recycler.paddingBottom
        val diff = recyclerInsideHeight - total

        val lastItemTopMargin = max(0, diff)

        removeStickToBottomItemDecoration()

        recycler.addItemDecoration(StickToBottomItemDecoration(lastItemTopMargin, items))
    }

    private fun calcItemsHeightInsideRecyclerView(): Int {
        val layoutManager = layoutManager()

        var totalHeight = 0
        for (index in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(index)
            val height = child?.height ?: 0
            val marginParams = child?.layoutParams as? ViewGroup.MarginLayoutParams
            val marginTop = marginParams?.topMargin ?: 0
            val marginBottom = marginParams?.bottomMargin ?: 0

            val heightWithMargins = height + marginBottom + marginTop

            totalHeight += heightWithMargins
        }
        return totalHeight
    }

    private fun isLayoutManagerNotEmpty() = layoutManager().childCount > 0

    private fun hasTheSameStickToBottomItemDecorator(
        recycler: RecyclerView,
        itemCount: Int
    ): Boolean {
        for (index in 0 until recycler.itemDecorationCount) {
            val item = recycler.getItemDecorationAt(index)
            if (item is StickToBottomItemDecoration) {
                return item.itemCount == itemCount
            }
        }
        return false
    }

    private fun removeStickToBottomItemDecoration() {
        for (index in 0 until recycler.itemDecorationCount) {
            val item = recycler.getItemDecorationAt(index)
            if (item is StickToBottomItemDecoration) {
                recycler.removeItemDecorationAt(index)
                break
            }
        }
    }

    private fun isLastItemCompletelyVisible(): Boolean {
        val layoutManager = layoutManager()
        val pos: Int = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems: Int = itemCount()
        return pos >= numItems - 1
    }

    private fun isLastItemVisible(): Boolean {
        val layoutManager = layoutManager()
        val pos: Int = layoutManager.findLastVisibleItemPosition()
        val numItems: Int = itemCount()
        return pos >= numItems - 1
    }
}

internal class StickToBottomItemDecoration(private val lastTopMarginPixels: Int, val itemCount: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        if (itemPosition == RecyclerView.NO_POSITION) {
            return
        }

        if (itemPosition == itemCount - 1) {
            outRect.top = lastTopMarginPixels
        }
    }
}
