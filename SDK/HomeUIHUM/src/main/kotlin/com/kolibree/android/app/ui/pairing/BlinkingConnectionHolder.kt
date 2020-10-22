/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Completable
import javax.inject.Inject

internal interface BlinkingConnectionHolder {
    var blinkingConnection: KLTBConnection?

    fun unpairBlinkingConnectionCompletable(): Completable
}

internal class BlinkingConnectionHolderImpl @Inject constructor(
    private val pairingAssistant: PairingAssistant
) : BlinkingConnectionHolder {

    override var blinkingConnection: KLTBConnection? = null

    override fun unpairBlinkingConnectionCompletable(): Completable =
        blinkingConnection?.let { connection ->
            pairingAssistant.unpair(connection.toothbrush().mac)
                .doFinally { blinkingConnection = null }
        } ?: Completable.complete()
}
