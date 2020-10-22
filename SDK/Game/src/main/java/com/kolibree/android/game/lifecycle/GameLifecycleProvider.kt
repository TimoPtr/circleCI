/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import androidx.annotation.Keep
import io.reactivex.Observable

@Keep
interface GameLifecycleProvider {

    /**
     * Observable that will emit [GameLifecycle]
     *
     * It won't emit any event until the Brushing either starts or exits
     *
     * It won't emit the same [GameLifecycle] two times in a row
     *
     * Events will be emitted on MainThread
     */
    fun gameLifecycleStream(): Observable<GameLifecycle>
}
