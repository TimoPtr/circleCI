package com.kolibree.android.sdk.connection.state

import androidx.annotation.Keep

/**
 * Created by aurelien on 11/10/16.
 *
 * Kolibree toothbrush connection state
 */

@Keep
enum class KLTBConnectionState {
    /**
     * The connection object has been created
     */
    NEW,

    /**
     * The connection is currently establishing
     */
    ESTABLISHING,

    /**
     * The connection is active and commands can be sent
     */
    ACTIVE,

    /**
     * The connection is terminating
     */
    TERMINATING,

    /**
     * The connection is now terminated
     */
    TERMINATED,

    /**
     * The toothbrush firmware is currently getting updated
     */
    OTA
}
