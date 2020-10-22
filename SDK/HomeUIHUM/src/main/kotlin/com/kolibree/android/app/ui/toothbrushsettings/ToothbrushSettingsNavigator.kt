/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenContract
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult
import com.kolibree.android.app.ui.ota.startOtaUpdateScreen
import com.kolibree.android.app.ui.settings.help.startHelpActivity
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import zendesk.support.Support
import zendesk.support.guide.ViewArticleActivity

internal class ToothbrushSettingsNavigator : BaseNavigator<ToothbrushSettingsActivity>() {

    private lateinit var pairingStartScreenContract: ActivityResultLauncher<Unit>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        withOwner {
            setupPairingStartScreen()
        }
    }

    private fun ToothbrushSettingsActivity.setupPairingStartScreen() {
        pairingStartScreenContract =
            registerForActivityResult(PairingStartScreenContract()) { result ->
                if (result == PairingStartScreenResult.OpenShop) {
                    navigateToShop()
                }
            }
    }

    fun navigatesToPairingScreen() = withOwner {
        pairingStartScreenContract.launch(Unit)
        finish()
    }

    fun showHelp() = withOwner {
        startHelpActivity(this)
    }

    fun finishScreen() = withOwner {
        finish()
    }

    fun navigateToShop() = withOwner {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun showNotConnectingHelpCenter() = withOwner {
        getString(R.string.zendesk_articles_can_not_connect).toLongOrNull()?.let { articlesId ->
            FailEarly.failInConditionMet(
                !Support.INSTANCE.isInitialized,
                "Zendesk is not initialized"
            )
            ViewArticleActivity.builder(articlesId)
                .withContactUsButtonVisible(false)
                .show(this)
        } ?: FailEarly.fail("articlesId invalid")
    }

    fun navigateToOta(isMandatory: Boolean, mac: String, model: ToothbrushModel) = withOwner {
        startOtaUpdateScreen(this, isMandatory, mac, model)
    }
}
