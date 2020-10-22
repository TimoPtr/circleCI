/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.bi

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.CharVector
import com.kolibree.kml.DoubleVector
import com.kolibree.kml.IntVector
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.avro.AVRO_CACHE_DIRECTORY
import com.kolibree.sdkws.core.avro.AVRO_FILE_FORMAT
import com.kolibree.sdkws.core.avro.AvroFileUploader
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import org.threeten.bp.format.DateTimeFormatter

/** KML AVRO file generator */
@Keep
interface KmlAvroCreator {

    /**
     * Create a KML AVRO [BrushingSession]
     *
     * @param connection [KLTBConnection]
     * @param avroTransitionsTable [IntArray]
     * @param activityName [String]
     * @param isPlaqlessSupervised [Boolean] default is true and it has only impact when using Coach +
     * @return [BrushingSession] [Single]
     */
    fun createBrushingSession(
        connection: KLTBConnection,
        activityName: String,
        avroTransitionsTable: IntArray = IntArray(0),
        zonesSequence: Array<MouthZone16> = emptyArray(),
        isPlaqlessSupervised: Boolean = true
    ): Single<BrushingSession>

    /**
     * Validate and submit AVRO-formatted raw data
     *
     * @param data KML-provided [CharVector]
     * @return [Completable]
     */
    fun submitAvroData(data: CharVector): Completable
}

/** [KmlAvroCreator] implementation */
internal class KmlAvroCreatorImpl @Inject constructor(
    private val connector: IKolibreeConnector,
    private val appVersions: KolibreeAppVersions,
    private val avroFileUploader: AvroFileUploader,
    context: Context
) : KmlAvroCreator {

    private val appContext = context.applicationContext

    override fun createBrushingSession(
        connection: KLTBConnection,
        activityName: String,
        avroTransitionsTable: IntArray,
        zonesSequence: Array<MouthZone16>,
        isPlaqlessSupervised: Boolean
    ) = toothbrushProfileCompletable(connection)
        .map { profile ->
            BrushingSession(
                connector.accountId.toInt(),
                profile.id.toInt(),
                connection.toothbrush().mac,
                mapToothbrushModelToAvroToothbrushModel(connection.toothbrush().model),
                connection.toothbrush().hardwareVersion.toString(),
                connection.toothbrush().firmwareVersion.toString(),
                appVersions.appVersion,
                activityName,
                getHandedness(profile.handedness),
                getAvroBrushingMode(connection.toothbrush().model),
                dateFormatter.format(currentDate()),
                IntVector(),
                getCalibrationData(connection),
                isPlaqlessSupervised
            )
        }

    override fun submitAvroData(data: CharVector): Completable =
        writeAvroFileCompletable(data)
            .doOnComplete { avroFileUploader.uploadPendingFiles() }

    private fun currentDate() = TrustedClock.getNowZonedDateTime()

    @VisibleForTesting
    fun toothbrushProfileCompletable(connection: KLTBConnection) =
        connection
            .userMode()
            .profileId()
            .flatMap { connector.getProfileWithIdSingle(it) }
            .onErrorReturn { connector.currentProfile }

    @VisibleForTesting
    fun getHandedness(handedness: Handedness) = when (handedness) {
        Handedness.RIGHT_HANDED -> Contract.Handedness.RIGHT_HANDED
        Handedness.LEFT_HANDED -> Contract.Handedness.LEFT_HANDED
        Handedness.UNKNOWN -> Contract.Handedness.UNKNOWN
    }

    private fun writeAvroFileCompletable(data: CharVector) =
        Completable.fromAction {
            val avroDirectory = File(appContext.cacheDir, AVRO_CACHE_DIRECTORY)
            avroDirectory.mkdirs()

            val avroFile = File(avroDirectory, generateAvroFileName())
            avroFile.writeBytes(data.map { it.toByte() }.toByteArray())
        }
}

private const val BRUSHING_SESSION_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

@VisibleForApp
val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(BRUSHING_SESSION_DATETIME_PATTERN)

/*
M1 is always manual https://docs.google.com/document/d/1NeIzYrm-T_aB_QHYSEwg29Ex0M5LvgVsEti2Jcvir9A
*/
@VisibleForApp
fun getAvroBrushingMode(model: ToothbrushModel) =
    if (model === ToothbrushModel.CONNECT_M1)
        Contract.BrushingMode.MANUAL
    else
        Contract.BrushingMode.VIBRATING

@VisibleForApp
fun generateAvroFileName() =
    String.format(
        Locale.US,
        AVRO_FILE_FORMAT,
        UUID.randomUUID().toString()
    )

@VisibleForApp
fun getCalibrationData(connection: KLTBConnection) =
    DoubleVector(
        connection
            .detectors()
            .calibrationData
            .map { it.toDouble() }
    )

@VisibleForApp
fun mapToothbrushModelToAvroToothbrushModel(model: ToothbrushModel): String = when (model) {
    ToothbrushModel.ARA -> Contract.ToothbrushModelName.ARA
    ToothbrushModel.CONNECT_E1 -> Contract.ToothbrushModelName.E1
    ToothbrushModel.CONNECT_E2, ToothbrushModel.HILINK, ToothbrushModel.HUM_ELECTRIC -> Contract.ToothbrushModelName.E2
    ToothbrushModel.CONNECT_M1 -> Contract.ToothbrushModelName.M1
    ToothbrushModel.CONNECT_B1, ToothbrushModel.HUM_BATTERY -> Contract.ToothbrushModelName.B1
    ToothbrushModel.PLAQLESS -> Contract.ToothbrushModelName.PQL
    ToothbrushModel.GLINT -> Contract.ToothbrushModelName.GLI
}
