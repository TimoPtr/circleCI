/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.ui.ota.GruwareFilter
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.toothbrushupdate.OtaUpdateType.MANDATORY
import com.kolibree.android.toothbrushupdate.OtaUpdateType.MANDATORY_NEEDS_INTERNET
import com.kolibree.android.toothbrushupdate.OtaUpdateType.STANDARD
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import timber.log.Timber

/**
 *
 */
@Keep
class OtaChecker
@Inject internal constructor(
    private val serviceProvider: ServiceProvider,
    private val gruwareInteractor: GruwareInteractor,
    private val networkChecker: NetworkChecker,
    private val featureToggleSet: FeatureToggleSet,
    private val availableUpdatesFilter: GruwareFilter
) : ConnectionStateListener {
    @VisibleForTesting
    internal var otaForConnectionSubject = PublishSubject.create<OtaForConnection>()

    @VisibleForTesting
    val connections = mutableSetOf<KLTBConnection>()

    @VisibleForTesting
    internal val disposables = CompositeDisposable()

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        if (newState === ACTIVE) {
            disposables.addSafely(
                otaForConnectionMaybe(connection)
                    .onTerminateDetach()
                    .subscribe(
                        otaForConnectionSubject::onNext,
                        Timber::e
                    )
            )
        }
    }

    /**
     * Checks if any KLTBConnection needs an Over The Air (OTA) update
     *
     * If it's a mandatory update and we can't fetch the data to update the toothbrush, the
     * Observable will emit a OtaForConnection with type MANDATORY_NEEDS_INTERNET.
     *
     * Other possible types include MANDATORY and STANDARD.
     *
     * MANDATORY and MANDATORY_NEEDS_INTERNET need immediate action from the user. Since the
     * toothbrush is in an unusable state, they are not safe to ignore.
     *
     * STANDARD updates will flag that a connection needs to be updated. This can be checked on any
     * connection by subscribing to hasOtaObservable()
     *
     * If there's no mandatory update AND there's no internet, the observable won't emit an item for
     * that KLTBConnection
     *
     * @return Observable<OtaForConnection> that will emit [0-N] OtaForConnection, where N is the
     * number of KLTBConnection. It will complete once all connections have been checked, or if
     * there isn't any toothbrush paired or none of the toothbrushes has state ACTIVE or OTA, the
     * returned Observable will complete
     */
    fun otaForConnectionsOnce(): Observable<OtaForConnection> {
        return Observable.merge(checkConnectionsFromServiceObservable(), otaForConnectionSubject)
            .doFinally { stopListeningToConnectionState() }
            .doFinally { disposables.clear() }
    }

    @VisibleForTesting
    internal fun stopListeningToConnectionState() {
        connections.forEach { it.state().unregister(this) }
    }

    @VisibleForTesting
    internal fun checkConnectionsFromServiceObservable(): Observable<OtaForConnection> {
        return serviceProvider.connectOnce()
            .flatMapObservable(::validConnectionsOnce)
            .doOnNext(::maybeListenToConnectionState)
            .filter(::filterCheckConnectionOta)
            .flatMapMaybe(::otaForConnectionMaybe)
    }

    private fun validConnectionsOnce(service: KolibreeService): Observable<KLTBConnection> {
        return if (service.knownConnections.isEmpty()) {
            /*
            merge only completes when both sources complete

            If there are no connections to check, the only way to complete otaForConnectionsOnce is
            to also emit emit complete on otaForConnectionSubject
             */
            Observable.empty<KLTBConnection>()
                .doOnComplete { completeAndRefreshSubject() }
        } else {
            Observable.fromIterable(service.knownConnections)
        }
    }

    @VisibleForTesting
    fun completeAndRefreshSubject() {
        otaForConnectionSubject.onComplete().also {
            otaForConnectionSubject = PublishSubject.create<OtaForConnection>()
        }
    }

    @VisibleForTesting
    fun filterCheckConnectionOta(connection: KLTBConnection) = connection.state().current
        .let { state -> state == ACTIVE || state == OTA }

    @VisibleForTesting
    internal fun maybeListenToConnectionState(connection: KLTBConnection) {
        if (!connections.contains(connection) && !connection.isActive()) {
            connection.state().register(this)
            connections.add(connection)
        }
    }

    @VisibleForTesting
    internal fun otaForConnectionMaybe(connection: KLTBConnection): Maybe<OtaForConnection> {
        // Check GRU data and bootloader mode (force update)
        if (connectionNeedsMandatoryOTAUpdate(connection)) {
            return gruwareInteractor.getGruware(connection)
                .flatMapMaybe {
                    Maybe.just(OtaForConnection(connection, MANDATORY, it))
                }
                .onErrorResumeNext(Function<Throwable, MaybeSource<OtaForConnection>> {
                    if (it is NetworkNotAvailableException) {
                        it.printStackTrace()

                        listenToNetworkState(connection)

                        return@Function Maybe.just(
                            OtaForConnection(
                                connection,
                                MANDATORY_NEEDS_INTERNET
                            )
                        )
                    }

                    Maybe.error(it)
                })
        }

        return connectionFirmwareUpdateMaybe(connection)
            .doOnComplete { completeAndRefreshSubject() }
    }

    /**
     * Checks if any of the connections needs a mandatory update (e.g., is in bootloader)
     *
     * @return true if the KLTBConnection needs a mandatory update, false otherwise
     */
    @VisibleForTesting
    internal fun connectionNeedsMandatoryOTAUpdate(connection: KLTBConnection): Boolean {
        if (!connectionSupportsOTA(connection)) {
            return false
        }

        if (connection.toothbrush().isRunningBootloader) {
            return true
        }

        return needsGruUpdate(connection)
    }

    @VisibleForTesting
    internal fun listenToNetworkState(connection: KLTBConnection) {
        disposables.addSafely(
            networkChecker.connectivityStateObservable()
                .filter { isConnected -> isConnected }
                .take(1)
                .singleOrError()
                .flatMapMaybe { otaForConnectionMaybe(connection).retry(1) }
                .subscribe(
                    otaForConnectionSubject::onNext,
                    Timber::e
                )
        )
    }

    @VisibleForTesting
    fun needsGruUpdate(connection: KLTBConnection): Boolean {
        if (connection.supportsGRUUpdates()) {
            val rnnDetector = connection.detectors().mostProbableMouthZones()

            return rnnDetector != null && !rnnDetector.hasValidGruData()
        }

        return false
    }

    /**
     * Checks if the KLTBConnection needs a firmware update
     *
     * If the connection doesn't support OTA, it emits an empty Maybe
     *
     * Otherwise, it checks if there's a Firmware update. In case of network not being available,
     * it'll emit an empty Maybe. If there's no update available for the KLTBConnection, it also
     * emits an empty Maybe
     *
     * @return a Maybe that will emit empty or a OtaForConnection
     */
    @VisibleForTesting
    internal fun connectionFirmwareUpdateMaybe(connection: KLTBConnection): Maybe<OtaForConnection> {
        if (connectionSupportsOTA(connection)) {
            return gruwareDataSingle(connection)
                .flatMapMaybe { onGruwareDataForConnection(connection, it) }
                .onErrorComplete { it is NetworkNotAvailableException }
        }

        Timber.w("Connection $connection doesn't support OTA, returning Maybe.empty()!")
        return Maybe.empty()
    }

    @VisibleForTesting
    internal fun connectionSupportsOTA(connection: KLTBConnection): Boolean {
        return modelSupportsOTA(connection.toothbrush().model) &&
            connectionStateSupportsOTA(connection.state().current)
    }

    @VisibleForTesting
    fun modelSupportsOTA(model: ToothbrushModel): Boolean {
        /*
        There's a bug that prevents us from completing an OTA with custom protocol on Android 5 & 6. It's probably
        related to BT cache

        Can be removed when https://github.com/NordicSemiconductor/Android-BLE-Library/issues/108 is fixed
         */
        if (model == ToothbrushModel.ARA || model == ToothbrushModel.CONNECT_E1) {
            return featureToggleSet.toggleForFeature(AlwaysOfferOtaUpdateFeature).value ||
                androidVersion() >= Build.VERSION_CODES.N
        }

        return true
    }

    @VisibleForTesting
    fun androidVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    private fun connectionStateSupportsOTA(state: KLTBConnectionState) =
        state === ACTIVE || state === OTA

    @VisibleForTesting
    internal fun gruwareDataSingle(connection: KLTBConnection): Single<GruwareData> {
        return gruwareInteractor.getGruware(connection)
    }

    /**
     * Given a KLTBConnection and a GruwareData, check if the connection needs to be updated
     *
     * @return a Maybe that will emit a OtaForConnection if the connection needs to updated, or empty
     * if no update is needed
     */
    @VisibleForTesting
    internal fun onGruwareDataForConnection(
        connection: KLTBConnection,
        gruwareData: GruwareData
    ): Maybe<OtaForConnection> {
        return filterGruwareData(connection, gruwareData)
            .flatMapMaybe { filteredGruwareData ->
                if (filteredGruwareData.isNotEmpty()) {
                    Maybe.just(OtaForConnection(connection, STANDARD, filteredGruwareData))
                } else {
                    Timber.w("Connection $connection doesn't have FW or GRU updates, returning Maybe.empty()!")
                    Maybe.empty()
                }
            }
    }

    private fun filterGruwareData(
        connection: KLTBConnection,
        gruwareData: GruwareData
    ): Single<GruwareData> {
        return availableUpdatesFilter.filterAvailableUpdates(connection, gruwareData)
    }
}

@Keep
enum class OtaUpdateType {
    STANDARD,
    MANDATORY,
    MANDATORY_NEEDS_INTERNET
}

@Keep
data class OtaForConnection(
    val connection: KLTBConnection,
    val otaUpdateType: OtaUpdateType,
    private val gruwareData: GruwareData? = null
) {
    init {
        if (gruwareData == null && (otaUpdateType == MANDATORY || otaUpdateType == STANDARD))
            throw IllegalArgumentException("GruwareData can't be null for STANDARD or MANDATORY updates")

        connection.tag = gruwareData
    }
}
