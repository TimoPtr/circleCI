package com.kolibree.android.processedbrushings.kml

import com.kolibree.android.processedbrushings.ZoneCheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqlessCheckup

/**
 * Stub for KML CheckupData
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */
internal interface KMLCheckupData {
    fun timestamp(): Long
    fun durationMs(): Long
    fun checkupData(): Map<MouthZone16, ZoneCheckupData>
    fun plaqlessCheckup(): PlaqlessCheckup?
}
