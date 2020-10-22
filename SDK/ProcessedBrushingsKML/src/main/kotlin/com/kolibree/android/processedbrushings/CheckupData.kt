package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.extensions.zeroIfNan
import com.kolibree.android.processedbrushings.CheckupData.Companion.NO_AVERAGE_SURFACE
import com.kolibree.android.processedbrushings.CheckupData.Companion.NO_ZONE_SURFACE
import com.kolibree.android.processedbrushings.kml.KMLCheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqueAggregate
import com.kolibree.kml.PlaqueStatus
import com.kolibree.kml.SpeedKPI
import kotlin.math.ceil
import kotlin.math.max
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

/**
 * Wrapper around a KMLCheckupData that provides helper methods so that old clients don't have to
 * change too much
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */
@Keep
@Suppress("TooManyFunctions")
interface CheckupData {

    @Keep
    companion object {
        const val NO_AVERAGE_SURFACE = -1
        const val NO_ZONE_SURFACE = NO_AVERAGE_SURFACE.toFloat()
    }

    val surfacePercentage: Int
    val dateTime: OffsetDateTime
    val duration: Duration
    val checkupDataMap: Map<MouthZone16, ZoneCheckupData>
    val zoneSurfaceMap: Map<MouthZone16, Float>
    fun zoneSurface(mouthZone16: MouthZone16): Float
    fun speedCorrectness(mouthZone16: MouthZone16): SpeedKPI?
    fun correctMovementAverage(): Double
    fun underSpeedAverage(): Double
    fun overpressureAverage(): Double
    fun correctSpeedAverage(): Double
    fun overSpeedAverage(): Double
    fun correctOrientationAverage(): Double

    @Deprecated(
        message = "Use correctSpeedAverage",
        replaceWith = ReplaceWith("correctSpeedAverage")
    )
    fun speedAverage(): Double = correctSpeedAverage()

    @Deprecated(
        message = "Use correctOrientationAverage",
        replaceWith = ReplaceWith("correctOrientationAverage")
    )
    fun angleAverage(): Double = correctOrientationAverage()

    @Deprecated(
        message = "Use correctMovementAverage",
        replaceWith = ReplaceWith("correctMovementAverage")
    )
    fun movementAverage(): Double = correctMovementAverage()

    val isManual: Boolean
        get() = zoneSurfaceMap.isEmpty() && checkupDataMap.isEmpty()

    val coverage: Float?
        get() = if (!isManual) {
            max(0, surfacePercentage) / 100f
        } else null

    val plaqlessCheckupData: PlaqlessCheckupData?
}

@Keep
interface PlaqlessCheckupData {
    val cleanPercent: Int?
    val missedPercent: Int?
    val plaqueLeftPercent: Int?
    val plaqueAggregate: Map<MouthZone16, PlaqueAggregate>?

    fun plaqueStatus(zone16: MouthZone16): PlaqueStatus? =
        plaqueAggregate?.get(zone16)?.plaqueStatus
}

@Keep
internal data class CheckupDataImpl internal constructor(private val kmlCheckupData: KMLCheckupData) :
    CheckupData {
    override val plaqlessCheckupData = plaqlessCheckupData()

    private fun plaqlessCheckupData(): PlaqlessCheckupData? {
        return kmlCheckupData.plaqlessCheckup()?.run {
            object : PlaqlessCheckupData {
                override val cleanPercent: Int? = plaquePercentage?.cleanPercent?.value()

                override val missedPercent: Int? = plaquePercentage?.missedPercent?.value()

                override val plaqueLeftPercent: Int? = plaquePercentage?.plaqueLeftPercent?.value()

                override val plaqueAggregate: Map<MouthZone16, PlaqueAggregate>? =
                    plaqueAggregateByZoneVector?.associate { it.first to it.second }
            }
        }
    }

    override val surfacePercentage: Int by lazy {
        if (kmlCheckupData.checkupData().isEmpty()) {
            NO_AVERAGE_SURFACE
        } else {
            val surfaceSum =
                kmlCheckupData.checkupData().values.map { it.surfacePercentage() }.sum()

            // to match iOS implementation output values. Not all checkups match, but most do
            ceil(surfaceSum / MouthZone16.values().size).toInt()
        }
    }

    override val dateTime: OffsetDateTime =
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(kmlCheckupData.timestamp()), TrustedClock.systemZone)

    /**
     * Duration of the brushing session
     */
    override val duration: Duration = Duration.ofMillis(kmlCheckupData.durationMs())

    override val checkupDataMap: Map<MouthZone16, ZoneCheckupData> by lazy {
        val zoneCheckupMap: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()

        zoneCheckupMap.putAll(kmlCheckupData.checkupData())

        zoneCheckupMap.toMap()
    }

    override val zoneSurfaceMap: Map<MouthZone16, Float> by lazy {
        checkupDataMap.mapValues { it.value.surfacePercentage() }
    }

    override fun zoneSurface(mouthZone16: MouthZone16): Float =
        zoneSurfaceMap.getOrElse(mouthZone16) { NO_ZONE_SURFACE }

    override fun speedCorrectness(mouthZone16: MouthZone16): SpeedKPI? =
        checkupDataMap[mouthZone16]?.zoneKpis()?.speedCorrectness

    @VisibleForTesting
    fun checkupZoneKpis() = checkupDataMap.values.mapNotNull { it.checkupZoneKpis() }

    override fun correctMovementAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::correctMovementPercentage).averageZeroIfNaN()

    override fun overpressureAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::overpressurePercentage).averageZeroIfNaN()

    override fun underSpeedAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::underSpeedPercentage).averageZeroIfNaN()

    override fun correctSpeedAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::correctSpeedPercentage).averageZeroIfNaN()

    override fun overSpeedAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::overSpeedPercentage).averageZeroIfNaN()

    override fun correctOrientationAverage(): Double =
        checkupZoneKpis().map(CheckupZoneKpis::correctOrientationPercentage).averageZeroIfNaN()
}

private fun Collection<Int>.averageZeroIfNaN(): Double = average().zeroIfNan()
