package com.kolibree.android.sdk.connection.brushing

import androidx.annotation.Keep
import com.kolibree.android.processedbrushings.LegacyProcessedBrushing
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.error.FailureReason

/**
 * Created by aurelien on 18/11/16.
 *
 * To be implemented to get notified on brushing data retrieval events
 */

@Keep
interface OfflineBrushingConsumer {

    /**
     * Called when a new brushing data offlineBrushing is available
     *
     * Implementation should make sure the offlineBrushing has been stored in database before returning true
     *
     * This method is NOT called on UI thread
     *
     * Make sure you put the data in a safe place before returning true
     *
     * @param connection [KLTBConnection] data source
     * @param offlineBrushing non null [LegacyProcessedBrushing]
     * @param remaining remaining offlineBrushing count (includes the one passed here)
     * @return true if the offlineBrushing has been consumed (will be removed from toothbrush memory), false
     * otherwise (won't be delete, will stop process)
     */
    fun onNewOfflineBrushing(
        connection: KLTBConnection,
        offlineBrushing: OfflineBrushing,
        remaining: Int
    ): Boolean

    /**
     * Called when process ends (when no record left, or a call to onBrushingRecordRetrieved returned
     * false)
     *
     * This method is called on UI thread
     *
     * @param connection [KLTBConnection] event source
     * @param retrievedCount pulled record count
     */
    fun onSuccess(connection: KLTBConnection, retrievedCount: Int)

    /**
     * Called when an error occurred
     *
     * This method is called on UI thread
     *
     * @param connection [KLTBConnection] failure source
     * @param failureReason if something wrong happened
     */
    fun onFailure(connection: KLTBConnection, failureReason: FailureReason)

    /**
     * Called when offline brushings synchronization process is started
     * This method is called on UI thread
     *
     * @param connection [KLTBConnection] connection
     */
    fun onSyncStart(connection: KLTBConnection)

    /**
     * Called when offline brushings synchronization process is ended
     * This method is called on UI thread
     *
     * @param connection [KLTBConnection] connection
     */
    fun onSyncEnd(connection: KLTBConnection)
}
