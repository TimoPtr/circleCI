/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.sample.showcase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.widget.SpeedometerView
import com.kolibree.android.baseui.hum.R

@VisibleForApp
class SpeedometerShowcaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_speedomenter_showcase)

        val speedometer1: SpeedometerView = findViewById(R.id.one)
        val speedometer2: SpeedometerView = findViewById(R.id.two)
        val speedometer3: SpeedometerView = findViewById(R.id.three)

        val seekbar: SeekBar = findViewById(R.id.seekbar)
        val seekText: TextView = findViewById(R.id.seek_value)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progress.toFloat().also { floatProgress ->
                    speedometer1.smoothPositionTo(floatProgress)
                    speedometer2.smoothPositionTo(floatProgress)
                    speedometer3.smoothPositionTo(floatProgress)
                }

                seekText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { /*NO-OP */ }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { /*NO_OP*/ }
        })
    }
}

@Keep
fun startSpeedometerPlaygroundIntent(context: Context) {
    context.startActivity(Intent(context, SpeedometerShowcaseActivity::class.java))
}
