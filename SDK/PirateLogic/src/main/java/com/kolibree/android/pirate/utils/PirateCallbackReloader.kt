/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.utils

import androidx.annotation.Keep
import com.kolibree.android.app.unity.KolibreeUnityPlayer
import com.kolibree.android.app.unity.UnityCallback
import com.kolibree.android.pirate.PirateCallback
import com.unity3d.player.UnityPlayer
import timber.log.Timber

/**
 * Responsible for reloading Pirate callback on the Unity side.
 *
 * Created by quent on 14/06/2017.
 * Updated by lookashc on 02/04/19
 */
@Keep
object PirateCallbackReloader {

    private var unityModuleName: String? = null

    @Keep
    @JvmStatic
    fun getInstance() = this

    /**
     * This method is being called by Pirate Unity game, not by our code.
     * Do not remove it!
     */
    @Keep
    @Suppress("unused")
    fun getCurrentCallback(): UnityCallback.Proxy<*>? {
        Timber.d("Unity is getting our current callback: ${KolibreeUnityPlayer.pirateCallback}")
        return KolibreeUnityPlayer.pirateCallback
    }

    fun setCurrentCallback(callback: PirateCallback.Proxy<*>?) {
        Timber.d("Setting new callback $callback")
        KolibreeUnityPlayer.pirateCallback = callback
        notifyPlayerToReload()
    }

    /**
     * This method is being called by Pirate Unity game, not by our code.
     * Do not remove it!
     */
    @Keep
    @Suppress("unused")
    fun registerModuleName(moduleName: String) {
        Timber.d("Unity called us: registerModuleName(module name = $moduleName)")
        this.unityModuleName = moduleName
    }

    private fun notifyPlayerToReload() {
        if (unityModuleName != null) {
            UnityPlayer.UnitySendMessage(unityModuleName, "NotifyNewCallback", "empty")
        }
    }
}
