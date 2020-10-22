/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

// Do not use outside of BrushingModeManagerImpl /!\
internal class BrushingModeChangedUseCase @Inject constructor() {

    /**
     * @return a Flowable that will emit non-duplicated BrushingMode changed notifications
     */
    fun brushingModeChangedStream(deviceParametersCharacteristicStream: Flowable<ByteArray>): Flowable<ByteArray> {
        return deviceParametersCharacteristicStream
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .filter { it.first() == BRUSHING_MODE_PARAMETER }
            .distinctUntilChanged { previous, incoming -> previous.contentEquals(incoming) }
    }
}
