/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.R
import kotlin.math.max
import kotlin.math.min

@VisibleForApp
class ElevationMediatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.elevationMediatorViewStyle
) : ConstraintHelper(context, attrs, defStyleAttr) {

    @IdRes
    private val scrollingViewId: Int

    private lateinit var strategy: Strategy

    private val scrollDirection: ScrollDirection
    private val startElevation: Float
    private val endElevation: Float
    private val elevationRange: Float
    private val scrollDistance: Float

    private var currentScroll: Int = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ElevationMediatorView).apply {
            scrollingViewId = getResourceId(R.styleable.ElevationMediatorView_scrollingView, 0)
            scrollDirection = getInteger(R.styleable.ElevationMediatorView_scrollDirection, 1)
                .let { direction ->
                    if (direction == 0) ScrollDirection.HORIZONTAL else ScrollDirection.VERTICAL
                }
            startElevation = getDimension(R.styleable.ElevationMediatorView_startElevation, 0f)
            endElevation = getDimension(R.styleable.ElevationMediatorView_endElevation, 0f)
            elevationRange = endElevation - startElevation
            scrollDistance = getDimension(R.styleable.ElevationMediatorView_scrollDistance, 0f)
            recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent !is ConstraintLayout) {
            throw IllegalArgumentException("parent is not a ConstraintLayout")
        }
        val scrollingView: View = (parent as ConstraintLayout).findViewById(scrollingViewId)
        strategy = when (scrollingView) {
            is RecyclerView -> RecyclerViewStrategy(scrollingView)
            is ScrollView -> ScrollViewStrategy(scrollingView)
            else -> throw IllegalArgumentException(
                "${resources.getResourceName(scrollingViewId)} is neither a RecyclerView nor ScrollView"
            )
        }

        if (scrollDistance > 0f) {
            strategy.attach()
        }
    }

    fun recalculateElevation() {
        if (scrollDistance <= 0f) return
        updateAndCalculateElevation()
    }

    private fun updateAndCalculateElevation() {
        currentScroll = strategy.getCurrentScroll()
        val elevationFraction =
            max(min(currentScroll.toFloat(), scrollDistance), 0f) / scrollDistance
        val newElevation = startElevation + (elevationFraction * elevationRange)
        getViews(parent as ConstraintLayout).forEach { view ->
            view.elevation = newElevation
        }
    }

    override fun onDetachedFromWindow() {
        strategy.detach()
        super.onDetachedFromWindow()
    }

    private interface Strategy {

        fun getCurrentScroll(): Int

        fun attach()

        fun detach()
    }

    private inner class RecyclerViewStrategy(
        private val recyclerView: RecyclerView
    ) : Strategy {

        override fun getCurrentScroll(): Int = with(recyclerView) {
            if (scrollDirection == ScrollDirection.HORIZONTAL) computeHorizontalScrollOffset()
            else computeVerticalScrollOffset()
        }

        override fun attach() {
            recyclerView.addOnScrollListener(scrollListener)
        }

        override fun detach() {
            recyclerView.removeOnScrollListener(scrollListener)
        }

        private val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateAndCalculateElevation()
            }
        }
    }

    private inner class ScrollViewStrategy(
        private val scrollView: ScrollView
    ) : Strategy {

        override fun getCurrentScroll(): Int = with(scrollView) {
            if (scrollDirection == ScrollDirection.HORIZONTAL) scrollX else scrollY
        }

        override fun attach() {
            scrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        }

        override fun detach() {
            scrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        }

        private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
            updateAndCalculateElevation()
        }
    }

    @VisibleForApp
    companion object {

        @VisibleForApp
        enum class ScrollDirection {
            HORIZONTAL,
            VERTICAL
        }
    }
}
