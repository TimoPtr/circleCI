/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.animation.LinearInterpolator
import com.kolibree.android.angleandspeed.testangles.model.ToothSide

// topPointsCount + sidePointsCount*2 are count of path points of 0..180
private const val TOP_POINTS_COUNTS = 59
private const val SIDE_POINTS_COUNTS = 61
private const val ANIMATION_DURATION = 800L
private const val MAX_ANGLES = 90.0f

internal class MolarAnimation constructor(
    private val view: View,
    private val molarAnimationArea: Rect
) {

    private val interpolator = LinearInterpolator()
    private var currentDegree = .0f
    private var molarAnimator: AnimatorSet = AnimatorSet()

    fun start(
        brushDegree: Float,
        brushSide: ToothSide
    ) {
        currentDegree = getRotatableDegree(brushSide, brushDegree)
        val path = getSegment(initPath())
        if (path.isNotEmpty()) {
            initAnimationSet(path)
            molarAnimator.start()
        }
    }

    private fun getSegment(path: Array<Point>): Array<Point> {
        val pointList = ArrayList<Point>()
        val past = getStraightAngle(view.rotation).toInt()
        val current = getStraightAngle(currentDegree).toInt()

        val diff = current - past

        if (diff > 0) {
            for (index in past..current) {
                pointList.add(path[index])
            }
        } else if (diff < 0) {
            for (index in current..past) {
                pointList.add(path[index])
            }
            pointList.reverse()
        }

        if (pointList.size == 0) {
            pointList.add(Point(view.x.toInt(), view.y.toInt()))
        }
        return pointList.toTypedArray()
    }

    private fun getStraightAngle(degree: Float): Float = MAX_ANGLES + degree

    private fun initPath(): Array<Point> {
        val leftX = molarAnimationArea.left
        val rightX = molarAnimationArea.right

        var topX = leftX
        val topY = molarAnimationArea.top

        var leftY = topY
        var rightY = topY

        val xIncrement = (rightX - leftX) / TOP_POINTS_COUNTS
        val yIncrement = (molarAnimationArea.bottom - molarAnimationArea.top) / SIDE_POINTS_COUNTS

        val topPoints = Array(TOP_POINTS_COUNTS) { Point() }
        val leftPoints = Array(SIDE_POINTS_COUNTS) { Point() }
        val rightPoints = Array(SIDE_POINTS_COUNTS) { Point() }

        for (element in topPoints) {
            topX += xIncrement
            element.set(topX, topY)
        }

        for (i in leftPoints.indices) {
            leftPoints[i].set(leftX, leftY)
            leftY += yIncrement

            rightPoints[i].set(rightX, rightY)
            rightY += yIncrement
        }

        leftPoints.reverse()
        return leftPoints + topPoints + rightPoints
    }

    fun cancel() {
        molarAnimator.cancel()
    }

    private fun initAnimationSet(path: Array<Point>) {
        molarAnimator.duration = ANIMATION_DURATION
        molarAnimator.play(createRotateAnim(view.rotation, currentDegree.toFloat()))
            .with(createMoveByPathAnim(path))
    }

    private fun createMoveByPathAnim(pathArray: Array<Point>): ObjectAnimator {
        val path = Path()
        path.moveTo(pathArray[0].x.toFloat(), pathArray[0].y.toFloat())
        for (i in 1 until pathArray.size) {
            path.lineTo(pathArray[i].x.toFloat(), pathArray[i].y.toFloat())
        }

        val objectAnimator = ObjectAnimator.ofFloat(view, View.X, View.Y, path)
        objectAnimator.interpolator = interpolator
        return objectAnimator
    }

    private fun createRotateAnim(from: Float, to: Float): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(view, "rotation", from, to)
        objectAnimator.interpolator = interpolator
        return objectAnimator
    }

    private fun getRotatableDegree(brushSide: ToothSide, brushDegree: Float): Float {
        val temp = if (brushDegree > MAX_ANGLES) MAX_ANGLES else brushDegree
        return if (brushSide == ToothSide.LEFT) temp.unaryMinus() else temp
    }
}

internal fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
