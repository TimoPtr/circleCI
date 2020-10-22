package com.kolibree.pairing.session

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import io.reactivex.Single

interface PairingSessionCreator {
    fun create(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<PairingSession>

    fun create(
        connection: KLTBConnection
    ): PairingSession

    fun connectAndBlinkBlue(
        mac: String,
        model: ToothbrushModel,
        name: String
    ): Single<KLTBConnection>

    fun blinkBlue(connection: KLTBConnection): Single<KLTBConnection>
}
