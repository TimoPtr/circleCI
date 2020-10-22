package com.kolibree.android.sdk.core.driver

import android.content.Context
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.core.driver.ble.AraDriver
import com.kolibree.android.sdk.core.driver.ble.CB1Driver
import com.kolibree.android.sdk.core.driver.ble.CE1Driver
import com.kolibree.android.sdk.core.driver.ble.CE2Driver
import com.kolibree.android.sdk.core.driver.ble.CM1Driver
import com.kolibree.android.sdk.core.driver.ble.GlintDriver
import com.kolibree.android.sdk.core.driver.ble.PlaqlessDriver
import javax.inject.Inject

internal class KLTBDriverFactory @Inject constructor() {
    fun create(
        context: Context,
        mac: String,
        toothbrushModel: ToothbrushModel,
        kltbDriverListener: KLTBDriverListener
    ): KLTBDriver {
        val appContext = context.applicationContext
        return when (toothbrushModel) {
            ARA -> AraDriver(appContext, mac, kltbDriverListener)
            CONNECT_E1 -> CE1Driver(appContext, mac, kltbDriverListener)
            // https://kolibree.atlassian.net/browse/KLTB002-10822
            CONNECT_E2, HILINK, HUM_ELECTRIC -> CE2Driver(appContext, mac, kltbDriverListener)
            CONNECT_M1 -> CM1Driver(appContext, mac, kltbDriverListener)
            CONNECT_B1, HUM_BATTERY -> CB1Driver(appContext, mac, kltbDriverListener)
            PLAQLESS -> PlaqlessDriver(appContext, mac, kltbDriverListener)
            GLINT -> GlintDriver(appContext, mac, kltbDriverListener)
        }
    }
}
