package com.kolibree.charts.models

import androidx.annotation.Keep
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 18/05/2018.
 */

@Keep
data class DayStat(val dateTime: OffsetDateTime, val data: List<Stat>) {

    private val morning: Boolean
    private val noon: Boolean
    private val evening: Boolean

    fun isMorning() = morning
    fun isEvening() = evening
    fun isNoon() = noon
    fun isEmpty() = data.isEmpty()
    fun count() = data.size

    init {
        val sortedData = data.toMutableList()
        sortedData.sort()

        val numberOfBrushings = sortedData.size
        @Suppress("MagicNumber")
        when (numberOfBrushings) {
            0 -> {
                morning = false
                noon = false
                evening = false
            }

            1 -> {
                when {
                    isBefore4pm(sortedData[0]) -> {
                        morning = true
                        evening = false
                    }
                    else -> {
                        evening = true
                        morning = false
                    }
                }
                noon = false
            }

            2 -> {
                val firstBefore4pm = isBefore4pm(sortedData[0])
                val secondBefore4pm = isBefore4pm(sortedData[1])

                when {
                    firstBefore4pm && secondBefore4pm -> {
                        morning = true
                        noon = true
                        evening = false
                    }
                    firstBefore4pm -> {
                        morning = true
                        noon = false
                        evening = true
                    }
                    else -> {
                        morning = false
                        noon = true
                        evening = true
                    }
                }
            }
            else -> {
                morning = true
                noon = true
                evening = !isBefore4pm(sortedData[2])
            }
        }
    }

    @Suppress("MagicNumber")
    private fun isBefore4pm(stat: Stat): Boolean {
        val brushingDateTime = OffsetDateTime
            .ofInstant(stat.date.toInstant(), dateTime.offset)

        return brushingDateTime.isBefore(dateTime.truncatedTo(ChronoUnit.DAYS).withHour(16))
    }
}
