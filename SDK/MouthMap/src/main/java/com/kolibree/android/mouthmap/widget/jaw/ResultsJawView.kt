/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.widget.jaw

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.jaws.color.ColorJawsView
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.mouthmap.R
import com.kolibree.kml.MouthZone16

@Keep
class ResultsJawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        initView()
    }

    private var isOpenJawsMode = true
    lateinit var jawsView: ColorJawsView

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.view_results_jaw, this, true)

        initJawViews(root)
    }

    private fun initJawViews(root: View) {
        jawsView = root.findViewById(R.id.jaws)
    }

    fun colorMouthZones(zones: ColorMouthZones) {
        jawsView.setColorMouthZones(zones)
    }

    fun colorMouthZones(zones: Map<MouthZone16, Float>) {
        jawsView.setColorMouthZones(zones)
    }

    fun toggleJawsMode() {
        if (isOpenJawsMode) {
            jawsView.closeJaws()
        } else {
            jawsView.openJaws()
        }
        isOpenJawsMode = !isOpenJawsMode
    }
}

@Keep
@BindingAdapter("colorMouthZones")
fun ResultsJawView.bindColorMouthZones(zones: ColorMouthZones?) {
    zones?.let { colorMouthZones(it) }
}
