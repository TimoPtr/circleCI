/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.BlinkScanResultUseCase
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import io.reactivex.Observable
import javax.inject.Inject

/**
 * UseCase that will blink a [ToothbrushScanResult]
 *
 * If the connection is already established, it'll send the blink command immediately
 *
 * Otherwise, it establishes the connection and sends the blink command
 *
 * Once the connection reports that it's blinking, this UseCase sets the BlinkingConnection for the
 * pairing flow
 */
internal class ScanListBlinkConnectionUseCase @Inject constructor(
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val blinkScanResultUseCase: BlinkScanResultUseCase,
    private val blinkConnectionUseCase: BlinkConnectionUseCase
) : PairingFlowSharedFacade by pairingFlowSharedFacade {

    fun blink(result: ToothbrushScanResult): Observable<BlinkEvent> =
        Observable.defer {
            blinkingConnection()?.let { existingConnection ->
                if (existingConnection.toothbrush().mac == result.mac) {
                    return@defer blinkExistingConnection(existingConnection)
                }
            }

            blinkNewScanResult(result)
        }

    private fun blinkNewScanResult(result: ToothbrushScanResult): Observable<BlinkEvent> {
        return unpairBlinkingConnectionCompletable()
            .andThen(blinkScanResultUseCase.blink(result)
                .doOnNext {
                    if (it is BlinkEvent.Success) setBlinkingConnection(it.connection)
                }
            )
    }

    private fun blinkExistingConnection(connection: KLTBConnection): Observable<BlinkEvent> {
        return blinkConnectionUseCase.blink(connection)
    }
}
