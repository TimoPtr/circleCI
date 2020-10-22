package com.kolibree.pairing.session

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.pairing.exception.KolibreeServiceDisconnectedException
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.jetbrains.annotations.NotNull
import timber.log.Timber

class PairingSessionCreatorImpl @Inject constructor(
    private val connector: IKolibreeConnector,
    private val gruwareRepository: GruwareRepository,
    private val accountToothbrushRepository: AccountToothbrushRepository,
    private val serviceProvider: ServiceProvider,
    private val connectionProvider: KLTBConnectionProvider,
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase
) : PairingSessionCreator {
    private companion object {
        const val IDENTIFY_RGB_RED: Byte = 0
        const val IDENTIFY_RGB_GREEN: Byte = 0
        const val IDENTIFY_RGB_BLUE: Byte = 100
        const val IDENTIFY_BLINK_PERIOD = 300
        const val IDENTIFY_BLINK_DURATION = 10000
    }

    override fun create(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<PairingSession> {
        return activeConnectionSingle(mac, model, name)
            .flatMap { pairToActiveProfileAndPersist(it) }
            .flatMap { maybeDiscardOfflineAndStopVibrator(it) }
            .map {
                PairingSessionImpl(
                    it, accountToothbrushRepository, gruwareRepository,
                    synchronizeBrushingModeUseCase
                )
            }
    }

    override fun create(
        @NotNull connection: KLTBConnection
    ): PairingSession {
        return PairingSessionImpl(
            connection, accountToothbrushRepository, gruwareRepository,
            synchronizeBrushingModeUseCase
        )
    }

    override fun connectAndBlinkBlue(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<KLTBConnection> {
        return activeConnectionSingle(mac, model, name)
            .flatMap(::blinkBlue)
    }

    override fun blinkBlue(connection: KLTBConnection): Single<KLTBConnection> {
        return connection.toothbrush().playLedSignal(
            IDENTIFY_RGB_RED,
            IDENTIFY_RGB_GREEN,
            IDENTIFY_RGB_BLUE,
            LedPattern.LONG_PULSE,
            IDENTIFY_BLINK_PERIOD,
            IDENTIFY_BLINK_DURATION
        )
            .onTerminateDetach()
            .toSingle { connection }
    }

    fun pairToActiveProfileAndPersist(activeConnection: KLTBConnection): Single<KLTBConnection> {
        return updateProfileIdUnlessBootloader(activeConnection)
            .andThen(persistToothbrush(activeConnection.toothbrush()))
            .andThen(Single.just(activeConnection))
    }

    @VisibleForTesting
    fun maybeDiscardOfflineAndStopVibrator(activeConnection: KLTBConnection): Single<KLTBConnection> {
        val activeConnectionSingle = Single.just(activeConnection)
        return if (!isInBootloader(activeConnection)) {
            activeConnection.brushing().monitorCurrent()
                .andThen(activeConnection.vibrator().off())
                .andThen(activeConnectionSingle)
        } else {
            activeConnectionSingle
        }
    }

    @VisibleForTesting
    fun updateProfileIdUnlessBootloader(activeConnection: KLTBConnection): Completable {
        if (!isInBootloader(activeConnection)) {
            return updateProfileId(activeConnection)
        }
        return Completable.complete()
    }

    @VisibleForTesting
    internal fun isInBootloader(connection: KLTBConnection) =
        connection.toothbrush().isRunningBootloader

    @VisibleForTesting
    fun updateProfileId(connection: KLTBConnection): Completable =
        connection.userMode().setProfileId(currentProfileId())

    @VisibleForTesting
    fun currentProfileId() = connector.currentProfile?.id ?: 0L

    @VisibleForTesting
    fun activeConnectionSingle(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<KLTBConnection> {
        return createConnectionSingle(mac, model, name)
            .flatMap {
                connectionProvider.existingActiveConnection(mac)
                    .doOnDispose { onUserDisposedConnection(it) }
                    .doOnError { Timber.e(it) }
            }
            .observeOn(Schedulers.io())
    }

    @VisibleForTesting
    fun onUserDisposedConnection(connection: KLTBConnection) {
        connection.disconnect()
    }

    @VisibleForTesting
    fun createConnectionSingle(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<KLTBConnection> {
        return serviceProvider.connectOnce()
            .map { it.createAndEstablishConnection(mac, model, name) }
            .onErrorResumeNext {
                Timber.e(it)

                Single.error(KolibreeServiceDisconnectedException())
            }
    }

    @VisibleForTesting
    fun persistToothbrush(toothbrush: Toothbrush): Completable {
        return accountToothbrushRepository.associate(
            toothbrush,
            connector.accountId,
            currentProfileId()
        )
            .ignoreElement()
    }
}
