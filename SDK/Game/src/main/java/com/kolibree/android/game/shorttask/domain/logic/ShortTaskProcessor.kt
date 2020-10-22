/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.domain.logic

import com.kolibree.android.synchronizator.Synchronizator
import javax.inject.Inject
import kotlinx.coroutines.delay
import org.threeten.bp.Duration

internal class ShortTaskProcessor @Inject constructor(private val synchronizator: Synchronizator) {

    suspend fun onShortTaskCreated() {
        // We need to delay the creation of the short task so the backend will have time to process each one of them
        delay(delayAfterShortTaskCreated.toMillis())

        synchronizator.synchronize()
    }
}

private val delayAfterShortTaskCreated = Duration.ofSeconds(2)
