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
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import com.kolibree.android.baseui.R

/**
 * A custom ImageView which allows a partner view to make an oval or round alpha cutout in this
 * image. The size of the cutout will match the bounds of the partner view, so if you need to add
 * space within the cutout then adjust the padding of the partner view, but use margin if you need
 * to add space to the partner view without affecting the cutout.
 *
 * <br />
 *
 * The partner View is specified using the `viewId` attribute in the layout XML. The partner view
 * must live within the view hierarchy below the parent of the `BadgeImageView`.
 *
 * <br />
 *
 * The distance that the cutout is made from the edge of the partner View is specified using the
 * `borderWidth` attribute of the BadgeImageView.
 *
 * <br />
 * Example usage:
 *
 * <pre>{@code
 *     <com.kolibree.android.app.ui.widget.BadgeImageView
 *         android:id="@+id/patterned_cart"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:src="@drawable/ic_shopping_cart"
 *         app:tint="@color/white"
 *         app:viewId="@id/patterned_cart_badge"
 *         app:borderWidth="1.5dp"
 *         app:layout_constraintBottom_toBottomOf="@id/patterned_background"
 *         app:layout_constraintEnd_toEndOf="@id/patterned_background"
 *         app:layout_constraintStart_toStartOf="@id/patterned_background"
 *         app:layout_constraintTop_toTopOf="@id/patterned_background" />
 *
 *     <TextView
 *         android:id="@+id/patterned_cart_badge"
 *         android:layout_width="@dimen/match_constraints"
 *         android:layout_height="wrap_content"
 *         android:background="@drawable/badge_background"
 *         android:backgroundTint="@color/fire_brick"
 *         android:gravity="center"
 *         android:text="6"
 *         android:textColor="@color/white"
 *         android:textSize="7sp"
 *         app:layout_constraintDimensionRatio="v,1:1"
 *         app:layout_constraintEnd_toEndOf="@id/patterned_cart"
 *         app:layout_constraintTop_toTopOf="@id/patterned_cart"
 *         tools:ignore="HardcodedText,SmallSp" />
 *
 * }</pre>
 */
@Keep
class BadgeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.badgeImageViewStyle
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val badgeViewId: Int
    private var badgeView: View? = null
    private val badgeBorderWidth: Float
    private val badgeBounds = RectF(0f, 0f, 0f, 0f)

    private val clipPath = Path()
    private val cutoutPath = Path()

    private var badgeViewVisibility: Int = View.GONE

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BadgeImageView).apply {
            badgeViewId = getResourceId(R.styleable.BadgeImageView_badgeViewId, 0)
            badgeBorderWidth = getDimension(R.styleable.BadgeImageView_badgeBorderWidth, 0f)
            recycle()
        }
    }

    private val badgeVisibilityHasChanged
        get() = badgeView?.visibility != badgeViewVisibility

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        badgeView = (parent as? ViewGroup)?.findViewById(badgeViewId)
        badgeView?.addOnLayoutChangeListener(onLayoutChangeListener)
        badgeViewVisibility = badgeView?.visibility ?: View.GONE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private val onLayoutChangeListener =
        OnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            badgeBounds.set(
                left.toFloat() - x,
                top.toFloat() - y,
                right.toFloat() - x,
                bottom.toFloat() - y
            )
            badgeBounds.inset(-badgeBorderWidth, -badgeBorderWidth)
            updateCutout(width, height)
        }

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (badgeVisibilityHasChanged) {
            badgeViewVisibility = badgeView?.visibility ?: View.GONE
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCutout(w, h)
    }

    private fun updateCutout(w: Int, h: Int) {
        cutoutPath.reset()
        cutoutPath.addOval(badgeBounds, Path.Direction.CW)
        clipPath.reset()
        clipPath.addRect(0f, 0f, w.toFloat(), h.toFloat(), Path.Direction.CW)
        clipPath.op(cutoutPath, Path.Op.DIFFERENCE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (badgeViewVisibility == View.VISIBLE) {
            canvas.clipPath(clipPath)
        }
        super.onDraw(canvas)
        canvas.restore()
    }
}
