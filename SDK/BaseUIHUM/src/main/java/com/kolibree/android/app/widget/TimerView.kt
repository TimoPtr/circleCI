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
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.databinding.BindingAdapter
import com.kolibree.android.baseui.hum.R
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.view_timer.view.*

@Keep
class TimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.timerViewStyle
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_timer, this)

        if (!isInEditMode) {
            var timerTextAppearance: Int
            var timerBubbleColor: Int

            context.obtainStyledAttributes(attrs, R.styleable.TimerView, defStyleAttr, 0).apply {
                timerTextAppearance =
                    getResourceIdOrThrow(R.styleable.TimerView_timerTextAppearance)
                timerBubbleColor = getColorOrThrow(R.styleable.TimerView_timerBubbleColor)
                recycle()
            }

            setTextStyle(timerTextAppearance, timerBubbleColor)

            setSeconds(0)
        }
    }

    private fun setTextStyle(timerTextAppearance: Int, timerBubbleColor: Int) {
        listOf(
            timerMinutesTenth,
            timerMinutesUnit,
            timerSecondsTenth,
            timerSecondsUnit
        ).forEach { textView ->
            textView.setTextAppearance(timerTextAppearance)
            (textView.background.mutate() as GradientDrawable).setColor(timerBubbleColor)
        }

        timerSeparator.setTextAppearance(timerTextAppearance)
    }
}

@Keep
@BindingAdapter("timer_seconds")
fun TimerView.setSeconds(seconds: Long?) {
    seconds?.let {
        val minutesPassed: Long = TimeUnit.SECONDS.toMinutes(seconds)
        val secondsStripped: Long = seconds % TimeUnit.MINUTES.toSeconds(1)

        timerMinutesTenth.text = (minutesPassed / TWO_DIGITS).toString()
        timerMinutesUnit.text = (minutesPassed % TWO_DIGITS).toString()

        timerSecondsTenth.text = (secondsStripped / TWO_DIGITS).toString()
        timerSecondsUnit.text = (secondsStripped % TWO_DIGITS).toString()
    }
}

private const val TWO_DIGITS = 10
