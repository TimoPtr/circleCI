/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.bottomnavigation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.hum.R

@VisibleForApp
class DottedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dottedBottomNavigationViewStyle
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val dotPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var topEdge: DottedBottomNavigationEdgeTreatment? = null
    private var dotAnimator: Animator? = null

    var dotDiameter: Float = 0f
        set(value) {
            field = value
            invalidateShape()
        }

    var dotPadding: Float = 0f
        set(value) {
            field = value
            invalidateShape()
        }

    var dotColor: Int = Color.RED
        set(value) {
            field = value
            dotPaint.color = value
            invalidate()
        }

    private var dotPosition: Float = 0f
        @Keep
        set(value) {
            field = value
            topEdge?.also {
                it.horizontalOffset = value
                invalidateOutline()
                invalidate()
            }
        }

    var dotAnimationDuration: Long = 0L

    init {
        attrs?.also {
            context.withStyledAttributes(it, R.styleable.DottedBottomNavigationView, defStyleAttr) {
                dotDiameter = getDimension(R.styleable.DottedBottomNavigationView_dotDiameter, 0f)
                dotPadding = getDimension(R.styleable.DottedBottomNavigationView_dotPadding, 0f)
                dotColor = getColor(R.styleable.DottedBottomNavigationView_dotColor, Color.RED)
                dotAnimationDuration =
                    getInt(R.styleable.DottedBottomNavigationView_dotAnimationDuration, 0).toLong()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        doOnPreDraw {
            updateDotPosition(selectedItemId)
        }
    }

    override fun setSelectedItemId(itemId: Int) {
        super.setSelectedItemId(itemId)
        updateDotPosition(itemId, true)
    }

    private fun updateDotPosition(itemId: Int, animate: Boolean = false) {
        val selectedView = findViewById<View>(itemId)
        val internalOffset = children.firstOrNull { it is BottomNavigationMenuView }?.x ?: 0f
        val newPosition = internalOffset + selectedView.x + (selectedView.width / 2)
        dotAnimator?.takeIf { it.isRunning }?.cancel()
        if (animate) {
            dotAnimator =
                ObjectAnimator.ofFloat(this, "dotPosition", dotPosition, newPosition).apply {
                    duration = dotAnimationDuration
                    interpolator = AccelerateDecelerateInterpolator()
                }.also { it.start() }
        } else {
            dotPosition = newPosition
        }
    }

    private fun invalidateShape() {
        topEdge =
            DottedBottomNavigationEdgeTreatment(dotDiameter / 2f, dotPadding).also { newTopEdge ->
                background = MaterialShapeDrawable(
                    ShapeAppearanceModel.builder()
                        .setTopEdge(newTopEdge)
                        .build()
                ).apply {
                    fillColor = getBackgroundColor()
                    (background as? MaterialShapeDrawable)?.also { old ->
                        tintList = old.tintList
                    }
                }
            }
        invalidateOutline()
    }

    private fun getBackgroundColor(): ColorStateList =
        background.let {
            when (it) {
                is ColorDrawable -> ColorStateList.valueOf(it.color)
                is ColorStateList -> it
                is MaterialShapeDrawable -> it.fillColor ?: illegalBackgroundColorState()
                else -> illegalBackgroundColorState()
            }
        }

    private fun illegalBackgroundColorState(): Nothing = throw IllegalStateException(
        "BottomNavigationView should have the correct background colour type. " +
            "This should not be possible"
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(dotPosition, 0f, dotDiameter / 2f, dotPaint)
    }
}
