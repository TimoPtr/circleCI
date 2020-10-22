/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.guidedbrushing

import android.content.Context
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.dagger.viewInjectorForViewType
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.jaws.R
import com.kolibree.android.jaws.base.BaseJawsView
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

/** Guided Brushing [BaseJawsView] implementation */
@Keep
class GuidedBrushingJawsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseJawsView<GuidedBrushingJawsViewRenderer>(context, attrs) {

    @Inject
    override lateinit var renderer: GuidedBrushingJawsViewRenderer

    init {
        context.viewInjectorForViewType<GuidedBrushingJawsView>().inject(this)

        if (::renderer.isInitialized) {
            setRenderer(renderer)
            setRenderMode(RENDERMODE_CONTINUOUSLY)
            setCurrentlyBrushedZoneColor(
                context.getColorFromAttr(R.attr.jawCurrentColor)
            )
            setMissedZonesColor(context.getColorFromAttr(R.attr.jawMissedColor))
            renderer.reset()
        }
    }

    /**
     * Show a given zone and paint it according to its coverage progression
     *
     * @param zone [MouthZone16] zone to show
     * @param progress [Int]
     */
    fun showZoneWithProgress(zone: MouthZone16, progress: Int) {
        renderer.setCurrentlyBrushedZone(zone, progress)
        requestRender()
    }

    /** Reset the view as it should be at the beginning of the brushing activity */
    fun reset() {
        renderer.reset()
        requestRender()
    }

    /**
     * Set the color that will be interpolated with white to paint the current zone
     *
     * @param color [ColorInt] [Int]
     */
    fun setCurrentlyBrushedZoneColor(@ColorInt color: Int) {
        renderer.currentZoneColor.set(color)
        requestRender()
    }

    /**
     * Set the color that will be interpolated with white to paint missed areas
     *
     * @param color [ColorInt] [Int]
     */
    fun setMissedZonesColor(@ColorInt color: Int) {
        renderer.missedZonesColor.set(color)
        requestRender()
    }
}

@Keep
@BindingAdapter("android:background")
fun setBackgroundColor(view: GuidedBrushingJawsView, @ColorInt color: Int) {
    view.setBackgroundColor(color)
    view.requestRender()
}

@Keep
@BindingAdapter(value = ["currentZone", "currentZoneProgressPercent"], requireAll = true)
fun GuidedBrushingJawsView.setZoneColor(
    currentZone: MouthZone16?,
    progressPercent: Int?
) = currentZone?.let {
    progressPercent?.let {
        showZoneWithProgress(currentZone, progressPercent)
    }
}
