/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import androidx.annotation.Keep

/**
 * JobService IDs have to be unique across application. In fact, if we have a collision with a third
 * party library, we'd be screwed
 *
 * The goal of this class is to centralize ids to avoid issues.
 * The [com.kolibree.android.app.initializers.WorkManagerAppInitializer] is using ids declared
 * between 10000 and 11000.
 */
@Keep
object JobServiceIdConstants {

    const val WORK_MANAGER_JOB_ID_USED_MIN = 10000
    const val WORK_MANAGER_JOB_ID_USED_MAX = 11000

    const val CLEAR_USER_CONTENT = 1001
    const val APP_CLEAR_USER_CONTENT = 1002

    const val EXTRACT_OFFLINE = 4242

    const val SCHEDULE_SCAN = EXTRACT_OFFLINE + 1

    const val RUN_SYNC_OPERATION = 678

    const val SYNC_IMMEDIATE = 9984354

    const val SYNC_WHEN_NETWORK = SYNC_IMMEDIATE + 1

    @JvmStatic
    fun contains(id: Int): Boolean = id in WORK_MANAGER_JOB_ID_USED_MIN..WORK_MANAGER_JOB_ID_USED_MAX
}
