/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
@file:JvmName("ProfileExtension")
package com.kolibree.account.profile

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.sdkws.core.LocalAvatarCache

@Keep
fun IProfile.getAvatarUrl(context: Context) =
    LocalAvatarCache.getAvatarUrl(context, this)
