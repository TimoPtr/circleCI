/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import com.kolibree.android.annotation.VisibleForApp
import javax.inject.Inject

@VisibleForApp
interface SystemNotificationsEnabledUseCase {
    fun areNotificationsEnabled(): Boolean
}

internal class SystemNotificationsEnabledUseCaseImpl @Inject constructor(
    private val notificationManagerCompat: NotificationManagerCompat
) : SystemNotificationsEnabledUseCase {
    override fun areNotificationsEnabled() = notificationManagerCompat.areNotificationsEnabled()
}
