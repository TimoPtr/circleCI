/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.pairing.session

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.wrapper.ToothbrushFacade

/**
 * Cancelable pairing session
 *
 * Use this object to manage a connection to a toothbrush
 */
@Keep
interface PairingSession {

    /**
     * Cancel a pairing session
     */
    fun cancel()

    /**
     * Returns connected [ToothbrushFacade] object
     */
    fun toothbrush(): ToothbrushFacade

    /**
     * Returns connection [KLTBConnection] object
     */
    fun connection(): KLTBConnection
}
