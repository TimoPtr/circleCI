package com.kolibree.android.offlinebrushings

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.GameApiConstants.GAME_OFFLINE
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.LegacyProcessedBrushingFactory
import com.kolibree.android.sdk.connection.brushing.DEFAULT_GOAL_DURATION
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt
import org.threeten.bp.OffsetDateTime

// TODO before merge the PR just as reminder add kml feature toggle

internal data class OfflineBrushingsDataMapper(
    private val kolibreeConnector: IKolibreeConnector,
    private val processedBrushingFactory: LegacyProcessedBrushingFactory,
    private val checkupCalculator: CheckupCalculator,
    val offlineBrushing: OfflineBrushing,
    private val isMultiUserMode: Boolean,
    private val userId: Long,
    private val toothbrushSerial: String,
    val toothbrushMac: String
) {

    /**
     * Creates a CreateBrushingData instance given the fields combination
     *
     * @return a CreateBrushingData ready to be uploaded to the server
     */
    @WorkerThread
    fun createBrushingData(appVersions: KolibreeAppVersions): CreateBrushingData {
        val data = CreateBrushingData(
            GAME_OFFLINE,
            sessionDurationInSeconds(), // Seconds needed
            targetBrushingTime(),
            datetime(),
            0
        )

        if (processedData().isNotEmpty()) {
            val checkupData = checkupData()
            data.coverage = checkupData.surfacePercentage
            data.setProcessedData(processedData())
        }

        data.addSupportData(
            toothbrushSerial,
            toothbrushMac,
            appVersions.appVersion,
            appVersions.buildVersion
        )

        return data
    }

    fun createOrphanBrushing(): OrphanBrushing =
        OrphanBrushing.create(
            sessionDurationInSeconds(),
            targetBrushingTime(),
            processedData(),
            datetime(),
            toothbrushSerial,
            toothbrushMac
        )

    fun datetime(): OffsetDateTime = offlineBrushing.datetime.atOffset(TrustedClock.systemZoneOffset)

    private fun sessionDurationInSeconds(): Long {
        return offlineBrushing.duration.seconds
    }

    /**
     * @return 50 if processed data is empty; otherwise, it calculates the
     * quality from checkupData
     */
    @VisibleForTesting
    fun quality(): Int {
        if (processedData().isEmpty()) { // V1 or multi user mode
            return EMPTY_DATA_QUALITY
        }

        val quality = checkupData().surfacePercentage

        // see http://jira.kolibree.com/browse/KLTB002-666
        return min(
            MAX_QUALITY,
            (quality * MAX_PERCENT / RECTIFYING_RATIO).toFloat()
        ).roundToInt()
    }

    private fun checkupData() = checkupCalculator.calculateCheckup(
        processedData(),
        offlineBrushing.datetime.toEpochSecond(TrustedClock.systemZoneOffset),
        offlineBrushing.duration
    )

    /**
     * @return empty String if there's no processed data, a json string otherwise
     */
    @VisibleForTesting
    fun processedData(): String = offlineBrushing.processedData

    /**
     * Retuns target brushing time in seconds
     *
     * @return main profile's target brushing time if isMultiUserMode is true; otherwise, the target
     * brushing time of the given profile
     */
    @VisibleForTesting
    fun targetBrushingTime(): Int {
        return if (isMultiUserMode && kolibreeConnector.ownerProfile != null) {
            kolibreeConnector.ownerProfile!!.brushingGoalTime
        } else kolibreeConnector.getProfileWithId(userId)?.brushingGoalTime ?: defaultBrushingTime
    }

    /**
     * @return the main profile id if isMultiUserMode; otherwise, toothbrush's ownerId
     */
    fun ownerId(): Long {
        return if (isMultiUserMode && kolibreeConnector.ownerProfile != null) {
            kolibreeConnector.ownerProfile!!.id
        } else userId
    }

    fun containsOrphanBrushing(): Boolean {
        return isMultiUserMode
    }

    internal class Builder @Inject constructor(
        private val kolibreeConnector: IKolibreeConnector,
        private val processedBrushingFactory: LegacyProcessedBrushingFactory,
        private val checkupCalculator: CheckupCalculator
    ) {
        private lateinit var offlineBrushing: OfflineBrushing
        private var isMultiUserMode: Boolean = false
        private var userId: Long = 0
        private var toothbrushSerial: String = ""
        private var toothbrushMac: String = ""

        fun offlineBrushing(offlineBrushing: OfflineBrushing) =
            apply { this.offlineBrushing = offlineBrushing }

        fun isMultiUserMode(isMultiUserMode: Boolean) =
            apply { this.isMultiUserMode = isMultiUserMode }

        fun userId(userId: Long) = apply { this.userId = userId }
        fun toothbrushSerial(toothbrushSerial: String) =
            apply { this.toothbrushSerial = toothbrushSerial }

        fun toothbrushMac(toothbrushMac: String) = apply { this.toothbrushMac = toothbrushMac }

        fun build(): OfflineBrushingsDataMapper {
            if (isMultiUserMode) userId(1)

            return OfflineBrushingsDataMapper(
                kolibreeConnector,
                processedBrushingFactory,
                checkupCalculator,
                offlineBrushing,
                isMultiUserMode,
                userId,
                toothbrushSerial,
                toothbrushMac
            )
        }
    }

    companion object {
        private val defaultBrushingTime = DEFAULT_GOAL_DURATION.seconds.toInt()
        private const val MAX_QUALITY = 100F
        private const val MAX_PERCENT = 100
        private const val EMPTY_DATA_QUALITY = 50
        private const val RECTIFYING_RATIO = 85
    }
}
