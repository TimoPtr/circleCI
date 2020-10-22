/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.behavior.SwipeDismissBehavior
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.failearly.FailEarly
import timber.log.Timber

private const val UNKNOWN_LAYOUT_ID = -1

@Keep
class DismissibleTooltip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    private lateinit var swipeBehaviour: CoordinatorLayout.Behavior<DismissibleTooltip>

    init {
        var layoutId = UNKNOWN_LAYOUT_ID
        attrs?.also {
            context.obtainStyledAttributes(attrs, R.styleable.DismissibleTooltip).apply {
                layoutId = getResourceId(R.styleable.DismissibleTooltip_layout, -1)
                recycle()
            }
        }
        if (layoutId > 0) {
            View.inflate(context, layoutId, this)
        } else {
            FailEarly.fail("Layout for the tooltip was not provided. " +
                "Pass it as XML `layout` attribute in your layout file."
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        swipeBehaviour = SwipeDismissBehavior<DismissibleTooltip>().apply {
            setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
            setListener(object : SwipeDismissBehavior.OnDismissListener {
                override fun onDismiss(view: View) {
                    Timber.v("onDismiss")
                }

                override fun onDragStateChanged(state: Int) {
                    Timber.v("onDragStateChanged: $state")
                }
            })
        }
        updateLayoutParams<CoordinatorLayout.LayoutParams> {
            behavior = swipeBehaviour
        }
        requestFocus()
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = swipeBehaviour
}
