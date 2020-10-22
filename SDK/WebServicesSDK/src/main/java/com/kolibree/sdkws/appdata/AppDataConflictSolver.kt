/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata

import androidx.annotation.Keep

/**
 * [AppData] conflict solver
 *
 * Check the [AppDataManager] documentation for more information
 */
@Keep
interface AppDataConflictSolver {

    /**
     * Called when there is a conflict in the app data versions
     *
     * @param serverData non null [AppData] that have been found on the server
     * @param lastSynchronized nullable last [AppData] that has been synchronized with the
     * server
     * @param lastSaved nullable last saved [AppData] that may contain the same data as the
     * last synchronized one
     * @return non null 'merged' [AppData] result of the ones above
     */
    fun onConflict(serverData: AppData, lastSynchronized: AppData?, lastSaved: AppData?): AppData
}
