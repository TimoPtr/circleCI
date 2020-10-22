/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.idlingresources

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingResource
import com.kolibree.android.annotation.VisibleForApp

/**
 * Custom idling resource that checks if idle
 * much more often than [IdlingResource].
 *
 * Check is performed every [IDLE_CHECK_DELAY_MS] milliseconds.
 *
 * By default Espresso checks isIdleNow every 5 seconds and it's not possible to change it.
 * @see [IdlingPolicies.getDynamicIdlingResourceWarningPolicy]
 *
 * In ideal world you should always use [IdlingResource.ResourceCallback]
 * to inform about idle changes. Unfortunately it's not easily achievable
 * for resources like [ViewVisibilityIdlingResource].
 *
 * If idling resource can use callback then you can ignore [EagerIdlingResource].
 * Otherwise it's always better to use it, because it's faster.
 */
@VisibleForApp
abstract class EagerIdlingResource(
    private val name: String
) {

    private var isIdle: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    private val idlingResource = object : IdlingResource {
        private var callback: IdlingResource.ResourceCallback? = null

        override fun getName() = this@EagerIdlingResource.name

        override fun isIdleNow(): Boolean {
            return isIdle
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
            checkIfIdle()
        }

        private fun checkIfIdle() {
            isIdle = isIdle()
            if (!isIdle) {
                handler.postDelayed(::checkIfIdle, IDLE_CHECK_DELAY_MS)
            } else {
                callback?.onTransitionToIdle()
            }
        }
    }

    abstract fun isIdle(): Boolean

    fun asIdlingResource() = idlingResource

    internal companion object {
        private const val IDLE_CHECK_DELAY_MS = 100L
    }
}
