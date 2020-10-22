package com.kolibree.android.sdk.core.ota.kltb003

import android.app.Activity
import no.nordicsemi.android.dfu.BuildConfig
import no.nordicsemi.android.dfu.DfuBaseService

/**
 * Created by aurelien on 05/09/17.
 *
 *
 * Nordic DFU service implementation for M1 firmware upgrades
 */
class KLTB003ToothbrushUpdaterService : DfuBaseService() {

    override fun getNotificationTarget(): Class<out Activity>? {
        return null // We won't use the service from an activity so we return null here
    }

    override fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }
}
