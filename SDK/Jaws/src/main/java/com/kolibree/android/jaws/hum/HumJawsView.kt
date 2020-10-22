/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.hum

import android.content.Context
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.annotation.Keep
import com.kolibree.android.app.dagger.viewInjectorForViewType
import com.kolibree.android.jaws.base.BaseJawsView
import com.kolibree.android.jaws.tilt.touch.TouchJawsTiltController
import javax.inject.Inject

/** Hum branded [BaseJawsView] implementation */
@Keep
class HumJawsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseJawsView<HumJawsViewRenderer>(context, attrs) {

    @Inject
    override lateinit var renderer: HumJawsViewRenderer

    @Inject
    internal lateinit var touchJawsTiltController: TouchJawsTiltController

    init {
        context.viewInjectorForViewType<HumJawsView>().inject(this)

        if (::renderer.isInitialized) {
            setRenderer(renderer)
            setRenderMode(RENDERMODE_WHEN_DIRTY)

            if (::touchJawsTiltController.isInitialized) {
                renderer.setTiltController(touchJawsTiltController)
            }
        }
    }

    fun onMotionEvent(event: MotionEvent) {
        preventTouchConflict(event)
        touchJawsTiltController.onMotionEvent(event)
        adjustRenderMode(event)
    }

    private fun preventTouchConflict(event: MotionEvent) {
        when (event.action) {
            ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
        }
    }

    private fun adjustRenderMode(event: MotionEvent) {
        when (event.action) {
            ACTION_DOWN -> setRenderMode(RENDERMODE_CONTINUOUSLY)
            ACTION_UP -> setRenderMode(RENDERMODE_WHEN_DIRTY)
        }
    }
}
