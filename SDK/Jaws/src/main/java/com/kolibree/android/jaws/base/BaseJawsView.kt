/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.base

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.extensions.withValueAnimator
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.jaws.opengl.GLTextureView
import com.kolibree.kml.MouthZone16

@VisibleForApp
abstract class BaseJawsView<T : JawsRenderer> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLTextureView(context, attrs) {

    protected abstract val renderer: JawsRenderer

    @ColorInt
    internal var cleanZoneColor: Int = Color.WHITE

    @ColorInt
    internal var neglectedZoneColor: Int = Color.LTGRAY

    private val argbEvaluator = ArgbEvaluator()

    @Suppress("MagicNumber")
    fun setColorMouthZones(checkupData: Map<MouthZone16, Float>) =
        setColorMouthZones(
            ColorMouthZones(
                MouthZone16.values()
                    .toList()
                    .map { it to checkupData.getOrGetDefault(it, 0f) }
                    .toMap()
                    .mapValues { evaluatedColor(it.value / 100f) }
            )
        )

    @Suppress("TooGenericExceptionCaught")
    fun setColorMouthZones(colorZones: ColorMouthZones) {
        renderer.colorMouthZones(colorZones)
        requestRender()
    }

    fun evaluatedColor(fraction: Float) =
        argbEvaluator.evaluate(fraction, neglectedZoneColor, cleanZoneColor) as Int

    fun setCleanZoneColor(@ColorInt color: Int) {
        cleanZoneColor = color
        requestRender()
    }

    fun setNeglectedZoneColor(@ColorInt color: Int) {
        neglectedZoneColor = color
        requestRender()
    }

    @CallSuper
    open fun pause() = renderer.pause()

    @CallSuper
    open fun resume() {
        renderer.resume()
    }

    final override fun setBackgroundColor(@ColorInt color: Int) =
        renderer.setEglBackgroundColor(color)

    internal fun lastMouthZones() = renderer.lastMouthZones()
}

private fun Map<MouthZone16, Float>.getOrGetDefault(key: MouthZone16, default: Float) =
    get(key) ?: default

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("shouldRender")
fun BaseJawsView<*>.bindShouldRender(shouldRender: Boolean) =
    if (shouldRender) {
        resume()
    } else {
        pause()
    }

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("colorMouthZones")
fun BaseJawsView<*>.bindColorMouthZones(colorMouthZones: ColorMouthZones) =
    setColorMouthZones(colorMouthZones)

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("checkupData")
fun BaseJawsView<*>.bindCheckupData(checkupData: Map<MouthZone16, Float>) =
    setColorMouthZones(checkupData)

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("updateCheckupData")
fun BaseJawsView<*>.bindUpdateCheckupData(checkupData: Map<MouthZone16, Float>) {
    bindUpdateColorMouthZones(
        ColorMouthZones(zonesColor = checkupData.mapValues { evaluatedColor(it.value) })
    )
}

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("updateColorMouthZones")
fun BaseJawsView<*>.bindUpdateColorMouthZones(colorMouthZones: ColorMouthZones) {
    if (lastMouthZones() == colorMouthZones.zonesColor) return

    val start = HashMap(lastMouthZones())
    withValueAnimator { progress ->
        val map = MouthZone16.values().map {
            val startColor = start[it] ?: Color.WHITE
            val endColor = colorMouthZones.zonesColor[it] ?: Color.WHITE
            val blendColor = ColorUtils.blendARGB(startColor, endColor, progress)
            it to blendColor
        }.toMap()
        setColorMouthZones(ColorMouthZones(map))
    }
}

@Keep
@BindingAdapter("neglectedZoneColor")
fun BaseJawsView<*>.bindNeglectedZoneColor(@ColorInt color: Int) = setNeglectedZoneColor(color)

@Keep
@BindingAdapter("cleanZoneColor")
fun BaseJawsView<*>.bindCleanZoneColor(@ColorInt color: Int) = setCleanZoneColor(color)
