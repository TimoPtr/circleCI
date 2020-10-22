/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.android.synchronizator.Synchronizator
import javax.inject.Inject
import kotlinx.coroutines.delay
import org.threeten.bp.Duration

internal class RewardsRemoteBrushingProcessor
@Inject constructor(private val synchronizator: Synchronizator) : RemoteBrushingsProcessor {
    override suspend fun onBrushingsCreated() = startSynchronization()

    override suspend fun onBrushingsRemoved() = startSynchronization()

    private suspend fun startSynchronization() {
        delay(delayAfterBrushingChanges.toMillis())

        synchronizator.synchronize()
    }
}

private val delayAfterBrushingChanges = Duration.ofSeconds(2)
