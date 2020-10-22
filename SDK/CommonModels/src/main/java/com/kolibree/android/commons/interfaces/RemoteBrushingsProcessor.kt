/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.interfaces

import androidx.annotation.Keep

@Keep
interface RemoteBrushingsProcessor {

    /**
     * To be invoked internally when a new batch of Brushing has been successfully added to remote endpoint
     */
    suspend fun onBrushingsCreated()

    /**
     * To be invoked internally when a new Brushing has been removed
     */
    suspend fun onBrushingsRemoved()
}
