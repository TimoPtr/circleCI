/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.BlinkScanResultUseCase
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Observable
import javax.inject.Inject

internal class BlinkFirstScanResultUseCase @Inject constructor(
    private val pairingAssistant: PairingAssistant,
    private val blinkScanResultUseCase: BlinkScanResultUseCase
) {

    /**
     * Observable that will attempt to blink the first scanner result
     *
     * @return [Observable]<[BlinkEvent]>
     */
    fun blinkFirstScanResult(): Observable<BlinkEvent> {
        return pairingAssistant
            .scannerObservable()
            .take(1)
            .switchMap(blinkScanResultUseCase::blink)
    }
}
