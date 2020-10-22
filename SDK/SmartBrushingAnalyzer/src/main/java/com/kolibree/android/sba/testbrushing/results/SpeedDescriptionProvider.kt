package com.kolibree.android.sba.testbrushing.results

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.mouthmap.logic.SpeedResult
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.InsideIncisives
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.InsideMolars
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.Occlusal
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.OutsideIncisives
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.OutsideMolars
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.BottomLeft
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.BottomRight
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.TopLeft
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.TopRight
import com.kolibree.kml.MouthZone16
import java.text.NumberFormat

internal class SpeedDescriptionProvider(private val context: Context) :
    DescriptionProvider<SpeedResult> {

    /**

    A) Per zone

    Zone groups = {Occlusal, Outside molars, Inside molars, Outside incisives, Inside incisives}
    For each group, compute X = % of time with over speed.
    Take the group with max X.
    If X> a% (50%), display text:
    Text = "Your movements were too fast when you brushed your <group name>. Slow down to keep healthy teeth."
    otherwise

    B) Per quadrant

    Zone groups = {Top Left, Top Right, Bottom Left, Bottom Right}
    Incisive prorata to be split equally between left and right
    For each group, compute X = % of time with over speed.
    Take the group with max X.
    If X> b% (25%), display text:
    Text = "Your movements were too fast when you brushed your <group name>. Slow down to keep healthy teeth."
    otherwise

    C) Simple solution

    Compute X = % of time with over speed.
    If X > c% (5%) to be defined
    Text = "Your movements were too fast during X% of your brushing. Slow down to keep healthy teeth."
    otherwise

    D) All good

     */
    override fun description(result: SpeedResult): String {
        val maxGroupOverspeed = maxGroupOverspeed(result.overSpeedPercentageMouthZones)
        if (maxGroupOverspeed.percentage > GROUPS_THRESHOLD) {
            val groupName = context.getString(maxGroupOverspeed.group.nameRes)
            return context.getString(R.string.speed_description_by_groups, groupName)
        }

        val maxQuadrantOverspeed = maxQuadrantOverspeed(result.overSpeedPercentageMouthZones)
        if (maxQuadrantOverspeed.percentage > QUADRANTS_THRESHOLD) {
            val groupName = context.getString(maxQuadrantOverspeed.group.nameRes)
            return context.getString(R.string.speed_description_by_groups, groupName)
        }

        val averageOverspeed = averageOverspeed(result.overSpeedPercentageMouthZones)
        if (averageOverspeed > AVERAGE_THRESHOLD) {
            val textualPercentOverspeed = textualPercentage(averageOverspeed)
            return context.getString(R.string.speed_description_average, textualPercentOverspeed)
        }

        return context.getString(R.string.speed_description_all_good)
    }

    @VisibleForTesting
    fun textualPercentage(speed: Int): String {
        val numberFormat = NumberFormat.getPercentInstance()
        return numberFormat.format((speed / 100f).toDouble())
    }

    @VisibleForTesting
    fun maxGroupOverspeed(speedZones: Map<MouthZone16, Int>): GroupOverspeed {
        val occlusal = GroupOverspeed(Occlusal, percentageForZones(speedZones, Occlusal.zones))
        val outsideMolars =
            GroupOverspeed(OutsideMolars, percentageForZones(speedZones, OutsideMolars.zones))
        val insideMolars =
            GroupOverspeed(InsideMolars, percentageForZones(speedZones, InsideMolars.zones))
        val outsideIncisives =
            GroupOverspeed(OutsideIncisives, percentageForZones(speedZones, OutsideIncisives.zones))
        val insideIncisives =
            GroupOverspeed(InsideIncisives, percentageForZones(speedZones, InsideIncisives.zones))

        val groups =
            listOf(occlusal, outsideMolars, insideMolars, outsideIncisives, insideIncisives)
        return groups.sortedByDescending { it.percentage }.first()
    }

    @VisibleForTesting
    fun percentageForZones(speedZones: Map<MouthZone16, Int>, zones: List<MouthZone16>): Int {
        return zones.map { speedZones[it] ?: 0 }.sum() / zones.size
    }

    @VisibleForTesting
    fun maxQuadrantOverspeed(speedZones: Map<MouthZone16, Int>): QuadrantOverspeed {
        val topLeft = QuadrantOverspeed(TopLeft, percentageForZones(speedZones, TopLeft.zones))
        val topRight = QuadrantOverspeed(TopRight, percentageForZones(speedZones, TopRight.zones))
        val bottomLeft =
            QuadrantOverspeed(BottomLeft, percentageForZones(speedZones, BottomLeft.zones))
        val bottomRight =
            QuadrantOverspeed(BottomRight, percentageForZones(speedZones, BottomRight.zones))

        val groups = listOf(topLeft, topRight, bottomLeft, bottomRight)
        return groups.sortedByDescending { it.percentage }.first()
    }

    @VisibleForTesting
    fun averageOverspeed(speedZones: Map<MouthZone16, Int>) =
        speedZones.values.sum() / MouthZone16.values().size

    /**
     * GROUP ZONES
     */

    internal enum class GroupZones(val nameRes: Int, val zones: List<MouthZone16>) {
        Occlusal(
            R.string.speed_group_occlusal, listOf(
                MouthZone16.LoMolLeOcc,
                MouthZone16.LoMolRiOcc,
                MouthZone16.UpMolRiOcc,
                MouthZone16.UpMolLeOcc
            )
        ),

        OutsideMolars(
            R.string.speed_group_outside_molars, listOf(
                MouthZone16.LoMolRiExt,
                MouthZone16.LoMolLeExt,
                MouthZone16.UpMolRiExt,
                MouthZone16.UpMolLeExt
            )
        ),

        InsideMolars(
            R.string.speed_group_inside_molars, listOf(
                MouthZone16.LoMolRiInt,
                MouthZone16.LoMolLeInt,
                MouthZone16.UpMolRiInt,
                MouthZone16.UpMolLeInt
            )
        ),

        OutsideIncisives(
            R.string.speed_group_outside_incisives, listOf(
                MouthZone16.LoIncExt,
                MouthZone16.UpIncExt
            )
        ),

        InsideIncisives(
            R.string.speed_group_inside_incisives, listOf(
                MouthZone16.LoIncInt,
                MouthZone16.UpIncInt
            )
        );
    }

    internal data class GroupOverspeed(val group: GroupZones, val percentage: Int)

    /**
     * QUADRANT ZONES
     */

    internal enum class QuadrantZones(val nameRes: Int, val zones: List<MouthZone16>) {
        TopLeft(
            R.string.speed_quadrant_top_left, listOf(
                MouthZone16.UpMolLeOcc,
                MouthZone16.UpMolLeExt,
                MouthZone16.UpMolLeInt
            )
        ),

        TopRight(
            R.string.speed_quadrant_top_right, listOf(
                MouthZone16.UpMolRiExt,
                MouthZone16.UpMolRiOcc,
                MouthZone16.UpMolRiInt
            )
        ),

        BottomLeft(
            R.string.speed_quadrant_bottom_left, listOf(
                MouthZone16.LoMolLeExt,
                MouthZone16.LoMolLeOcc,
                MouthZone16.LoMolLeInt
            )
        ),

        BottomRight(
            R.string.speed_quadrant_bottom_right, listOf(
                MouthZone16.LoMolRiExt,
                MouthZone16.LoMolRiOcc,
                MouthZone16.LoMolRiInt
            )
        ),
    }

    internal data class QuadrantOverspeed(val group: QuadrantZones, val percentage: Int)

    companion object {
        const val GROUPS_THRESHOLD = 50
        const val QUADRANTS_THRESHOLD = 25
        const val AVERAGE_THRESHOLD = 5
    }
}
