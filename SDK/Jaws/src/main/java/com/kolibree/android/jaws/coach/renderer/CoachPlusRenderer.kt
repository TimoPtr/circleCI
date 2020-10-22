package com.kolibree.android.jaws.coach.renderer

import android.opengl.GLSurfaceView
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import com.kolibree.kml.MouthZone16

/**
 * Coach+ renderer
 */
@Keep
interface CoachPlusRenderer : GLSurfaceView.Renderer {

    /**
     * Set the currently brushed zone and its completion percentage
     *
     * @param zone non null [MouthZone16]
     * @param color ARGB color
     */
    fun setCurrentlyBrushedZone(zone: MouthZone16, @ColorInt color: Int)

    /**
     * Set if the view should draw the toothbrush head or not
     *
     * @param show true to make it draw the toothbrush head, false otherwise
     */
    fun showToothbrushHead(show: Boolean)

    /**
     * Set the EGL horizon color
     *
     * @param color ARGB color int
     */
    fun setBackgroundColor(@ColorInt color: Int)

    /**
     * Reset (or set) the 3D view as it has to be at the beginning of the brushing activity
     */
    fun reset()

    /**
     * Set the Plaqless toothbrush head LED color
     *
     * Will do nothing on a regular toothbrush head
     *
     * @param color ARGB color int
     */
    fun setRingLedColor(@ColorInt color: Int)
}
