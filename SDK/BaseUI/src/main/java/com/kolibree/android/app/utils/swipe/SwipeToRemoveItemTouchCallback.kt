/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.app.utils.swipe

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@VisibleForApp
abstract class SwipeToRemoveItemTouchCallback(
    context: Context,
    direction: SwipeToRemoveDirection,
    @DrawableRes icon: Int,
    @ColorInt backgroundColor: Int
) : ItemTouchHelper.SimpleCallback(0, toSwipeDirection(direction)) {

    private val swipedPositionSubject = PublishSubject.create<Int>()
    private val viewBackground: ColorDrawable
    private val iconMargin: Int
    private val iconDrawable: Drawable

    init {
        iconMargin = context.resources.getDimensionPixelSize(R.dimen.dot_double)
        iconDrawable = ContextCompat.getDrawable(context, icon) ?: throw Resources.NotFoundException()
        viewBackground = ColorDrawable(backgroundColor)
    }

    abstract fun canSwipeItem(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Boolean

    fun positionSwipedObservable(): Observable<Int> {
        return swipedPositionSubject.hide()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        swipeDir: Int
    ) {
        swipedPositionSubject.onNext(viewHolder.adapterPosition)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        deltaX: Float,
        deltaY: Float,
        actionState: Int,
        isActive: Boolean
    ) {
        if (!canSwipeItem(recyclerView, viewHolder)) {
            return
        }

        val itemView = viewHolder.itemView

        setBackgroundBounds(itemView, deltaX)
        viewBackground.draw(canvas)

        setIconBounds(itemView, deltaX)
        iconDrawable.draw(canvas)

        super.onChildDraw(canvas, recyclerView, viewHolder, deltaX, deltaY, actionState, isActive)
    }

    private fun setIconBounds(itemView: View, deltaX: Float) {
        val drawableHeight = iconDrawable.intrinsicHeight
        val centerY = viewBackground.bounds.centerY()
        val y0 = centerY - drawableHeight / 2
        val y1 = centerY + drawableHeight / 2
        if (deltaX > 0) {
            val right = iconMargin + iconDrawable.intrinsicWidth
            iconDrawable.setBounds(iconMargin, y0, right, y1)
        } else {
            val left = itemView.right - iconMargin - iconDrawable.intrinsicWidth
            val right = itemView.right - iconMargin
            iconDrawable.setBounds(left, y0, right, y1)
        }
    }

    private fun setBackgroundBounds(itemView: View, deltaX: Float) = if (deltaX > 0) {
        viewBackground.setBounds(
            0,
            itemView.top,
            itemView.left + deltaX.toInt(),
            itemView.bottom
        )
    } else {
        viewBackground.setBounds(
            itemView.right + deltaX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
    }
}

private fun toSwipeDirection(direction: SwipeToRemoveDirection) = when (direction) {
    SwipeToRemoveDirection.LEFT -> ItemTouchHelper.LEFT
    SwipeToRemoveDirection.RIGHT -> ItemTouchHelper.RIGHT
}
