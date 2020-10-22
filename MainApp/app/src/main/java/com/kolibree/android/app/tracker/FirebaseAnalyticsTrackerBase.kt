/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.tracker

import androidx.annotation.VisibleForTesting
import com.kolibree.android.tracker.logic.AnalyticsTracker
import com.kolibree.android.tracker.logic.userproperties.UserProperties.ALL_PROPERTIES

@VisibleForTesting
const val NOT_AVAILABLE: String = "n/a"

internal abstract class FirebaseAnalyticsTrackerBase : AnalyticsTracker {

    protected abstract fun logEvent(eventName: String, arguments: Map<String, String>?)

    protected abstract fun setUserProperty(key: String, value: String)

    override fun sendEvent(eventName: String, details: Map<String, String?>) {
        val detailsWithoutUserProperties = details.toMutableMap().apply {
            updateUserAndToothbrushProperties(this)
            ALL_PROPERTIES.forEach { remove(it) }
        }

        val arguments = HashMap<String, String>()
        for ((key, value) in detailsWithoutUserProperties) {
            arguments[key] = value ?: NOT_AVAILABLE
        }

        logEvent(
            sanitizeEventName(eventName),
            if (arguments.isEmpty()) null else arguments
        )
    }

    @Synchronized
    private fun updateUserAndToothbrushProperties(argumentsAndProperties: Map<String, String?>) {
        val diff = argumentsAndProperties.entries
            .filter { entry -> ALL_PROPERTIES.contains(entry.key) }
            .map { Pair(it.key, it.value ?: NOT_AVAILABLE) }

        diff.forEach {
            setUserProperty(it.first, it.second)
        }
    }

    private fun sanitizeEventName(eventName: String): String {
        return eventName.replace(
            SANITIZE_EVENT_NAME_REGEX.toRegex(),
            SANITIZE_EVENT_NAME_SUBSTITUTE
        )
    }

    companion object {

        private const val SANITIZE_EVENT_NAME_REGEX = "[^A-Za-z0-9]+"
        private const val SANITIZE_EVENT_NAME_SUBSTITUTE = "_"
    }
}
