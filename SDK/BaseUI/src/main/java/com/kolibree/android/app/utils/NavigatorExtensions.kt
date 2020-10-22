/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils

import android.os.Bundle
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.kolibree.android.extensions.runOnMainThread
import timber.log.Timber

/**
 * Allows to execute [NavController.navigate] from any thread,
 * although you should always try to call it from the main thread.
 * Scheduling action to the main looper may cause unwanted delays.
 *
 * By design [NavController.navigate] method should be only called from the main thread.
 * Otherwise you may encounter exception coming from underlying libraries.
 *
 * It also fixes IllegalArgumentException when trying to navigate
 * to the current destination.
 *
 * @see <a href="https://stackoverflow.com/a/54146679">stackoverflow</a>
 */
@AnyThread
@Keep
fun NavController.navigateSafe(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navExtras: Navigator.Extras? = null
) {
    val action = { navigateOnMainThread(resId, args, navOptions, navExtras) }

    if (Looper.getMainLooper().isCurrentThread) {
        action.invoke()
    } else {
        action.runOnMainThread()
        Timber.w(
            "NavigateSafe was executed from a background thread. You should try to call it on the main thread."
        )
    }
}

/**
 * @suppress UnsafeNavigate - this is the only place where
 * we can call [NavController.navigate] directly.
 */
@MainThread
@SuppressWarnings("UnsafeNavigate")
private fun NavController.navigateOnMainThread(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navExtras: Navigator.Extras? = null
) {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)

    if (action == null) {
        Timber.e("Unable to get NavAction for $resId")
        return
    }

    if (action.destinationId == currentDestination?.id) {
        Timber.w("Already in the given destination.")
        return
    }

    navigate(resId, args, navOptions, navExtras)
}
