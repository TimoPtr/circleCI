/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.zone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.widget.ZoneData
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.extensions.getColorFromAttr
import kotlin.math.min

@VisibleForApp
class ZoneProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val foregroundColor = context.getColorFromAttr(R.attr.zoneForegroundColor)
    private val backgroundColor = context.getColorFromAttr(R.attr.zoneBackgroundColor)
    private val ongoingForegroundColor = context.getColorFromAttr(R.attr.zoneOngoingForegroundColor)
    private val ongoingBackgroundColor = context.getColorFromAttr(R.attr.zoneOngoingBackgroundColor)

    private val spaceBetweenZones = context.resources.getDimensionPixelOffset(R.dimen.dot_quarter)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cacheBitmap: Bitmap? = null
    private val finishedAtMap = mutableMapOf<Int, Long>()
    private val rect = RectF()

    var data: ZoneProgressData = ZoneProgressData()
        private set
    private var lastAnimationUpdateTime: Long = 0

    init {
        shapePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

        if (isInEditMode) {
            data = ZoneProgressData(listOf(ZoneData(false, 1f)))
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            it.drawColor(Color.TRANSPARENT)
            drawZones(it)
        }

        if (shouldInvalidate()) {
            invalidate()
        }
    }

    private fun shouldInvalidate(): Boolean {
        val elapsedTime = SystemClock.elapsedRealtime() - lastAnimationUpdateTime
        return elapsedTime <= REDRAW_DURATION
    }

    private fun drawZones(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val widthWithoutSpacesBetween = width - spaceBetweenZones * (data.zones.size - 1)
        val zoneWidth = widthWithoutSpacesBetween / data.zones.size
        val shift = zoneWidth + spaceBetweenZones
        data.zones.forEachIndexed { index, zoneData ->
            val left = index * shift
            val right = left + zoneWidth
            rect.set(left, 0f, right, canvas.height.toFloat())
            drawZone(canvas, rect, zoneData, index)
        }
        roundEdges(canvas)
    }

    private fun prepareBitmap(): Bitmap =
        cacheBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .apply { cacheBitmap = this }

    private fun roundEdges(canvas: Canvas) {
        val radius = canvas.height / 2f
        val shapeBitmap = prepareBitmap()
        val shapeCanvas = Canvas(shapeBitmap)
        shapeCanvas.drawColor(Color.TRANSPARENT)
        shapeCanvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            paint
        )

        canvas.drawBitmap(shapeBitmap, 0f, 0f, shapePaint)
    }

    private fun drawZone(canvas: Canvas, rect: RectF, zoneData: ZoneData, index: Int) {
        paint.color = if (zoneData.isOngoing) {
            ongoingBackgroundColor
        } else {
            calcFinishedBackgroundColor(index)
        }
        canvas.drawRect(rect, paint)

        paint.color = if (zoneData.isOngoing) {
            ongoingForegroundColor
        } else {
            calcFinishedForegroundColor(index)
        }
        val progressWidth = zoneData.progress * rect.width()
        val progressRight = rect.left + progressWidth
        canvas.drawRect(rect.left, 0f, progressRight, rect.bottom, paint)
    }

    @ColorInt
    private fun calcFinishedForegroundColor(index: Int): Int {
        val ration = calcColorBlendRation(index)
        return ColorUtils.blendARGB(ongoingForegroundColor, foregroundColor, ration)
    }

    @ColorInt
    private fun calcFinishedBackgroundColor(index: Int): Int {
        val ratio = calcColorBlendRation(index)
        return ColorUtils.blendARGB(ongoingBackgroundColor, backgroundColor, ratio)
    }

    private fun calcColorBlendRation(index: Int): Float {
        val finishedAt = finishedAtMap[index] ?: 0L
        val timeElapsed = min(ANIM_DURATION, SystemClock.elapsedRealtime() - finishedAt)
        return timeElapsed / ANIM_DURATION.toFloat()
    }

    fun setZoneProgressData(newestData: ZoneProgressData) {
        newestData.zones.forEachIndexed { index, currentZone ->
            val previousZone = data.zones.getOrNull(index)
            if (isAlreadyFinished(previousZone, currentZone)) {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                finishedAtMap[index] = elapsedRealtime
                lastAnimationUpdateTime = elapsedRealtime
            }
        }
        this.data = newestData

        invalidate()
    }

    private fun isAlreadyFinished(previous: ZoneData?, zoneData: ZoneData): Boolean {
        return !zoneData.isOngoing && previous?.isOngoing == true
    }
}

@BindingAdapter("zoneData")
internal fun ZoneProgressBarView.bindZoneProgressData(data: ZoneProgressData) {
    setZoneProgressData(data)
}

private const val ANIM_DURATION = 300L
private const val REDRAW_DURATION = 500L
