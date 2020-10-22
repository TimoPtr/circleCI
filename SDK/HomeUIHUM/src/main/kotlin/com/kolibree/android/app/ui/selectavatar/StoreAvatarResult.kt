/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

internal sealed class StoreAvatarResult {
    data class Success(val avatarPath: String) : StoreAvatarResult()
    data class Error(val exception: StoreAvatarException) : StoreAvatarResult()
}
