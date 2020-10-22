package com.kolibree.android.coachplus.controller

import androidx.annotation.VisibleForTesting
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.DI_GOAL_BRUSHING_TIME
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeRepository
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject
import javax.inject.Named
import org.threeten.bp.Duration

internal interface ZoneDurationAdjuster {
    fun getAdjustedDuration(zone: MouthZone16): Long
}

/**
 * ZoneDurationAdjuster that will return the original zone duration untouched
 */
internal class NoOpZoneDurationAdjuster
@Inject constructor(
    @Named(DI_GOAL_BRUSHING_TIME) goalBrushingDuration: Duration
) : ZoneDurationAdjuster {
    private val originalDurationPerZone =
        goalBrushingDuration.toMillis() / MouthZone16.values().size

    override fun getAdjustedDuration(zone: MouthZone16): Long = originalDurationPerZone
}

/**
 * ZoneDurationAdjuster that will change the default duration of specific zone base on different
 * brushingMode
 */
internal class BrushingModeZoneDurationAdjuster @Inject constructor(
    @Named(DI_GOAL_BRUSHING_TIME)
    goalBrushingDuration: Duration,
    private val connector: IKolibreeConnector,
    private val brushingModeRepository: BrushingModeRepository
) : ZoneDurationAdjuster {
    private val originalDurationPerZone =
        goalBrushingDuration.toMillis() / MouthZone16.values().size
    private val adjustedDuration = originalDurationPerZone.times(FACTOR).toLong()

    @VisibleForTesting
    val regularDurations: Map<MouthZone16, Long> = mapOf(
        MouthZone16.UpMolLeOcc to adjustedDuration, MouthZone16.LoMolLeOcc to adjustedDuration,
        MouthZone16.UpMolRiOcc to adjustedDuration, MouthZone16.LoMolRiOcc to adjustedDuration
    )
    @VisibleForTesting
    val strongDurations: Map<MouthZone16, Long> = mapOf(
        MouthZone16.UpIncExt to adjustedDuration, MouthZone16.LoIncExt to adjustedDuration
    )

    /*
    Slow -> Sensitive Program
    Regular -> Everyday Care
    Strong -> Whitening Program
    Reference: https://kolibree.atlassian.net/wiki/spaces/PROD/pages/31686662/Brushing+program+vibration+speed#Impact-of-Brushing-Program-on-brushing-duration
     */
    private var brushingMode: BrushingMode = BrushingMode.Slow

    init {
        initBrushingMode()
    }

    private fun initBrushingMode() {
        connector.currentProfile?.let {
            brushingMode = brushingModeRepository.getDefaultModeIfNull(it.id).brushingMode
        }
    }

    /**
     * To get adjusted duration by brushingMode and MouthZone
     */
    override fun getAdjustedDuration(zone: MouthZone16): Long {
        return when (brushingMode) {
            BrushingMode.Slow -> originalDurationPerZone
            BrushingMode.Regular -> regularDurations[zone] ?: originalDurationPerZone
            BrushingMode.Strong -> strongDurations[zone] ?: originalDurationPerZone
            BrushingMode.Polishing -> originalDurationPerZone
            BrushingMode.UserDefined -> originalDurationPerZone
        }
    }

    companion object {
        /*
        Chewing surfaces (4 zones) → d’target = dtarget × α, α = 1.5.
        Front teeth outer surfaces (2 zones) → d’target = dtarget × β, β = 1.5
        */
        internal const val FACTOR = 1.5
    }
}
