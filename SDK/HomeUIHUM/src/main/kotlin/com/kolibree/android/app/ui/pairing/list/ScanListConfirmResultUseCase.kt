/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.ui.pairing.brush_found.BrushFoundConfirmConnectionUseCase
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * UseCase to pair a [ToothbrushScanResult] from Scan List screen
 */
internal class ScanListConfirmResultUseCase @Inject constructor(
    private val scanListBlinkConnectionUseCase: ScanListBlinkConnectionUseCase,
    private val brushFoundConfirmConnectionUseCase: BrushFoundConfirmConnectionUseCase
) {
    fun confirm(
        scanResult: ToothbrushScanResult,
        doOnSubscribeBlock: () -> Unit = {}
    ): Observable<BlinkEvent> {
        /*
        This relies on [ScanListBlinkConnectionUseCase] to set the blinking connection
         */
        return scanListBlinkConnectionUseCase.blink(scanResult)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { doOnSubscribeBlock() }
            .flatMap { blinkEvent ->
                maybeConfirmConnection(blinkEvent)
                    .andThen(Observable.just(blinkEvent))
            }
    }

    private fun maybeConfirmConnection(blinkEvent: BlinkEvent): Completable {
        return if (blinkEvent is BlinkEvent.Success) {
            brushFoundConfirmConnectionUseCase.maybeConfirmConnection()
        } else {
            Completable.complete()
        }
    }
}
