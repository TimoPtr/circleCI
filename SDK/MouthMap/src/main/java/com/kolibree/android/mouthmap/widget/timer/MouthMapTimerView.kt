/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.widget.timer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Keep
import com.kolibree.android.commons.DurationFormatter
import com.kolibree.android.mouthmap.R

@Keep
class MouthMapTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val timerTextView: TextView
    private val formatter = DurationFormatter()

    init {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.view_checkup_timer, this, true)
        timerTextView = root.findViewById(R.id.timer)

        updateTime(0)
    }

    fun updateTime(elapsedSeconds: Long) {
        timerTextView.text = formatter.format(elapsedSeconds)
    }
}
