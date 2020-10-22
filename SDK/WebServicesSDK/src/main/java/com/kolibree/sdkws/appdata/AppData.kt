/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.appdata

import androidx.annotation.Keep
import org.threeten.bp.ZonedDateTime

/**
 * Versioned app data class
 *
 * Contains the app's JSON data and keeps track of dates and versions
 */
@Keep
interface AppData {

    /**
     * Get the instant the data has been created at
     *
     * @return non null [ZonedDateTime]
     */
    fun getDateTime(): ZonedDateTime

    /**
     * Get the version of the data structure
     *
     * @return int
     */
    fun getDataVersion(): Int

    /**
     * Get the app proprietary data
     *
     * @return non null [String]
     */
    fun getData(): String

    /**
     * Get the ID of the profile the data is associated to
     *
     * @return long profile ID
     */
    fun getProfileId(): Long
}
