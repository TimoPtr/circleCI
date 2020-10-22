/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.migration

import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Completable

/**
 * Class allowing the migration of the App when it has been updated.
 *
 * To add a new migration feature, please add the created subclass at the end
 * of the Set in [AppMigrationModule.providesOrderedMigrationSet]
 */
@VisibleForApp
interface Migration {

    /**
     * @returns the [Completable] which will execute the migration.
     * Injected objects should be used here.
     */
    fun getMigrationCompletable(): Completable
}

typealias Migrations = List<@JvmSuppressWildcards Migration>
