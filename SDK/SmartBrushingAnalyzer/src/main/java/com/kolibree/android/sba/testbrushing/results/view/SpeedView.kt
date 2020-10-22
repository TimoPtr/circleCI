package com.kolibree.android.sba.testbrushing.results.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.kolibree.android.mouthmap.logic.SpeedResult
import com.kolibree.android.sba.R

internal class SpeedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Renderable<SpeedResult> {

    init {
        initView()
    }

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_speed, this, true)
    }
}
