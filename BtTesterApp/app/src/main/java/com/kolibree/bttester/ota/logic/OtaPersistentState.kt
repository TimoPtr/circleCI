/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.logic

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.bttester.ota.mvi.OtaViewState
import javax.inject.Inject

private const val PREFERENCES_NAME = "OtaToothbrushPreviousState"
private const val KEY_TB_MODEL = "$PREFERENCES_NAME.KEY_TB_MODEL"
private const val KEY_TB_MAC = "$PREFERENCES_NAME.KEY_TB_MAC"
private const val KEY_NUMBER_OF_ITERATIONS = "$PREFERENCES_NAME.KEY_NUMBER_OF_ITERATIONS"

class OtaPersistentState @Inject constructor(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    internal fun storeState(otaViewState: OtaViewState) {
        sharedPreferences.edit()
            .putString(KEY_TB_MODEL, otaViewState.toothbrushModel.internalName)
            .putString(KEY_TB_MAC, otaViewState.macAddress)
            .putInt(KEY_NUMBER_OF_ITERATIONS, otaViewState.numberOfIterations ?: 1)
            .apply()
    }

    internal fun retrieveState(): OtaViewState =
        OtaViewState(
            toothbrushModel = sharedPreferences.getString(KEY_TB_MODEL, null)?.let {
                ToothbrushModel.getModelByInternalName(it)
            } ?: ToothbrushModel.CONNECT_E1,
            macAddress = sharedPreferences.getString(KEY_TB_MAC, null),
            numberOfIterations = sharedPreferences.getInt(KEY_NUMBER_OF_ITERATIONS, 1)
        )
}
