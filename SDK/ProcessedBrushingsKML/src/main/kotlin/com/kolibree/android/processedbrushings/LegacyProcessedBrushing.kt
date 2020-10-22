package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.processedbrushings.models.ZoneData
import com.kolibree.android.processedbrushings.models.ZonePass
import com.kolibree.kml.MouthZone16
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

/**
 * Describes an offline brushing session
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */

@Keep
interface LegacyProcessedBrushing {
    val datetime: LocalDateTime
    val duration: Duration
    val processedData: String
    val mouthZonePasses: Map<MouthZone16, List<BrushingPass>>
}

// TODO should be remove when StoredBrushings and FreeBrushings are available from KML

@Keep
class LegacyProcessedBrushingFactory @Inject internal constructor(private val clock: Clock) {

    fun createAtCurrentInstant(
        data: Map<MouthZone16, List<ZonePass>>,
        goalTime: Int
    ): LegacyProcessedBrushing =
        LegacyOfflineBrushingImpl.fromZonePassStartingAtCurrentInstant(
            clock,
            data,
            goalTime
        )
}

/**
 * Describes an offline brushing session coming from legacy code
 */
internal data class LegacyOfflineBrushingImpl(
    private val timestampMs: Long,
    private val durationMs: Long,
    override val mouthZonePasses: Map<MouthZone16, List<BrushingPass>>,
    private val legacyJsonString: String?
) : LegacyProcessedBrushing {
    override val processedData: String = legacyJsonString ?: ""

    /**
     * LocalDateTime at which the brushing session started
     */
    override val datetime: LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneOffset.UTC)

    /**
     * Duration of the brushing session
     */
    override val duration: Duration = Duration.ofMillis(durationMs)

    internal companion object {

        fun fromZonePassStartingAtCurrentInstant(
            clock: Clock,
            data: Map<MouthZone16, List<ZonePass>>,
            goalBrushingTime: Int
        ): LegacyProcessedBrushing {
            val startTimestamp = Instant.now(clock).toEpochMilli()

            val mouthZonePasses = extractMouthZonePasses(data, goalBrushingTime)

            val legacyProcessedData =
                LegacyProcessedDataGenerator.computeProcessedData(
                    data,
                    goalBrushingTime
                )
            val duration =
                LegacyProcessedDataGenerator.getEffectiveBrushingTimeSeconds(
                    data
                )

            return LegacyOfflineBrushingImpl(
                timestampMs = startTimestamp,
                durationMs = TimeUnit.SECONDS.toMillis(duration.toLong()),
                mouthZonePasses = mouthZonePasses,
                legacyJsonString = legacyProcessedData
            )
        }

        @VisibleForTesting
        fun extractMouthZonePasses(
            data: Map<MouthZone16, List<ZonePass>>,
            goalTime: Int
        ): Map<MouthZone16, List<BrushingPass>> {
            val metricsMap =
                LegacyProcessedDataGenerator.computeProcessedDataToMap(
                    data,
                    goalTime
                )

            return metricsMapToMouthPasses(metricsMap)
        }

        /**
         * Returns an immutable MouthZonePasses
         */
        private fun metricsMapToMouthPasses(
            metricsMap: Map<MouthZone16, ZoneData>
        ): Map<MouthZone16, List<BrushingPass>> {
            val mouthZonePasses: MutableMap<MouthZone16, List<BrushingPass>> = mutableMapOf()

            metricsMap.forEach { (zone, zoneData) ->
                val zonePassAsBrushingPass = zoneData.passes.map {
                    BrushingPassLegacy(
                        startTimestampMs = it.startTime,
                        durationMs = toMilliseconds(it.durationTenthSecond)
                    )
                }

                /**
                 * Make sure it's immutable
                 */
                mouthZonePasses[zone] = zonePassAsBrushingPass.toList()
            }

            /**
             * Make sure it's immutable
             */
            return mouthZonePasses.toMap()
        }

        private fun toMilliseconds(tenthSeconds: Long) = tenthSeconds * 100L
    }
}
