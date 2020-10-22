package com.kolibree.pairing.session

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.Brushing
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.sdk.connection.parameters.Parameters
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.user.User
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.wrapper.ToothbrushFacade
import com.kolibree.android.sdk.wrapper.ToothbrushFacadeImpl
import com.kolibree.sdkws.core.GruwareRepository

data class PairingSessionImpl(
    private val connection: KLTBConnection,
    private val accountToothbrushRepository: AccountToothbrushRepository,
    private val gruwareRepository: GruwareRepository,
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase
) : PairingSession {

    private val toothbrush: ToothbrushFacade by lazy {
        val tb: Toothbrush = connection.toothbrush()
        val user: User = connection.userMode()
        val parameters: Parameters = connection.parameters()
        val brushing: Brushing = connection.brushing()
        ToothbrushFacadeImpl(connection, tb, user, parameters, brushing, gruwareRepository,
            synchronizeBrushingModeUseCase)
    }

    override fun toothbrush() = toothbrush

    override fun cancel() {
        connection.disconnect()
    }

    override fun connection() = connection
}
