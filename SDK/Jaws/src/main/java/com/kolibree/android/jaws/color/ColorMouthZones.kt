package com.kolibree.android.jaws.color

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import com.kolibree.kml.MouthZone16
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ColorMouthZones(
    internal val zonesColor: Map<MouthZone16, Int>,
    @ColorInt private val defaultColor: Int = Color.WHITE
) : Parcelable {

    @ColorInt
    fun color(zone: MouthZone16) = zonesColor[zone] ?: defaultColor

    @Keep
    companion object {
        fun oneColor(@ColorInt color: Int) =
            ColorMouthZones(
                MouthZone16
                    .values()
                    .associate { it to color }, color
            )

        fun white() = oneColor(Color.WHITE)

        @JvmStatic
        fun gray() = oneColor(Color.LTGRAY)
    }
}
