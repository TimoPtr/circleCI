/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.main

import android.app.Activity
import android.content.Intent
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.bttester.R
import com.kolibree.bttester.databinding.ActivityMainBinding
import com.kolibree.bttester.freebrushing.FreeBrushingActivity
import com.kolibree.bttester.legacy.LegacyMainActivity
import com.kolibree.bttester.ota.mvi.OtaActivity
import com.kolibree.bttester.singleconnection.SingleConnectionActivity
import kotlin.reflect.KClass

internal class MainActivity : BaseMVIActivity<
    MainActivityViewState,
    MainActivityAction,
    MainActivityViewModel.Factory,
    MainActivityViewModel,
    ActivityMainBinding
    >() {

    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun execute(action: MainActivityAction) {
        when (action) {
            is OpenLegacyMainActivity -> openActivity(LegacyMainActivity::class)
            is OpenSingleConnectionActivity -> openActivity(SingleConnectionActivity::class)
            is OpenOtaActivity -> openActivity(OtaActivity::class)
            is OpenFreeBrushingActivity -> openActivity(FreeBrushingActivity::class)
        }
    }

    private fun openActivity(activityClass: KClass<out Activity>) = startActivity(Intent(this, activityClass.java))
}
