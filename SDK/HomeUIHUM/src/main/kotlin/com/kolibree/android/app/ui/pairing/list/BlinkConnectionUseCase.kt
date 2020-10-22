/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.BlinkUseCase
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Observable
import javax.inject.Inject

internal class BlinkConnectionUseCase @Inject constructor(
    private val pairingAssistant: PairingAssistant,
    private val blinkUseCase: BlinkUseCase
) {
    /**
     * Observable that will attempt to blink the [connection]
     *
     * The blink attempt will timeout after [CONNECTION_TIMEOUT]
     *
     * The stream starts with [BlinkEvent.InProgress] and is followed by one of termination events
     * - [BlinkEvent.Success]: toothbrush is blinking and connection has been established. It
     * includes the established connection ([KLTBConnection])
     * - [BlinkEvent.Error]: Error while attempting to blink [connection]
     * - [BlinkEvent.Timeout]: Timeout while attempting to blink [connection]
     *
     * @return [Observable]<[BlinkEvent]>
     */
    fun blink(connection: KLTBConnection): Observable<BlinkEvent> {
        return blinkUseCase.blink(
            pairingAssistant.blinkBlue(connection),
            connection.toothbrush().mac
        )
    }
}
