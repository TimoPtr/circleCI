/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import androidx.annotation.Keep
import io.reactivex.Completable

/**
 * Hook to be executed after a profile has successfully been deleted
 */
@Keep
interface ProfileDeletedHook {
    fun onProfileDeleted(profileId: Long): Completable
}
