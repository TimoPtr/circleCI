/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.navigation

import androidx.annotation.IntDef

@Deprecated(message = "Remove once V1 UI is dropped")
data class NavigateAction(
    @ScreenId @get:JvmName("screenId") val screenId: Int,
    @get:JvmName("extra") val extra: Any?,
    @get:JvmName("multiToothbrushes") val multiToothbrushes: Boolean
) {
    companion object {

        const val EXTRA_MAC = "extra_mac"
        const val EXTRA_MODEL = "extra_model"
        const val EXTRA_MULTI_TB = "extra_multi_tb"

        @JvmStatic
        fun create(@ScreenId screenId: Int, multiToothbrushes: Boolean): NavigateAction {
            return create(screenId, null, multiToothbrushes)
        }

        @JvmStatic
        fun create(@ScreenId screenId: Int): NavigateAction {
            return create(screenId, null, false)
        }

        @JvmStatic
        fun create(@ScreenId screenId: Int, extra: Any?): NavigateAction {
            return NavigateAction(screenId, extra, false)
        }

        @JvmStatic
        fun create(
            @ScreenId screenId: Int,
            extra: Any?,
            multiToothbrushes: Boolean
        ): NavigateAction {
            return NavigateAction(screenId, extra, multiToothbrushes)
        }
    }
}

@kotlin.annotation.Retention
@IntDef(
    MainActivityNavigationController.MY_TOOTHBRUSHES_SCREEN,
    MainActivityNavigationController.TOOTHBRUSH_SCREEN,
    MainActivityNavigationController.SETUP_TOOTHBRUSH_SCREEN,
    MainActivityNavigationController.DASHBOARD_DETAILS_SCREEN,
    MainActivityNavigationController.CHECKUP_SCREEN,
    MainActivityNavigationController.WELCOME_SCREEN,
    MainActivityNavigationController.OTA_UPDATE_SCREEN,
    MainActivityNavigationController.SAVE_DATA_BY_EMAIL_SCREEN,
    MainActivityNavigationController.CONNECTION_HELP_SCREEN,
    MainActivityNavigationController.COACH_SCREEN,
    MainActivityNavigationController.COACH_SCREEN_MANUAL,
    MainActivityNavigationController.PIRATE_SCREEN,
    MainActivityNavigationController.COACH_PLUS_SCREEN_MANUAL,
    MainActivityNavigationController.COACH_PLUS_SCREEN,
    MainActivityNavigationController.GRANT_LOCATION_SCREEN,
    MainActivityNavigationController.TEST_BRUSHING_SCREEN,
    MainActivityNavigationController.OFFLINE_BRUSHING_SCREEN,
    MainActivityNavigationController.TEST_ANGLES_SCREEN,
    MainActivityNavigationController.SPEED_CONTROL_SCREEN
)
@Deprecated(message = "Remove once V1 UI is dropped")
internal annotation class ScreenId
