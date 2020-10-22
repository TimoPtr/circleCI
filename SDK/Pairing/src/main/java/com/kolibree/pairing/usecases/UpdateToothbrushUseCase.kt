/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.pairing.usecases

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.RNNDetector
import com.kolibree.android.tracker.studies.StudiesRepository
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.UpdateToothbrushData
import com.kolibree.sdkws.utils.ApiSDKUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/** Utility to let the backend keep a track of paired brushes */
@Keep
interface UpdateToothbrushUseCase {

    /**
     * Observes on io() by default
     */
    fun updateToothbrush(connection: KLTBConnection): Single<UpdateToothbrushResponse>
}

/** [UpdateToothbrushUseCase] implementation */
internal class UpdateToothbrushUseCaseImpl @Inject constructor(
    private val connector: IKolibreeConnector,
    private val apiSdkUtils: ApiSDKUtils,
    private val studiesRepository: StudiesRepository
) : UpdateToothbrushUseCase {

    override fun updateToothbrush(connection: KLTBConnection): Single<UpdateToothbrushResponse> =
        createUpdateToothbrushData(connection)
            .flatMap { connector.updateToothbrush(it) }
            .subscribeOn(Schedulers.io())
            .doOnSuccess { response ->
                studiesRepository.addStudy(
                    connection.toothbrush().mac,
                    response.tokens.firstOrNull { it.studyName.isBlank().not() }?.studyName
                )
            }

    @VisibleForTesting
    fun createUpdateToothbrushData(connection: KLTBConnection): Single<UpdateToothbrushData> =
        connection
            .userMode()
            .profileId()
            .map { profileId ->
                UpdateToothbrushData(
                    connection.toothbrush().serialNumber,
                    connection.toothbrush().mac,
                    apiSdkUtils.deviceId
                ).apply {
                    fwVersion = connection.toothbrush().firmwareVersion.toBinary()
                    setGruVersion(getBinaryVersion(connection.detectors().mostProbableMouthZones()))
                    hwVersion = connection.toothbrush().hardwareVersion.toBinary()
                    setProfileId(profileId)
                }
            }

    /**
     * Return the binary version of RNNDetector, or -1 if it's null.
     *
     * @param detector the RNNDetector
     * @return the binary gru data version, or -1
     */
    @VisibleForTesting
    fun getBinaryVersion(detector: RNNDetector?): Long? {
        return detector?.gruDataVersion?.toBinary()
    }
}
