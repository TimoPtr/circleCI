/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import io.reactivex.Flowable

/** Utility that allows other components to be up to date with [BrushingModeManager]'s state */
internal interface BrushingModeStateObserver {

    /**
     * Emits every state change in [BrushingModeManager]
     *
     * @return [BrushingModeState] [Flowable]
     */
    fun brushingModeStateFlowable(): Flowable<BrushingModeState>
}
