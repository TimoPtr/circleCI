/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.ui.brushhead.repo.BrushHeadReplacedDateWriter
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadWorkerDateConfigurator
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadWorkerDateConfigurator.Payload
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.pairing.assistant.PairingAssistant
import com.kolibree.pairing.session.PairingSession
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * UseCase where user confirms that he wants to connect to the toothbrush.
 *
 * It's important to note that association can be revoked if [NextNavigationActionUseCase] returns
 * [MODEL_MISMATCH]. Related bug: https://kolibree.atlassian.net/browse/KLTB002-13226
 */
internal class ConfirmConnectionUseCase @Inject constructor(
    private val pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val updateToothbrushUseCase: UpdateToothbrushUseCase,
    private val pairingAssistant: PairingAssistant,
    private val brushHeadReplacedDateWriter: BrushHeadReplacedDateWriter,
    private val brushHeadWorkerDateConfigurator: SyncBrushHeadWorkerDateConfigurator
) {
    fun confirm(failOnMissingConnection: Boolean): Completable {
        return Completable.defer {
            pairingFlowSharedFacade.blinkingConnection()?.let { connection ->
                pairingAssistant.pair(
                    connection.toothbrush().mac,
                    connection.toothbrush().model,
                    connection.toothbrush().getName()
                )
                    .launchSyncBrushHeadWorker()
                    .ignoreElement()
                    .andThen(maybeUpdateToothbrushRemotely(connection))
            } ?: onNoConnectionToConfirmCompletable(failOnMissingConnection)
        }
    }

    private fun Single<PairingSession>.launchSyncBrushHeadWorker() =
        flatMap { pairingSession ->
            val mac = pairingSession.toothbrush().getMac()
            val serial = pairingSession.toothbrush().getSerialNumber()

            brushHeadReplacedDateWriter.writeReplacedDateNow(mac)
                .doOnSuccess { brushHeadWorkerDateConfigurator.configure(Payload(mac, serial)) }
        }

    private fun maybeUpdateToothbrushRemotely(connection: KLTBConnection): Completable {
        return Completable.defer {
            if (connection.toothbrush().isRunningBootloader) {
                Completable.complete()
            } else {
                updateToothbrushUseCase.updateToothbrush(connection)
                    .ignoreElement()
                    .onErrorComplete() // Don't make the pairing fail for a missing serial number
            }
        }
    }

    private fun onNoConnectionToConfirmCompletable(failOnMissingConnection: Boolean): Completable =
        Completable.fromAction {
            if (failOnMissingConnection) FailEarly.fail("No connection to confirm. Something went wrong")
        }
}
