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
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.MainThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.appbar.AppBarLayout
import com.kolibree.android.extensions.runOnMainThread
import com.kolibree.android.homeui.hum.R
import kotlin.math.max

internal class ShadeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    CoordinatorLayout.AttachedBehavior,
    AppBarLayout.OnOffsetChangedListener {

    private var backgroundScrim: Drawable? = null
    private var shadeExpandedHeight: Float = 0f
    private var topOffset: Int = 0
    private var scrimHeight: Int = 0
    private var maxScrimHeight: Int = -1

    private var listener: OnShadeViewExpandedListener? = null
    private val shadeViewExpandedNotifier = ShadeViewExpandedNotifier()

    init {
        context.withStyledAttributes(attrs, R.styleable.ShadedRecyclerView) {
            backgroundScrim = getDrawable(R.styleable.ShadedRecyclerView_backgroundScrim)
            shadeExpandedHeight =
                getDimension(R.styleable.ShadedRecyclerView_shadeExpandedHeight, 0f)
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return Behaviour()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val factor = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
        val elevationFactor = ((1f - factor) / HALF).coerceAtMost(1f)
        val expandedFactor = (((1f - factor) - HALF) / HALF).coerceAtLeast(0f)
        appBarLayout.translationZ = -(appBarLayout.elevation * elevationFactor)
        topOffset = appBarLayout.bottom
        setScrimHeight(expandedFactor)
        invalidate()
    }

    private fun setScrimHeight(expandedFactor: Float) {
        scrimHeight = topOffset + (expandedFactor * shadeExpandedHeight).toInt()

        maxScrimHeight = max(maxScrimHeight, scrimHeight)

        shadeViewExpandedNotifier.runOnMainThread()
    }

    override fun draw(canvas: Canvas) {
        backgroundScrim?.also { scrim ->
            scrim.mutate().setBounds(0, topOffset, width, scrimHeight)
            scrim.draw(canvas)
        }
        super.draw(canvas)
    }

    fun isExpanded() = scrimHeight == maxScrimHeight

    fun setOnExpandedListener(listener: OnShadeViewExpandedListener) {
        this.listener = listener
    }

    private inner class Behaviour : CoordinatorLayout.Behavior<ShadeView>() {
        private var isAttached = false

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: ShadeView,
            dependency: View
        ): Boolean {
            return dependency is AppBarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: ShadeView,
            dependency: View
        ): Boolean {
            super.onDependentViewChanged(parent, child, dependency)
            if (!isAttached) {
                (dependency as? AppBarLayout)?.also { appBarLayout ->
                    appBarLayout.addOnOffsetChangedListener(child)
                    isAttached = true
                }
            }
            return false
        }

        override fun onDependentViewRemoved(
            parent: CoordinatorLayout,
            child: ShadeView,
            dependency: View
        ) {
            super.onDependentViewRemoved(parent, child, dependency)
            if (isAttached) {
                (dependency as? AppBarLayout)?.removeOnOffsetChangedListener(child)
                isAttached = false
            }
        }
    }

    private inner class ShadeViewExpandedNotifier : Runnable {
        private var wasExpanded = false

        override fun run() {
            listener?.let {
                if (!wasExpanded && isExpanded()) {
                    it.onFullyExpanded()

                    wasExpanded = true
                } else if (wasExpanded && !isExpanded()) {
                    it.onNotFullyExpanded()

                    wasExpanded = false
                }
            }
        }
    }

    companion object {
        const val HALF = 0.5f
    }
}

internal interface OnShadeViewExpandedListener {

    /**
     * Invoked when the view is fully expanded
     *
     * It will only be invoked once after a previous invocation to [onNotFullyExpanded]
     *
     * Implementers shouldn't do blocking operations
     */
    @MainThread
    fun onFullyExpanded()

    /**
     * Invoked when the view is not fully expanded
     *
     * It will only be invoked if there was a previous invocation to [onFullyExpanded]
     *
     * Implementers shouldn't do blocking operations
     */
    @MainThread
    fun onNotFullyExpanded()
}
