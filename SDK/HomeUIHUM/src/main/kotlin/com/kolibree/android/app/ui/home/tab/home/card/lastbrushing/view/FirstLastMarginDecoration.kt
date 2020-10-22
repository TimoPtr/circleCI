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
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.homeui.hum.R

internal class FirstLastMarginDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val marginLarge = context.resources.getDimensionPixelSize(R.dimen.dot_trip)
    private val marginSmall = context.resources.getDimensionPixelSize(R.dimen.dot_half)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition: Int = parent.getChildAdapterPosition(view)
        if (itemPosition == RecyclerView.NO_POSITION) return

        outRect.left = marginSmall
        outRect.right = marginSmall

        if (itemPosition == 0) {
            outRect.left = marginLarge
        }

        val adapter = parent.adapter
        if (adapter != null && itemPosition == adapter.itemCount - 1) {
            outRect.right = marginLarge
        }
    }
}
