/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.interfaces

import io.reactivex.Completable

/**
 * Classes can implements this interface to execute any logic needed when the user disconnects from
 * the application. Operate as [Truncable] but for a more generic purpose.
 * The operation must not be time consuming.
 *
 * @see [Truncable]
 */
interface UserLogoutHook {

    /**
     *  @return a [Completable] which will be run when the user logout from the application.
     */
    fun getLogoutHookCompletable(): Completable
}
