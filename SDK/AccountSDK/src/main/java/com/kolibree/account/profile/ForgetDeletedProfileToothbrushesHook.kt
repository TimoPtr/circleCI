/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import com.kolibree.account.utils.ToothbrushForgetter
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Forgets all toothbrushes owned by the deleted profile
 */
internal class ForgetDeletedProfileToothbrushesHook
@Inject constructor(private val toothbrushForgetter: ToothbrushForgetter) : ProfileDeletedHook {
    override fun onProfileDeleted(profileId: Long): Completable {
        return toothbrushForgetter.forgetOwnedByProfile(profileId)
    }
}
