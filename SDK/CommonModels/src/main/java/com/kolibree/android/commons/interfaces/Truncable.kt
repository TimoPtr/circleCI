/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.interfaces

import androidx.annotation.Keep
import io.reactivex.Completable

/**
 * Classes can implement this interface to expose a capability to truncate persisted storage.
 * Operate as [UserLogoutHook] but for persistence related operations.
 *
 *  @see [UserLogoutHook]
 */
@Keep
interface Truncable {
    fun truncate(): Completable
}
