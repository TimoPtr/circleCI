/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.usecase

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import java.lang.IllegalStateException
import javax.inject.Inject

internal class RenameToothbrushNameUseCase @Inject constructor(
    private val toothbrushRepository: ToothbrushRepository
) {

    fun rename(connection: KLTBConnection?, name: String): Completable {
        return when {
            toothbrushNameIsTheSame(connection, name) -> Completable.complete()
            connection == null || !connection.isActive() -> toothbrushDisconnectedException()
            else -> renameToothbrushName(connection, name)
        }
    }

    private fun toothbrushNameIsTheSame(connection: KLTBConnection?, name: String): Boolean {
        return connection?.toothbrush()?.getName() == name
    }

    private fun toothbrushDisconnectedException(): Completable =
        Completable.error(ToothbrushDisconnectedException)

    private fun renameToothbrushName(connection: KLTBConnection, name: String): Completable {
        val mac = connection.toothbrush().mac
        return connection
            .toothbrush()
            .setAndCacheName(name)
            .andThen(toothbrushRepository.rename(mac, name))
    }
}

internal object ToothbrushDisconnectedException : IllegalStateException()
