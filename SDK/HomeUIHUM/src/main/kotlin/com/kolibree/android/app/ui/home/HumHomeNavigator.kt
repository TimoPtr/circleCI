/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.navigation.NavigationHelper
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.headspace.mindful.HeadspaceMindfulMomentNavigator
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge

@VisibleForApp
@Suppress("TooManyFunctions")
interface HumHomeNavigator : HomeNavigator, HeadspaceMindfulMomentNavigator, PermissionNavigator,
    NavigationHelper {

    fun showCoachPlusWithoutStartScreen(mac: String, model: ToothbrushModel)

    fun showSettingsScreen(withInitialAction: SettingsInitialAction? = null)

    fun showDeleteBrushingSessionConfirmationDialog(
        deletionConfirmedCallback: () -> Unit,
        deletionCanceledCallback: () -> Unit
    )

    fun showLowBatteryDialog(toothbrushName: String)

    fun showHeadReplacementDialog()

    fun navigateToShopTab()

    fun showChallengeCompletedScreen(smiles: Int)

    fun openEarningPointsTermsAndConditions()

    fun showQuestionOfTheDay(question: QuestionOfTheDay)

    fun showCelebrationScreen(challenges: List<CompleteEarnPointsChallenge>)

    fun navigatesToSmilesHistory()

    fun showProductSupport()

    fun showOralCareSupport()
}
