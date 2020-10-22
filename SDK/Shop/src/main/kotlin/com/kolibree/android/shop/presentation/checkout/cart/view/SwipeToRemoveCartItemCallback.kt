/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.app.utils.swipe.SwipeToRemoveDirection
import com.kolibree.android.app.utils.swipe.SwipeToRemoveItemTouchCallback
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.shop.R

internal class SwipeToRemoveCartItemCallback(
    context: Context
) : SwipeToRemoveItemTouchCallback(
    context,
    SwipeToRemoveDirection.LEFT,
    R.drawable.ic_shop_trash,
    context.getColorFromAttr(R.attr.colorAccent)
) {
    override fun canSwipeItem(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Boolean {
        val position = viewHolder.adapterPosition
        val itemCount = recyclerView.adapter?.itemCount ?: 0
        return position < itemCount - 1
    }
}
