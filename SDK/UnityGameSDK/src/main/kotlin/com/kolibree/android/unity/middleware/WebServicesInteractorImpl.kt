/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.annotation.SuppressLint
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.app.lifecycle.LifecycleDisposableScope
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.android.utils.callSafely
import com.kolibree.android.utils.onErrorReturnNull
import com.kolibree.game.middleware.CharVector
import com.kolibree.game.middleware.ProfileGender
import com.kolibree.game.middleware.ProfileHandedness
import com.kolibree.game.middleware.WebServicesInteractor
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

typealias MiddlewareProfile = com.kolibree.game.middleware.Profile
typealias KmlCharVector = com.kolibree.kml.CharVector

@SuppressLint("ExperimentalClassUse")
internal class WebServicesInteractorImpl(
    private val lifecycleDisposableScopeOwner: LifecycleDisposableScopeOwner,
    private val profile: IProfile,
    private val connector: IKolibreeConnector,
    private val gameProgressRepository: GameProgressRepository,
    private val checkupCalculator: CheckupCalculator,
    private var appVersions: KolibreeAppVersions,
    private val goalDurationTime: Duration,
    private val avroCreator: KmlAvroCreator
) : WebServicesInteractor(), LifecycleDisposableScope by lifecycleDisposableScopeOwner {

    init {
        lifecycleDisposableScopeOwner.monitoredClassName = javaClass.simpleName
    }

    override fun currentProfile(): MiddlewareProfile =
        MiddlewareProfile(
            connector.accountId.toInt(),
            profile.id.toInt(),
            profile.firstName,
            profile.gender.toMiddlewareType(),
            profile.handedness.toMiddlewareType(),
            profile.brushingGoalTime
        )

    override fun getGameProgress(gameId: String): String? = onErrorReturnNull {
        gameProgressRepository.getProgress(profile.id, gameId).progress
    }

    override fun updateGameProgress(gameId: String, progress: String) = callSafely {
        gameProgressRepository.saveProgress(profile.id, gameId, progress).blockingAwait()
    }

    @Suppress("TooGenericExceptionCaught")
    override fun uploadBrushing(
        gameId: String,
        processedData: String,
        macAddress: String,
        serial: String,
        startTimeInSeconds: Long,
        durationInMilliseconds: Long,
        avro: CharVector
    ) {
        try {
            val duration = Duration.ofMillis(durationInMilliseconds)
            val result = checkupCalculator.calculateCheckup(processedData, startTimeInSeconds, duration)
            val coverage = result.surfacePercentage
            val startTime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(startTimeInSeconds),
                TrustedClock.systemZoneOffset)
            val createBrushingData =
                CreateBrushingData(
                    gameId, duration,
                    goalDurationTime.seconds.toInt(),
                    startTime, 0
                )
            createBrushingData.coverage = coverage
            createBrushingData.setProcessedData(processedData)
            createBrushingData.addSupportData(serial, macAddress, appVersions.appVersion, appVersions.buildVersion)
            connector.withProfileId(profile.id).createBrushing(createBrushingData)
            avroCreator.submitAvroData(KmlCharVector(avro)).blockingAwait()
        } catch (e: Exception) {
            Timber.e(e, "Error when uploading a brushing")
        }
    }
}

internal fun Handedness.toMiddlewareType(): ProfileHandedness = when (this) {
    Handedness.LEFT_HANDED -> ProfileHandedness.LEFT_HANDED
    Handedness.RIGHT_HANDED -> ProfileHandedness.RIGHT_HANDED
    Handedness.UNKNOWN -> ProfileHandedness.UNKNOWN
}

internal fun Gender.toMiddlewareType(): ProfileGender = when (this) {
    Gender.FEMALE -> ProfileGender.FEMALE
    Gender.MALE -> ProfileGender.MALE
    Gender.PREFER_NOT_TO_ANSWER, Gender.UNKNOWN -> ProfileGender.UNKNOWN
}
