/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.scheduler

import com.kolibree.android.commons.interfaces.UserLogoutHook
import io.reactivex.Completable
import javax.inject.Inject
import timber.log.Timber

internal class CancelReminderLogout @Inject constructor(
    private val brushingReminderScheduler: BrushingReminderScheduler
) : UserLogoutHook {
    override fun getLogoutHookCompletable(): Completable {
        return Completable.fromAction {
            brushingReminderScheduler.cancelReminder()
        }.doOnComplete {
            Timber.i("Brushing reminder notifications has been canceled")
        }
    }
}
