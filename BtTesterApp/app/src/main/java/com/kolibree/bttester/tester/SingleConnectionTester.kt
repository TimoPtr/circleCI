package com.kolibree.bttester.tester

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.createConnection
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class SingleConnectionTester private constructor() : ConnectionTester {

    /**
     * Try to unpair before pairing to avoid being already connected to the TB
     */
    fun testFor(
        service: KolibreeService,
        pairingAssistant: PairingAssistant,
        toothbrushScanResult: ToothbrushScanResult
    ): Observable<String> =

        pairingAssistant
            .unpair(
                toothbrushScanResult
                    .mac
            ) // forget the TB if already known to avoid being already connected
            .onErrorResumeNext { Completable.complete() } // otherwise we don't care go ahead
            .andThen(Observable.defer {
                connectAndListenToStatus(
                    service,
                    pairingAssistant,
                    toothbrushScanResult
                )
            })

    private fun connectAndListenToStatus(
        service: KolibreeService,
        pairingAssistant: PairingAssistant,
        toothbrushScanResult: ToothbrushScanResult
    ): Observable<String> {

        val connectionStateRelay = BehaviorRelay.create<String>()
        val connectionStateListener: ConnectionStateListener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(connection: KLTBConnection, newState: KLTBConnectionState) {
                Timber.tag("single").d("onConnectionStateChanged $newState")
                connectionStateRelay.accept(formatState(newState))

                if (connection.state().current == KLTBConnectionState.ACTIVE) {
                    connectionStateRelay.accept(formatMessage("Starting automatic disconnect"))
                }
            }
        }

        // Create the connection to the TB without establishing it
        val connection = toothbrushScanResult.run {
            service.createConnection(mac, name, model)
        }

        connection.state().register(connectionStateListener)

        // the subscription to pair will use the previous connection created
        return Observable.merge(
            connectionStateRelay,
            pairingAssistant
                .pair(toothbrushScanResult)
                .timeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS, Schedulers.computation())
                .flatMapObservable {
                    pairingAssistant.unpair(toothbrushScanResult.mac).toObservable<String>()
                }).onTerminateDetach()
            .doOnError { Timber.d("SingleConnection pair error") }
            .doFinally {
                connection.state().unregister(connectionStateListener)
            }
    }

    private fun formatState(newState: KLTBConnectionState): String {
        return formatMessage(newState.toString())
    }

    private fun formatMessage(message: String): String {
        return TIME_FORMATTER.format(ZonedDateTime.now()) + ": " + message
    }

    companion object {

        private val TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME

        private const val CONNECTION_TIMEOUT_SECONDS = 30L

        fun create(): SingleConnectionTester {
            return SingleConnectionTester()
        }
    }
}
