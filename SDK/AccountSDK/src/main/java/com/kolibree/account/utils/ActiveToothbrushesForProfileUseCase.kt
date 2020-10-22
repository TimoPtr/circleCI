/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.utils

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import javax.inject.Inject
import timber.log.Timber

/**
 * Returns active [KLTBConnectionState.ACTIVE] toothbrushes for current profile
 */
@VisibleForApp
class ActiveToothbrushesForProfileUseCase
@Inject constructor(
    private val toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase
) {

    @Suppress("TooGenericExceptionCaught")
    fun activeToothbrushes(): List<KLTBConnection> {
        val toothbrushes = try {
            toothbrushesForProfileUseCase
                .currentProfileToothbrushesOnceAndStream()
                .take(1)
                .blockingSingle()
        } catch (e: Exception) {
            Timber.e(e)
            emptyList<KLTBConnection>()
        }
        return toothbrushes.filter { it.state().current == KLTBConnectionState.ACTIVE }
    }
}
