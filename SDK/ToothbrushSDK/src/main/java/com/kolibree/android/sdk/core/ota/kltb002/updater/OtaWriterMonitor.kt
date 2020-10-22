package com.kolibree.android.sdk.core.ota.kltb002.updater

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.error.FailureReason
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal fun monitorOtaWriteObservable(
    connection: KLTBConnection,
    otaWriteObservable: Observable<OtaUpdateEvent>
) = monitorOtaWriteObservable(ConnectionStateMonitor(connection), otaWriteObservable)

internal fun monitorOtaWriteObservable(
    connectionStateMonitor: ConnectionStateMonitor,
    otaWriteObservable: Observable<OtaUpdateEvent>
): Observable<OtaUpdateEvent> {
    return Observable.combineLatest(
        otaWriteObservable,
        connectionStateMonitor.connectionStateObservable()
            .distinctUntilChanged()
            .onTerminateDetach(),
        BiFunction<OtaUpdateEvent, KLTBConnectionState, OtaUpdateEvent> { otaUpdateEvent, connectionState ->
            if (connectionState != KLTBConnectionState.OTA) {
                throw FailureReason("Connection is not in OTA state!")
            }
            otaUpdateEvent
        })
        .takeWhile { otaUpdateEvent -> otaUpdateEvent.isProgressCompleted().not() }
        .distinctUntilChanged()
}
