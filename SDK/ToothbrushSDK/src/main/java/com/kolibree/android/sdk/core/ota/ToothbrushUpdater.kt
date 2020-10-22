package com.kolibree.android.sdk.core.ota

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import io.reactivex.Observable

/** Toothbrush firmware and GRU data OTA update manager */
@VisibleForApp
interface ToothbrushUpdater {

    /**
     * Update the toothbrush with given binary data
     *
     * @param availableUpdate [AvailableUpdate]
     * @return [OtaUpdateEvent] [Observable]
     */
    fun update(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent>

    /**
     * Provides information if update is in progress
     *
     * Not supported by all [ToothbrushUpdater]s
     */
    fun isUpdateInProgress(): Boolean = false
}
