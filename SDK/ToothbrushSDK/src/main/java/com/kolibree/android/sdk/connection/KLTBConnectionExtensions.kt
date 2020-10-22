/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.utils.callSafely

@Keep
fun KLTBConnection.isPlaqless(): Boolean {
    return toothbrush().model.isPlaqless
}

@Keep
fun KLTBConnection.isActive(): Boolean {
    return state().current == KLTBConnectionState.ACTIVE
}

@Keep
fun KLTBConnection?.mac(): String? {
    return this?.toothbrush()?.mac
}

@Keep
fun KLTBConnection?.callSafelyIfActive(block: KLTBConnection.() -> Unit) {
    if (this != null && isActive()) {
        callSafely { block.invoke(this) }
    }
}

@Keep
fun KLTBConnection.emitsVibrationStateAfterLostConnection(): Boolean {
    val impl = this as? InternalKLTBConnection

    return impl?.let { connection ->
        connection.emitsVibrationStateAfterLostConnection()
    } ?: false
}
