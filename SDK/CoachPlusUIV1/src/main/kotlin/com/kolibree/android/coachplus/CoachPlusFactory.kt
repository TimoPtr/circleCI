/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.coachplus.mvi.CoachPlusActivity
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel
import javax.inject.Inject

@Keep
interface CoachPlusFactory {

    fun createConnectedCoach(
        context: Context,
        macAddress: String?,
        model: ToothbrushModel?,
        colorSet: CoachPlusColorSet? = null
    ): Intent

    fun createConnectedCoach(
        context: Context,
        macAddress: String?,
        model: ToothbrushModel?
    ): Intent

    // Tips : Manual mean here without TB
    fun createManualCoach(
        context: Context,
        colorSet: CoachPlusColorSet? = null
    ): Intent

    fun createManualCoach(
        context: Context
    ): Intent
}

internal class CoachPlusFactoryImpl @Inject constructor() : CoachPlusFactory {

    override fun createConnectedCoach(
        context: Context,
        macAddress: String?,
        model: ToothbrushModel?,
        colorSet: CoachPlusColorSet?
    ): Intent = createIntent(context, false, macAddress, model, colorSet)

    override fun createConnectedCoach(context: Context, macAddress: String?, model: ToothbrushModel?): Intent =
        createConnectedCoach(context, macAddress, model, null)

    override fun createManualCoach(
        context: Context,
        colorSet: CoachPlusColorSet?
    ): Intent = createIntent(context, true, colorSet = colorSet)

    override fun createManualCoach(context: Context): Intent = createManualCoach(context, null)
    /**
     * Create a launch intent for this activity.
     *
     * @param context [Context]
     * @param macAddress [String] toothbrush MAC address
     * @param colorSet [CoachPlusColorSet] color set
     * @return CoachPlusActivity launch [Intent]
     */
    private fun createIntent(
        context: Context,
        isManual: Boolean,
        macAddress: String? = null,
        model: ToothbrushModel? = null,
        colorSet: CoachPlusColorSet? = null
    ): Intent {
        val intent = Intent(context, CoachPlusActivity::class.java)

        if (macAddress != null) {
            intent.putExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC, macAddress)
            intent.putExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL, model)
        }
        intent.putExtra(INTENT_COLOR_SET, colorSet)
        intent.putExtra(EXTRA_MANUAL_MODE, isManual)

        return intent
    }

    companion object {
        internal const val INTENT_COLOR_SET = "intentColorSet"
        internal const val EXTRA_MANUAL_MODE = "manualMode"
    }
}
