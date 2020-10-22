/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.interfaces

import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.kolibree.sdkws.brushing.wrapper.IBrushing

@Keep
interface LocalBrushingsProcessor {

    /**
     * To be invoked internally when a new Brushing has been added to local DataStore.
     *
     * Note: this method doesn't deduplicate content. It's caller's responsibility
     * to make sure the same brushing isn't added twice.
     */
    @WorkerThread
    suspend fun onBrushingCreated(brushing: IBrushing)

    /**
     * To be invoked internally when a new batch of Brushing has been added to local DataStore
     *
     * Note: this method doesn't deduplicate content. It's caller's responsibility
     * to make sure the same brushings aren't added twice.
     */
    @WorkerThread
    suspend fun onBrushingsCreated(brushings: List<IBrushing>)

    /**
     * To be invoked internally when a new Brushing has been removed
     *
     * Note: this method doesn't deduplicate content. It's caller's responsibility
     * to make sure the same brushing isn't removed twice.
     */
    @WorkerThread
    suspend fun onBrushingRemoved(brushing: IBrushing)
}
