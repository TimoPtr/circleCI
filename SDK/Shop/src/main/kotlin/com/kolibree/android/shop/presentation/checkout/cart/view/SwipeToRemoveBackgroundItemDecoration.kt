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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.shop.R

internal class SwipeToRemoveBackgroundItemDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {

    private val backgroundDrawable: Drawable
    private val trashDrawable: Drawable
    private val iconMargin: Int

    var isEnabled = true

    init {
        backgroundDrawable = ColorDrawable(context.getColorFromAttr(R.attr.colorAccent))
        trashDrawable = ContextCompat.getDrawable(context, R.drawable.ic_shop_trash)
            ?: throw Resources.NotFoundException()
        iconMargin = context.resources.getDimensionPixelSize(R.dimen.dot_double)
    }

    override fun onDraw(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (isEnabled && parent.itemAnimator?.isRunning == true) {

            setBackgroundDrawableBounds(parent)
            backgroundDrawable.draw(c)

            setTrashDrawableBounds(parent)
            trashDrawable.draw(c)
        }
        super.onDraw(c, parent, state)
    }

    private fun setBackgroundDrawableBounds(parent: RecyclerView) {
        val lastViewComingDown = findLastViewComingDown(parent)
        val firstViewComingUp: View? = findFirstViewComingUp(parent)

        val top = findTopPosition(lastViewComingDown, firstViewComingUp)
        val bottom = findBottomPosition(lastViewComingDown, firstViewComingUp)

        backgroundDrawable.setBounds(0, top, parent.width, bottom)
    }

    private fun setTrashDrawableBounds(parent: RecyclerView) {
        val drawableHeight = trashDrawable.intrinsicHeight
        val centerY = backgroundDrawable.bounds.centerY()
        val y0 = centerY - drawableHeight / 2
        val y1 = centerY + drawableHeight / 2
        trashDrawable.setBounds(
            parent.width - iconMargin - trashDrawable.intrinsicWidth,
            y0,
            parent.width - iconMargin,
            y1
        )
    }

    private fun findBottomPosition(lastViewComingDown: View?, firstViewComingUp: View?): Int = when {
        lastViewComingDown != null && firstViewComingUp != null -> {
            firstViewComingUp.top + firstViewComingUp.translationY.toInt()
        }
        lastViewComingDown != null -> {
            lastViewComingDown.bottom
        }
        firstViewComingUp != null -> {
            firstViewComingUp.top + firstViewComingUp.translationY.toInt()
        }
        else -> 0
    }

    private fun findTopPosition(lastViewComingDown: View?, firstViewComingUp: View?): Int = when {
        lastViewComingDown != null && firstViewComingUp != null -> {
            lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
        }
        lastViewComingDown != null -> {
            lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
        }
        firstViewComingUp != null -> {
            firstViewComingUp.top
        }
        else -> 0
    }

    private fun findFirstViewComingUp(parent: RecyclerView): View? {
        for (i in 0 until childCount(parent)) {
            val child: View = parent.layoutManager?.getChildAt(i) ?: continue
            if (child.translationY > 0) { // view is coming up
                return child
            }
        }
        return null
    }

    private fun findLastViewComingDown(parent: RecyclerView): View? {
        val lastIndex = childCount(parent) - 1
        for (i in lastIndex downTo 0) {
            val child: View = parent.layoutManager?.getChildAt(i) ?: continue
            if (child.translationY < 0) { // view is coming down
                return child
            }
        }
        return null
    }

    private fun childCount(parent: RecyclerView) = parent.layoutManager?.childCount ?: 0
}
