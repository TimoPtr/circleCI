/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.activity.mvi.MVIUnityPlayerLifecycleActivity
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.commons.ToothbrushModel

@VisibleForApp
abstract class BasePirateCompatActivity :
    MVIUnityPlayerLifecycleActivity<
        PirateViewState,
        PirateViewModel.Factory,
        PirateViewModel
        >() {

    /**
     * We start a new intent in order to navigate to the next activity, so we need to manually set the
     * result to the intent
     */
    private var activityResult = RESULT_CANCELED

    override fun getViewModelClass(): Class<PirateViewModel> = PirateViewModel::class.java

    override fun onUnityGameFinished(result: UnityGameResult<*>) {
        innerSetResult(if (result.success) RESULT_OK else RESULT_CANCELED)
    }

    override fun beforeSendResultIntent(intent: Intent) {
        super.beforeSendResultIntent(intent)

        intent.putExtra(EXTRA_PIRATE_NEW_BRUSHING, activityResult == RESULT_OK)
    }

    override fun onBackPressed() {
        innerSetResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        activityResult = RESULT_CANCELED
    }

    private fun innerSetResult(result: Int) {
        activityResult = result
        setResult(result)
    }

    @Keep
    companion object {

        /**
         * Create a pirate intent to use to open the game from another activity
         *
         * @param context current context
         * @param toothbrushModel model of the toothbrush that will be used
         * @param macAddress of the toothbrush that will be used
         * @param activityAfterGameFinish activity's class to open when finishing the game
         * @return pirate game intent
         */
        @Keep
        fun <
            AFTER_FINISH : AppCompatActivity,
            HOST : MVIUnityPlayerLifecycleActivity<*, *, *>
            > createPirateCompatIntent(
                context: Context,
                hostActivity: Class<HOST>,
                activityAfterGameFinish: Class<AFTER_FINISH>,
                toothbrushModel: ToothbrushModel,
                macAddress: String
            ): Intent = context.createGameIntent(
            hostActivity = hostActivity,
            activityAfterGameFinish = activityAfterGameFinish
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(INTENT_TOOTHBRUSH_MODEL, toothbrushModel)
            putExtra(INTENT_TOOTHBRUSH_MAC, macAddress)
        }
    }
}

@Keep
const val EXTRA_PIRATE_NEW_BRUSHING = "extra_pirate_new_brushing"
