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
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Facade shared amongst all Pairing Flow ViewModels
 */
internal interface PairingFlowSharedFacade : PairingSharedViewModel, PairingFlowHost {

    fun setBlinkingConnection(connection: KLTBConnection)

    fun blinkingConnection(): KLTBConnection?

    fun unpairBlinkingConnectionCompletable(): Completable
}

/**
 * @param pairingSharedViewModel is retained on configuration changes
 * @param pairingFlowHost communicates with the Host of the Pairing Flow
 */
internal class PairingFlowSharedFacadeImpl @Inject constructor(
    private val pairingSharedViewModel: PairingViewModel,
    pairingFlowHost: PairingFlowHost
) : PairingFlowSharedFacade,
    PairingSharedViewModel by pairingSharedViewModel,
    PairingFlowHost by pairingFlowHost {

    override fun onPairingFlowSuccess() {
        pairingSharedViewModel.blinkingConnection = null
    }

    override fun setBlinkingConnection(connection: KLTBConnection) {
        pairingSharedViewModel.blinkingConnection = connection
    }

    override fun blinkingConnection(): KLTBConnection? = pairingSharedViewModel.blinkingConnection

    override fun unpairBlinkingConnectionCompletable(): Completable =
        pairingSharedViewModel.unpairBlinkingConnectionCompletable()
}

internal fun PairingFlowSharedFacade.finishPairingFlow(pairingNavigator: PairingNavigator) {
    onPairingFlowSuccess()

    pairingNavigator.finishFlow()
}
