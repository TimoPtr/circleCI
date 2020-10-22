/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.game

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.kolibree.account.utils.ActiveToothbrushesForProfileUseCase
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import javax.inject.Inject

@SuppressLint("DeobfuscatedPublicSdkClass")
interface DefaultUserActivityUseCase {
    fun defaultActivity(): DefaultActivity
    fun registerForVibratingConnection(
        connection: KLTBConnection,
        callback: (ToothbrushModel) -> Unit
    )

    fun unregister()
}

internal class DefaultUserActivityUseCaseImpl @Inject constructor(
    private val activeToothbrushesForProfileUseCase: ActiveToothbrushesForProfileUseCase
) : DefaultUserActivityUseCase, VibratorListener {

    @VisibleForTesting
    var registeredConnection: KLTBConnection? = null
    @VisibleForTesting
    var callback: (ToothbrushModel) -> Unit = { _ -> }

    override fun registerForVibratingConnection(
        connection: KLTBConnection,
        callback: (ToothbrushModel) -> Unit
    ) {
        unregister()
        connection.vibrator().register(this)
        registeredConnection = connection
        this.callback = callback
    }

    override fun unregister() {
        registeredConnection?.vibrator()?.unregister(this)
    }

    override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
        if (on) {
            callback(connection.toothbrush().model)
            unregister()
        }
    }

    override fun defaultActivity(): DefaultActivity {
        val availableToothbrushes = activeToothbrushesForProfileUseCase.activeToothbrushes()
        return when (availableToothbrushes.size) {
            0 -> DefaultActivity.NoToothbrushConnected
            1 -> DefaultActivity.CoachPlusActivity(availableToothbrushes.first())
            else -> DefaultActivity.MultiToothbrush(availableToothbrushes)
        }
    }
}

sealed class DefaultActivity {
    data class CoachPlusActivity(val connection: KLTBConnection) : DefaultActivity()
    data class MultiToothbrush(val connections: List<KLTBConnection>) : DefaultActivity()
    object NoToothbrushConnected : DefaultActivity()
}
