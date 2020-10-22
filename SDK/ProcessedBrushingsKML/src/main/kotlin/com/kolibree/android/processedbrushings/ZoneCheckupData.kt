package com.kolibree.android.processedbrushings

import androidx.annotation.Keep

/**
 * CheckupData associated to each MouthZone
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 *
 * @property surfacePercentage of zone coverage. 100% means you brush all zones in your mouth.
 * @property zoneKpis it's the global validation of KPIs statistics
 * @property checkupZoneKpis it's KPIs statistics in %
 */
@Keep
interface ZoneCheckupData {
    fun surfacePercentage(): Float // equivalent to old surface
    fun zoneKpis(): ZoneKpis? = null
    fun checkupZoneKpis(): CheckupZoneKpis? = null
}
