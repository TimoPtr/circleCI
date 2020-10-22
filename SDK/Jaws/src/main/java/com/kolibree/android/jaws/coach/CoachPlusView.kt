package com.kolibree.android.jaws.coach

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.dagger.viewInjectorForViewType
import com.kolibree.android.jaws.coach.renderer.CoachPlusRenderer
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

/**
 * Animated 3D jaws and toothbrush view
 *
 *
 * Make sure you use it in a hardware accelerated environment
 */
@Keep
class CoachPlusView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    var renderer: CoachPlusRenderer? = null
        /**
         * Set the [CoachPlusRenderer] that will draw this view
         */
        set(value) {
            field = value
            setRenderer(renderer)
        }

    @Inject
    internal lateinit var configChooser: AndroidConfigChooser

    init {
        context.viewInjectorForViewType(CoachPlusView::class.java).inject(this)
        GLSurfaceViewInitializer.initEGLConfig(this, configChooser)
        preserveEGLContextOnPause = true
    }

    /**
     * Show a given zone and paint it with a color
     *
     * @param zone [MouthZone16] zone to show
     * @param color ARGB int color
     */
    fun showZoneWithColor(zone: MouthZone16, @ColorInt color: Int) {
        renderer?.setCurrentlyBrushedZone(zone, color)
    }

    /**
     * Set if the view should draw the toothbrush head or not
     *
     * @param show true to make it draw the toothbrush head, false otherwise
     */
    fun showToothbrushHead(show: Boolean) {
        renderer?.showToothbrushHead(show)
    }

    /**
     * Set the underlying [SurfaceView] background color
     *
     * @param color ARGB color
     */
    override fun setBackgroundColor(@ColorInt color: Int) {
        renderer?.setBackgroundColor(color)
    }

    /** Reset the view as it should be at the beginning of the brushing activity  */
    fun reset() {
        renderer?.reset()
    }
}

@Keep
@BindingAdapter("android:background")
fun setBackgroundColor(view: CoachPlusView, @ColorInt color: Int) {
    view.setBackgroundColor(color)
}

@Keep
@BindingAdapter("ringLedColor")
fun CoachPlusView.setRingLedColor(@ColorInt color: Int) {
    renderer?.setRingLedColor(color)
}

@Keep
@BindingAdapter(value = ["currentZone", "currentZoneColor"], requireAll = true)
fun CoachPlusView.setZoneColor(currentZone: MouthZone16?, @ColorInt currentZoneColor: Int?) {
    currentZone?.let {
        currentZoneColor?.let {
            showZoneWithColor(currentZone, currentZoneColor)
        }
    }
}

@Keep
@BindingAdapter("showToothbrushHead")
fun CoachPlusView.showTBHead(show: Boolean) {
    showToothbrushHead(show)
}
