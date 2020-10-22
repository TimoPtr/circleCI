/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.tester

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.bi.getAvroBrushingMode
import com.kolibree.android.game.bi.getCalibrationData
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.game.bi.Contract.ActivityName.FREE_BRUSHING
import com.kolibree.android.game.bi.Contract.Handedness.UNKNOWN
import com.kolibree.android.game.bi.mapToothbrushModelToAvroToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.createConnection
import com.kolibree.android.sdk.error.InvalidConnectionStateException
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.FreeBrushingAppContext
import com.kolibree.kml.IntVector
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.PlaqlessData
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.RawData
import com.kolibree.pairing.assistant.PairingAssistant
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import org.threeten.bp.Duration
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class FreeBrushingTester private constructor(
    private val brushingDuration: Duration,
    private val appVersions: KolibreeAppVersions
) : ConnectionTester {

    // Should return the processedBrushingData and avro bytes
    fun testFor(
        service: KolibreeService,
        pairingAssistant: PairingAssistant,
        toothbrushScanResult: ToothbrushScanResult
    ): Single<FreeBrushingReport> = pairingAssistant
        .unpair(
            toothbrushScanResult
                .mac
        ) // forget the TB if already known to avoid being already connected
        .onErrorResumeNext { Completable.complete() } // otherwise we don't care go ahead
        .andThen(Single.defer {
            connectAndStartFreeBrushing(
                service,
                pairingAssistant,
                toothbrushScanResult
            ).doOnSuccess {
                Timber.e("final result $it")
            }
        }).finalizeConnection(pairingAssistant, toothbrushScanResult.mac)

    /**
     * Make sure that at in any case we unpair the connection
     */
    private fun <T> Single<T>.finalizeConnection(pairingAssistant: PairingAssistant, macAddress: String): Single<T> =
        onErrorResumeNext {
            Timber.e("unpaired after failure")
            pairingAssistant.unpair(macAddress).andThen(Single.error(it))
        }.flatMap { result ->
            pairingAssistant.unpair(macAddress)
                .andThen(Single.defer {
                    Timber.e("unpaired")
                    Single.just(result)
                })
        }.onTerminateDetach()

    /**
     * Create the connection and invokes doBrushing
     */
    private fun connectAndStartFreeBrushing(
        service: KolibreeService,
        pairingAssistant: PairingAssistant,
        toothbrushScanResult: ToothbrushScanResult
    ): Single<FreeBrushingReport> {
        // Create the connection to the TB without establishing it
        val connection = toothbrushScanResult.run {
            service.createConnection(mac, name, model)
        }

        return pairingAssistant
            .pair(toothbrushScanResult)
            .timeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS, Schedulers.computation())
            .flatMapOnActive(connection, doBrushing(service.applicationContext, connection))
    }

    /**
     * FlatMap with single given when the state is Active, if state is NEW and ESTABLISHING it's ignored,
     * otherwise an exception is thrown
     * Subscribe to the given single when the connection state is ACTIVE, if state is NEW/ESTABLISHING nothing happen,
     * If before single complete the state is not NEW or ESTABLISHING or ACTIVE then it throw an error
     */
    private fun <T> Single<T>.flatMapOnActive(
        connection: KLTBConnection,
        single: Single<FreeBrushingReport>
    ): Single<FreeBrushingReport> {
        val connectionRelay = PublishRelay.create<KLTBConnectionState>()
        val stateListener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(connection: KLTBConnection, newState: KLTBConnectionState) {
                connectionRelay.accept(newState)
            }
        }

        return doOnSuccess {
            Timber.d("register stateListener")
            connection.state().register(stateListener)
        }
            .flatMapObservable { connectionRelay }
            .switchMap {
                when (it) {
                    KLTBConnectionState.NEW, KLTBConnectionState.ESTABLISHING -> Observable.never<FreeBrushingReport>()
                    KLTBConnectionState.ACTIVE -> single.toObservable() // continue
                    else -> Observable.error(InvalidConnectionStateException("got $it"))
                }
            }
            .take(1) // Only active state will emit a success once we received a success we want to unregister the state
            .singleOrError()
            .doOnEvent { _, _ ->
                // We don't use doFinally because we want to unregister before the disconnection happen
                Timber.d("unregister stateListener")
                connection.state().unregister(stateListener)
            }
    }

    /**
     * Start a brushing for a given amount of time.
     * Flow :
     * 0. init daggerComponent
     * 1. start vibration
     * 2. start freeBrushingAppContext
     * 3. invokes subscribeToSensors and stop it after the given amount of time
     * 4. invokes finalizeBrushing
     */
    private fun doBrushing(context: Context, connection: KLTBConnection): Single<FreeBrushingReport> {
        val component = DaggerFreeBrushingTesterComponent.factory()
            .create(context, connection.toothbrush().model)
        val freeBrushing = component.freeBrushingProvider().get()

        return connection.vibrator().on()
            .doOnComplete {
                freeBrushing.start()
            }
            .andThen(
                subscribeToSensors(freeBrushing, connection)
                    .timeout(
                        brushingDuration.toMillis(),
                        TimeUnit.MILLISECONDS,
                        Schedulers.computation(),
                        Observable.just(false)
                    )
            ).take(1).singleOrError()
            .finalizeBrushing(connection, freeBrushing)
    }

    /**
     * Subscribe to possible sources of data (rawData and PlaqlessData) to feed the appContext.
     * It doesn't emit anything, but it need to be completed in order to close the streams.
     * It always send Running to the appContext
     */
    private fun subscribeToSensors(freeBrushing: FreeBrushingAppContext, connection: KLTBConnection):
        Observable<Boolean> =
        Flowable.merge(
            getRawDataFlowable(connection)
                .doOnNext {
                    freeBrushing.addRawData(it, PauseStatus.Running)
                }.flatMap {
                    Flowable.never<Boolean>()
                },
            getPlaqlessDataFlowable(connection).doOnNext {
                freeBrushing.addPlaqlessData(it, PauseStatus.Running)
            }.flatMap {
                Flowable.never<Boolean>()
            }
        ).toObservable()

    /**
     * After the brushing we finalize it by stoping the vibration and generate the processedBrushing json
     */
    private fun <T> Single<T>.finalizeBrushing(
        connection: KLTBConnection,
        freeBrushing: FreeBrushingAppContext
    ): Single<FreeBrushingReport> =
        flatMapCompletable {
            connection.vibrator().off().doOnError { Timber.w("Fail to turn off vibrator") }.onErrorComplete()
        }
            .andThen(Single.defer {
                val avro = freeBrushing.getAvro(getBrushingSession(connection)).toList()
                val pb = ProcessedBrushing(freeBrushing.processFullBrushing()).toJSON()
                Single.just(
                    FreeBrushingReport(pb, avro)
                )
            }).doOnSuccess {
                Timber.i("Brushing done !")
            }

    private fun getBrushingSession(connection: KLTBConnection) = BrushingSession(
        -1,
        -1,
        connection.toothbrush().mac,
        mapToothbrushModelToAvroToothbrushModel(connection.toothbrush().model),
        connection.toothbrush().hardwareVersion.toString(),
        connection.toothbrush().firmwareVersion.toString(),
        appVersions.appVersion,
        FREE_BRUSHING,
        UNKNOWN,
        getAvroBrushingMode(connection.toothbrush().model),
        dateFormatter.format(TrustedClock.getNowZonedDateTime()),
        IntVector(),
        getCalibrationData(connection),
        false
    )

    /**
     * Given the toothbrush model it subscribe to the right stream of data and map it to kml RawData model
     * it start the detector when we subscribe to the Flowable and it stop the stream when the Flowable is
     * complete/dispose
     */
    private fun getRawDataFlowable(connection: KLTBConnection): Flowable<RawData> =
        if (connection.toothbrush().model == ToothbrushModel.PLAQLESS) {
            connection.detectors().plaqlessRawDataNotifications().map { it.convertToKmlRawData() }
        } else {
            val processor = PublishProcessor.create<RawData>()
            val rawDataListener =
                RawDetectorListener { _, sensorState -> processor.offer(sensorState.convertToKmlRawData()) }
            processor
                .doOnSubscribe {
                    Timber.e("enable rawData")
                    connection.detectors().enableRawDataNotifications()
                    connection.detectors().rawData().register(rawDataListener)
                }
                .doFinally {
                    Timber.e("disable rawData")
                    // TODO check if it work since the connection will be kill before disable rawdata
                    connection.detectors().disableRawDataNotifications()
                    connection.detectors().rawData().unregister(rawDataListener)
                }
        }

    /**
     * Given the toothbrush model it subscribe to the right stream of data and map it to kml RawData model
     * it start the detector when we subscribe to the Flowable and it stop the stream when the Flowable is
     * complete/dispose
     *
     * Do nothing when the tb is not plaqless
     */
    private fun getPlaqlessDataFlowable(connection: KLTBConnection): Flowable<PlaqlessData> =
        if (connection.toothbrush().model == ToothbrushModel.PLAQLESS) {
            connection.detectors().plaqlessNotifications().map { it.convertToKmlPlaqlessData() }
        } else {
            Flowable.never<PlaqlessData>()
        }

    companion object {

        private const val CONNECTION_TIMEOUT_SECONDS = 30L

        private const val BRUSHING_SESSION_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

        @VisibleForTesting
        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(BRUSHING_SESSION_DATETIME_PATTERN)

        fun create(
            brushingDuration: Duration = Duration.ofMinutes(2),
            appVersions: KolibreeAppVersions
        ): FreeBrushingTester =
            FreeBrushingTester(brushingDuration, appVersions)
    }
}

data class FreeBrushingReport(val processedBrushing: String, val avroData: List<Char>)

/**
 * Dagger Component used to get all the dependency to create a FreeBrushingAppContext
 */
@Component(modules = [FreeBrushingTesterModule::class])
internal interface FreeBrushingTesterComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            @BindsInstance toothbrushModel: ToothbrushModel
        ): FreeBrushingTesterComponent
    }

    fun freeBrushingProvider(): Provider<FreeBrushingAppContext>
}

@Module(includes = [ProcessedBrushingsModule::class, RnnWeightProviderModule::class, KpiSpeedProviderModule::class])
object FreeBrushingTesterModule {

    @Provides
    @JvmStatic
    @Suppress("LongParameterList")
    fun provideFreeBrushingAppContext(
        rnnWeightProvider: RnnWeightProvider?,
        angleProvider: AngleProvider,
        kpiSpeedProvider: KpiSpeedProvider?,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel
    ): FreeBrushingAppContext {

        checkNotNull(rnnWeightProvider) {
            "no weight available"
        }

        checkNotNull(kpiSpeedProvider) {
            "no speed kpi available"
        }

        return FreeBrushingAppContext(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator(),
            toothbrushModel.hasOverPressure()
        )
    }
}
