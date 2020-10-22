/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker

import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.validator.EventNameValidator

/**
 * Universal carrier for analytics data. Contains event name and collection of additional data
 * as collection of string key-value pairs.
 *
 * @param name Name of the event. Non-null & mandatory.
 * @param details Collection of string key-value pairs. Optional, null by default.
 * @param parent Parent event to this one. Optional, null by default.
 */
@Keep
class AnalyticsEvent @JvmOverloads constructor(
    name: String,
    details: Map<String, String?>? = null,
    parent: AnalyticsEvent? = null
) {
    /**
     * Name of the event. If parent was passed to the constructor, it contains parent and child
     * names, combined with parent's name `_` (for ex. `ParentName_ChildName`)
     */
    val name: String

    /**
     * Collection of string key-value pairs, carrying additional event data (values can be null).
     * If parent was passed to the constructor, it contains both parent and child details
     * (if both were present). Note that child details has precedence (can nullify or override
     *  value from parent if it contains the same key).
     */
    val details: Map<String, String?>?

    init {
        this.name = parent?.let { "${it.name}_$name" } ?: name
        this.details = composeDetails(parent?.details, details)

        FailEarly.failInConditionMet(
            condition = !EventNameValidator.isValidEventName(this.name),
            message = "'${this.name}' is not valid event name"
        )
    }

    /**
     * Convenience operator for combining event and a string (string is treated as event
     * without details).
     * @param subEventName name of child event we want to combine.
     */
    operator fun plus(subEventName: String): AnalyticsEvent =
        AnalyticsEvent(parent = this, name = subEventName)

    /**
     * Convenience operator for combining 2 events.
     * @param event event we want to combine
     */
    operator fun plus(event: AnalyticsEvent): AnalyticsEvent =
        AnalyticsEvent(parent = this, name = event.name, details = event.details)

    /**
     * Convenience operator for adding new detail entry to details map
     * @param detail details entry we want to add
     */
    operator fun plus(detail: Pair<String, String?>): AnalyticsEvent =
        AnalyticsEvent(name = name, details = composeDetails(details, mapOf(detail)))

    private fun composeDetails(
        lhs: Map<String, String?>?,
        rhs: Map<String, String?>?
    ): Map<String, String?>? = when {
        lhs == null && rhs == null -> null
        lhs == null -> rhs
        rhs == null -> lhs
        else -> lhs + rhs
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnalyticsEvent) return false

        if (name != other.name) return false
        if (details != other.details) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (details?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AnalyticsEvent(name='$name', details=$details)"
    }
}
