/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager

@Keep
open class NonSwipeableViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}

@Keep
@BindingAdapter(value = ["content"])
fun <T : Any> NonSwipeableViewPager.setContent(content: List<T>) {
    @Suppress("UNCHECKED_CAST")
    val adapter = adapter as DataBindableViewPagerAdapter<T>?

    adapter?.update(content)
}

@Keep
@BindingAdapter(value = ["currentPosition"])
fun NonSwipeableViewPager.setCurrentPosition(currentPosition: Int) {
    currentItem = currentPosition
}

@Keep
@BindingAdapter(value = ["currentPositionSmooth"])
fun NonSwipeableViewPager.setCurrentPositionSmooth(currentPosition: Int) {
    setCurrentItem(currentPosition, true)
}
