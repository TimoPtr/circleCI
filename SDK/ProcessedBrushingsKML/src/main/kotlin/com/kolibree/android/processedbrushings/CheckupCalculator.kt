package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.processedbrushings.kml.KMLCheckupData
import com.kolibree.kml.CheckupCalculatorConfig
import com.kolibree.kml.CheckupCapabilities
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqlessCheckup
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.get
import com.kolibree.kml.toMap
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.ZoneOffset
import timber.log.Timber

@Keep
interface CheckupCalculator {
    // This should be removed when we get rid of LegacyProcessedBrushing
    fun calculateCheckup(brushing: IBrushing): CheckupData

    fun calculateCheckup(legacyProcessedBrushing: LegacyProcessedBrushing): CheckupData

    // This will be called when whe receive a Brushing from backend or legacy (checkup without KML enabled)
    fun calculateCheckup(
        processedData: String?,
        timestampInSeconds: Long,
        duration: Duration
    ): CheckupData

    // Call directly KML
    fun calculateCheckup(processedBrushing: ProcessedBrushing): CheckupData
}

// This class is open so it can be spied in the EspressoProcessedBrushingsModule class
internal open class CheckupCalculatorImpl
@Inject constructor(featureToggles: FeatureToggleSet) :
    CheckupCalculator {

    private val checkupGoalConfiguration =
        featureToggles.toggleForFeature(CheckupGoalDurationConfigurationFeature)
    private val kmlCheckupCalculator =
        com.kolibree.kml.CheckupCalculator(CheckupCalculatorConfig().apply {
            val goal = checkupGoalConfiguration.value
            Timber.v("Goal duration per zone= $goal")
            setGoalDurationPerZone16(goal)
        })

    override fun calculateCheckup(brushing: IBrushing): CheckupData =
        calculateCheckup(
            brushing.processedData,
            brushing.dateTime.toEpochSecond(),
            brushing.durationObject
        )

    override fun calculateCheckup(legacyProcessedBrushing: LegacyProcessedBrushing): CheckupData =
        calculateCheckup(
            legacyProcessedBrushing.processedData,
            legacyProcessedBrushing.datetime.toEpochSecond(
                ZoneOffset.UTC
            ),
            legacyProcessedBrushing.duration
        )

    override fun calculateCheckup(
        processedData: String?,
        timestampInSeconds: Long,
        duration: Duration
    ): CheckupData = when {
        !isValidJsonObject(processedData) -> emptyCheckupData(timestampInSeconds, duration)
        else -> calculateCheckupFromJSON(processedData!!, timestampInSeconds, duration)
    }

    private fun calculateCheckupFromJSON(
        processedData: String,
        timestampInSeconds: Long,
        duration: Duration
    ): CheckupData = try {
        val processedBrushing = ProcessedBrushing.fromJSON(
            processedData,
            timestampInSeconds,
            duration.toMillis()
        )

        calculateCheckup(processedBrushing)
    } catch (e: RuntimeException) {
        Timber.e(
            e,
            "Error parsing JSON in KML (timestamp: %s, duration: %s, %s)",
            timestampInSeconds,
            duration,
            processedData
        )
        emptyCheckupData(timestampInSeconds, duration)
    }

    @VisibleForTesting
    fun isValidJsonObject(processedData: String?): Boolean {
        if (processedData.isNullOrEmpty()) {
            return false
        }

        return try {
            JsonParser.parseString(processedData).asJsonObject
            true
        } catch (e: JsonParseException) {
            Timber.e(e)
            false
        } catch (e: IllegalStateException) {
            Timber.e(e)
            false
        }
    }

    private fun emptyCheckupData(timestampInSeconds: Long, duration: Duration) =
        CheckupDataImpl(object : KMLCheckupData {
            override fun plaqlessCheckup() = null

            override fun timestamp() = TimeUnit.SECONDS.toMillis(timestampInSeconds)

            override fun durationMs() = duration.toMillis()

            override fun checkupData(): Map<MouthZone16, ZoneCheckupData> = mapOf()
        })

    /**
     * This'll call KML library
     */
    override fun calculateCheckup(processedBrushing: ProcessedBrushing): CheckupData {
        val checkup = kmlCheckupCalculator.computeCheckup(processedBrushing)

        return CheckupDataImpl(object : KMLCheckupData {
            override fun plaqlessCheckup(): PlaqlessCheckup? {
                if (checkup.capabilities.has(CheckupCapabilities.Flags.PLAQUE_LEVEL)) {
                    return checkup.optionalPlaqlessCheckup.get()
                }

                return null
            }

            override fun timestamp() = TimeUnit.SECONDS.toMillis(checkup.timestampInSeconds)

            override fun durationMs() = checkup.durationInMilliseconds

            override fun checkupData(): Map<MouthZone16, ZoneCheckupData> =
                checkup.checkupByZoneVector.toMap().mapValues { entry ->
                    object : ZoneCheckupData {
                        override fun zoneKpis(): ZoneKpis? = ZoneKpis(entry.value.kpiAggregate)

                        override fun checkupZoneKpis(): CheckupZoneKpis? =
                            CheckupZoneKpis(entry.value.kpiPercentage)

                        override fun surfacePercentage(): Float =
                            entry.value.coverage.value().toFloat()
                    }
                }
        })
    }
}
