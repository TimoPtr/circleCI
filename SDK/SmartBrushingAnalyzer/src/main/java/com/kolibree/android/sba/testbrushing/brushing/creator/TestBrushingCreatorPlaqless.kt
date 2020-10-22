/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing.creator

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.game.bi.Contract.ActivityName.FREE_BRUSHING
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.FreeBrushingAppContext
import com.kolibree.kml.PauseStatus
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import timber.log.Timber

/**
 * Class implements BrushingCreator interface. Responsible for collecting toothbrush's raw data and plaqless data
 * and creating appropriate brushing object. After the creation process, the class will insert new
 * brushing object to the local database and notifies backend, that new brushing has been already created.
 * This class uses [ActivityScope] and can be shared only between [TestBrushingActivity] fragments.
 */
// TODO merge it with TestBrushingCreatorKMLImpl https://kolibree.atlassian.net/browse/KLTB002-8660
internal class TestBrushingCreatorPlaqless @Inject constructor(
    private val checkupCalculator: CheckupCalculator,
    rnnWeightProvider: RnnWeightProvider,
    angleProvider: AngleProvider,
    kpiSpeedProvider: KpiSpeedProvider,
    transitionProvider: TransitionProvider,
    thresholdProvider: ThresholdProvider,
    zoneValidatorProvider: ZoneValidatorProvider,
    toothbrushModel: ToothbrushModel,
    private val connector: IKolibreeConnector,
    private val appVersions: KolibreeAppVersions,
    @VisibleForTesting internal val avroCreator: KmlAvroCreator,
    @VisibleForTesting internal val appContext: FreeBrushingAppContext = FreeBrushingAppContext(
        rnnWeightProvider.getRnnWeight(),
        angleProvider.getKPIAngle(),
        kpiSpeedProvider.getKpiSpeed(),
        transitionProvider.getTransition(),
        thresholdProvider.getThresholdBalancing(),
        zoneValidatorProvider.getZoneValidator(),
        toothbrushModel.hasOverPressure()
    )
) : TestBrushingCreator {

    @VisibleForTesting
    internal val disposables = CompositeDisposable()

    @VisibleForTesting
    internal val isPlaying = AtomicBoolean(false)

    override fun start(connection: KLTBConnection) {
        disposables += connection.detectors().plaqlessNotifications()
            .subscribeOn(Schedulers.io())
            .subscribe(this::onPlaqlessSensorState, Timber::e)
        disposables += connection.detectors().plaqlessRawDataNotifications()
            .subscribeOn(Schedulers.io())
            .subscribe(this::onPlaqlessRawSensorState, Timber::e)
        isPlaying.set(true)
        appContext.start()
    }

    override fun resume(connection: KLTBConnection) {
        monitorCurrentBrushing(connection)
        isPlaying.set(true)
    }

    override fun pause(connection: KLTBConnection) {
        monitorCurrentBrushing(connection)
        isPlaying.set(false)
    }

    override fun create(connection: KLTBConnection): CheckupData {
        disposables.clear()

        if (appContext.isFullBrushingProcessingPossible) {
            val processedBrushing16 = appContext.processFullBrushing()
            val processedBrushing = processedBrushing16.toProcessedBrushing()
            val processedData = processedBrushing.toJSON()
            val checkupData = checkupCalculator.calculateCheckup(processedBrushing)
            val brushingData = createBrushingData(connection, checkupData, processedData)

            connector.currentProfile?.let {
                createBrushing(it, brushingData)
            }

            disposables += avroCreator.createBrushingSession(connection, FREE_BRUSHING)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable {
                    avroCreator.submitAvroData(appContext.getAvro(it))
                }
                .subscribe({ Timber.i("AVRO data submitted") }, Timber::e)

            return checkupData
        } else {
            throw ProcessedBrushingNotAvailableException() // TODO handle this
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposables.clear()
    }

    override fun notifyReconnection() {
        Timber.i("Notify KML of a reconnection")
        appContext.notifyReconnection()
    }

    @VisibleForTesting
    fun onPlaqlessSensorState(plaqlessSensorState: PlaqlessSensorState) {
        val pauseStatus = if (isPlaying.get()) PauseStatus.Running else PauseStatus.InPause

        appContext.addPlaqlessData(
            plaqlessSensorState.convertToKmlPlaqlessData(),
            pauseStatus
        )
    }

    @VisibleForTesting
    fun onPlaqlessRawSensorState(plaqlessRawSensorState: PlaqlessRawSensorState) {
        val pauseStatus = if (isPlaying.get()) PauseStatus.Running else PauseStatus.InPause
        appContext.addRawData(
            plaqlessRawSensorState.convertToKmlRawData(),
            pauseStatus
        )
    }

    @VisibleForTesting
    fun createBrushing(profile: Profile, brushingData: CreateBrushingData) {
        val createBrushingCompletable = Completable.fromAction {
            connector.withProfileId(profile.id).createBrushing(brushingData)
        }

        disposables +=
            createBrushingCompletable
                .subscribeOn(Schedulers.io())
                .subscribe({ }, Timber::e)
    }

    @VisibleForTesting
    fun createBrushingData(
        connection: KLTBConnection,
        checkupData: CheckupData,
        processedData: String
    ): CreateBrushingData {

        val brushingData = initBrushingData(checkupData)

        brushingData.coverage = checkupData.surfacePercentage
        brushingData.setProcessedData(processedData)

        // Add support data
        brushingData.addSupportData(
            connection.toothbrush().serialNumber,
            connection.toothbrush().mac,
            appVersions.appVersion,
            appVersions.buildVersion
        )

        return brushingData
    }

    @VisibleForTesting
    fun initBrushingData(checkupData: CheckupData) = CreateBrushingData(
        GameApiConstants.GAME_SBA,
        checkupData.duration,
        DEFAULT_BRUSHING_GOAL,
        checkupData.dateTime,
        checkupData.surfacePercentage
    )

    @VisibleForTesting
    fun monitorCurrentBrushing(connection: KLTBConnection) {
        disposables +=
            connection.brushing()
                .monitorCurrent()
                .subscribe({}, Timber::e)
    }
}
