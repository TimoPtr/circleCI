/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.app.ui.ota.databinding.ActivityOtaUpdateBinding
import com.kolibree.android.app.ui.ota.inprogress.InProgressOtaFragment
import com.kolibree.android.app.utils.keepScreenOn
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.tracker.NonTrackableScreen

internal class OtaUpdateActivity : BaseMVIActivity<
    OtaUpdateViewState,
    OtaUpdateActions,
    OtaUpdateViewModel.Factory,
    OtaUpdateViewModel,
    ActivityOtaUpdateBinding>(), NonTrackableScreen {

    val params: OtaUpdateParams by lazy {
        OtaUpdateParams(
            readIsMandatoryFromIntent(),
            checkNotNull(readMacFromIntent()) {
                "mac address should not be null please use startOtaUpdateIntent method"
            },
            readModelFromIntent()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We want to make sure that the phone stay awake during the whole OTA process
        keepScreenOn()
    }

    override fun getViewModelClass(): Class<OtaUpdateViewModel> = OtaUpdateViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_ota_update

    override fun execute(action: OtaUpdateActions) {
        // no-op
    }

    override fun onBackPressed() {
        when {
            getCurrentlyVisibleFragment() is InProgressOtaFragment -> {
                // no-op update in progress
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun getCurrentlyVisibleFragment(): Fragment? =
        getCurrentNavFragment(R.id.nav_host_fragment)

    private fun readIsMandatoryFromIntent(): Boolean =
        if (intent.hasExtra(INTENT_IS_MANDATORY)) intent.getBooleanExtra(
            INTENT_IS_MANDATORY,
            false
        ) else throw IllegalArgumentException("isMandatory flag should be given please use startOtaUpdateIntent method")
}

@Keep
fun startOtaUpdateScreen(
    context: Context,
    isMandatory: Boolean,
    mac: String,
    toothbrushModel: ToothbrushModel
) {
    context.startActivity(startOtaUpdateIntent(context, isMandatory, mac, toothbrushModel))
}

// Used by Espresso to be able to start the activity directly
@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun startOtaUpdateIntent(
    context: Context,
    isMandatory: Boolean,
    mac: String,
    toothbrushModel: ToothbrushModel
): Intent =
    Intent(context, OtaUpdateActivity::class.java).apply {
        putExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC, mac)
        putExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL, toothbrushModel)
        putExtra(INTENT_IS_MANDATORY, isMandatory)
    }

private const val INTENT_IS_MANDATORY = "intent_is_mandatory"
