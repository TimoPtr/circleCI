package com.kolibree.android.app.ui.common

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Decorates a RecyclerView adding the dimensions specified between items
 *
 * spacingBottom is only added if it's the last position
 */
class ItemSpacingDecorator(
    context: Context,
    @DimenRes spacingLeft: Int = 0,
    @DimenRes spacingRight: Int = 0,
    @DimenRes spacingTop: Int = 0,
    @DimenRes spacingBottom: Int = 0
) : RecyclerView.ItemDecoration() {

    constructor(context: Context, @DimenRes spacing: Int) : this(
        context,
        spacingLeft = spacing,
        spacingRight = spacing,
        spacingBottom = spacing,
        spacingTop = spacing
    )

    private val spacingLeft: Int = readDimension(context, spacingLeft)
    private val spacingRight: Int = readDimension(context, spacingRight)
    private val spacingTop: Int = readDimension(context, spacingTop)
    private val spacingBottom: Int = readDimension(context, spacingBottom)

    private fun readDimension(context: Context, @DimenRes dimenResId: Int): Int {
        if (dimenResId == 0) return 0

        return context.resources.getDimensionPixelSize(dimenResId)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = spacingTop
        outRect.right = spacingRight
        outRect.left = spacingLeft

        if (parent.adapter != null && parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
            outRect.bottom = spacingBottom
        }
    }
}
