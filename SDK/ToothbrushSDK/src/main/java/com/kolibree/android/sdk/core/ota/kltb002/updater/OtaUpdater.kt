package com.kolibree.android.sdk.core.ota.kltb002.updater

import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate
import io.reactivex.Observable

internal interface OtaUpdater {

    /**
     * Performs an OTA update.
     *
     * @param otaUpdate an instance containing the information needed to perform the update
     * @return an Observable that emits the progress in percent value
     */
    fun update(otaUpdate: OtaUpdate): Observable<OtaUpdateEvent>
}
