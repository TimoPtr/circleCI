/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.kolibree.android.app.ui.fragment.BaseFragment
import com.kolibree.android.app.ui.widget.CheckupView
import com.kolibree.android.app.ui.widget.bindCheckupData
import com.kolibree.android.app.ui.widget.bindShouldRender
import com.kolibree.android.app.ui.widget.bindShowData
import com.kolibree.android.homeui.hum.R
import com.kolibree.kml.MouthZone16

internal class DayCheckupFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_day_checkup, container, false).apply {
        findViewById<CheckupView>(R.id.checkup_view).apply {
            setNeglectedZoneColor(ContextCompat.getColor(context, R.color.neglectedZoneColor))
            setCleanZoneColor(ContextCompat.getColor(context, R.color.cleanZoneColor))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<CheckupView>(R.id.checkup_view).apply {
            val checkupData = checkupData()
            bindShouldRender(true)
            bindCheckupData(checkupData)
            bindShowData(showData = true, isManualBrushing = checkupData.isEmpty())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkupData(): Map<MouthZone16, Float> {
        val checkupData = mutableMapOf<MouthZone16, Float>()
        MouthZone16.values().forEach { zone ->
            val zoneData = arguments?.get(zone.name) as? Float?
            zoneData?.let { checkupData[zone] = it }
        }
        return checkupData
    }

    companion object {

        fun create(checkupData: Map<MouthZone16, Float>) =
            DayCheckupFragment().apply {
                val args = Bundle()
                checkupData.entries.forEach {
                    args.putFloat(it.key.name, it.value)
                }
                arguments = args
            }
    }
}
