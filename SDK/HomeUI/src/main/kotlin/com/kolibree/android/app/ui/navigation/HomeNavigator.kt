/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.navigation

import android.content.Intent
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.HomeToolbarListener
import com.kolibree.android.commons.ToothbrushModel
import org.threeten.bp.OffsetDateTime

const val REQUEST_CODE_GAME = 104

// TODO split this into fine-grained interfaces
@SuppressWarnings("TooManyFunctions")
@VisibleForApp
interface HomeNavigator : HomeToolbarListener, ActivityResultAware {

    fun navigateToSetupToothbrushScreen()

    fun navigateToMyToothbrushesScreen()

    fun navigateToToothbrushScreen(mac: String)

    fun navigateToDayCheckup(forDate: OffsetDateTime)

    fun navigateToCheckup()

    fun showStartMessageView(toothbrushName: String)

    fun showNoModelToothbrushDialog()

    fun showNoToothbrushDialog()

    fun showMandatoryToothbrushUpdateDialog(mac: String, model: ToothbrushModel)

    fun showCoachPlus(mac: String, model: ToothbrushModel)

    fun showCoachPlusInManualMode()

    fun showTestBrushing(mac: String, model: ToothbrushModel)

    fun showSpeedControl(mac: String, model: ToothbrushModel)

    fun showTestAngles(mac: String, model: ToothbrushModel)

    fun showCoach(mac: String)

    fun showCoachInManualMode()

    fun showCheckoutScreen()

    fun showAmazonDashConnectScreen()

    fun finishAndNavigateToWelcomeScreen()

    fun navigateToEditProfile(profileId: Long)

    fun navigateTo(intent: Intent)
}

@VisibleForApp
interface ActivityResultAware {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}
