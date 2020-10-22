package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kolibree.android.processedbrushings.models.ZonePass
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

/**
 * Represents a brushing pass at a given timestamp and a given duration, and holds the KPIs achieved
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */
@Keep
interface BrushingPass {
    val zoneKpis: ZoneKpis?
    val datetime: LocalDateTime
    val duration: Duration

    /**
     * Returns the Json String used in legacy to store a BrushingPass
     */
    fun toLegacyJsonString(): String
}

/**
 * Represents a BrushingPass coming from legacy offline brushings
 */
internal data class BrushingPassLegacy(
    @SerializedName(ZonePass.START_TIME_JSON_KEY) private val startTimestampMs: Long,
    @SerializedName(ZonePass.DURATION_JSON_KEY) private val durationMs: Long
) : BrushingPass {
    /**
     * LocalDateTime at which the BrushingPass started
     */
    override val datetime: LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestampMs), ZoneOffset.UTC)

    /**
     * Duration of the BrushingPass
     */
    override val duration: Duration = Duration.ofMillis(durationMs)

    /**
     * Legacy brushings don't have ZoneKpis
     */
    override val zoneKpis: ZoneKpis?
        get() = null

    override fun toLegacyJsonString(): String = Gson().toJson(this)
}
