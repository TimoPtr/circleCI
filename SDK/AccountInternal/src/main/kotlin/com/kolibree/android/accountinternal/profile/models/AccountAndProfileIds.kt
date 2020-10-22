/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.models

import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.annotation.VisibleForApp

@KolibreeExperimental
@VisibleForApp
data class AccountAndProfileIds(
    val accountId: Long,
    val profileId: Long
)
