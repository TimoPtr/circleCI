/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.speedcontrol.mvi.confirmation.SpeedControlConfirmationFragment
import com.kolibree.android.app.ui.activity.BaseActivity.Companion.INTENT_TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.activity.BaseActivity.Companion.INTENT_TOOTHBRUSH_MODEL
import com.kolibree.android.app.ui.activity.BaseDaggerActivity
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.android.translationssupport.TranslationContext

@Keep
class SpeedControlActivity : BaseDaggerActivity(),
    NonTrackableScreen, UserStep {

    internal lateinit var macAddress: String

    internal lateinit var toothbrushModel: ToothbrushModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_control)
        extractIntentExtra()
    }

    override fun onBackPressed() {
        if (getCurrentNavFragment(R.id.nav_host_fragment) is SpeedControlConfirmationFragment) {
            finish()
        } else super.onBackPressed()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TranslationContext(newBase))
    }

    private fun extractIntentExtra() {
        macAddress = checkNotNull(readMacFromIntent()) { "mac is null" }
        toothbrushModel = readModelFromIntent()
    }
}

@Keep
fun createSpeedControlIntent(
    context: Context,
    macAddress: String,
    toothbrushModel: ToothbrushModel
): Intent =
    Intent(context, SpeedControlActivity::class.java).apply {
        putExtra(INTENT_TOOTHBRUSH_MAC, macAddress)
        putExtra(INTENT_TOOTHBRUSH_MODEL, toothbrushModel)
    }
