/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers.base

import android.app.Application

/**
 * Allows to initialize stuff along with Application creation.
 */
interface AppInitializer {

    /**
     * Is executed during [Application.onCreate].
     *
     * Should not perform any blocking operations.
     * If required please schedule job to background thread.
     *
     * MainThread main not exist at this moment.
     * Inappropriate usage may lead to deadlock.
     */
    fun initialize(application: Application)
}
