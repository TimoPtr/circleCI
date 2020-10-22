/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.dialog.colorIntFromAttribute
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.homeui.hum.databinding.ViewBrushingProgressionBinding
import com.kolibree.databinding.bindingadapter.setTintColor

/**
 * This class is in charge to construct dynamically the layout of the Brushing Streak Progression,
 * according to the progression passed with the data-bindings methods
 */
@VisibleForApp
class BrushingProgressionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var progression = BrushingStreakProgression(0)

    val binding: ViewBrushingProgressionBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewBrushingProgressionBinding.inflate(inflater, this, true)
        binding.viewState = progression
        binding.executePendingBindings()
    }

    fun setProgression(progression: BrushingStreakProgression) {
        this.progression = progression
        binding.viewState = progression
        binding.executePendingBindings()
    }
}

@Keep
@BindingAdapter("brushing_progression")
fun BrushingProgressionView.bindProgression(progression: BrushingStreakProgression) {
    setProgression(progression)
}

@BindingAdapter("progression_foreground")
internal fun ImageView.bindForegroundProgressionColor(stepAchieved: Boolean) {
    when (stepAchieved) {
        true -> setTintColor(R.attr.streakProgressionForegroundColor)
        else -> setTintColor(R.attr.streakProgressionInactiveColor)
    }
    val backgroundColorAttr = when (stepAchieved) {
        true -> R.attr.streakProgressionBackgroundColor
        else -> R.attr.streakProgressionInactiveColor
    }
    val backgroundColor = colorIntFromAttribute(context, backgroundColorAttr)
    backgroundTintList = ColorStateList.valueOf(backgroundColor)
}

@BindingAdapter("progression_background")
internal fun View.bindBackgroundProgressionColor(stepAchieved: Boolean) {
    val colorAttributeId = when (stepAchieved) {
        true -> R.attr.streakProgressionBackgroundColor
        else -> R.attr.streakProgressionInactiveColor
    }
    val backgroundColor = colorIntFromAttribute(context, colorAttributeId)
    setBackgroundColor(backgroundColor)
}
