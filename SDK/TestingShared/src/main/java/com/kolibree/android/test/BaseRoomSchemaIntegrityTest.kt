/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.annotation.VisibleForApp
import kotlin.reflect.KClass

@VisibleForApp
abstract class BaseRoomSchemaIntegrityTest<DB : RoomDatabase>(
    private val databaseClass: KClass<DB>,
    private val databaseName: String,
    private val schemaVersion: Int,
    private vararg val migrationsFrom1ToSchemaVersion: Migration
) : BaseRoomMigrationTest<DB>(databaseClass, databaseName) {

    /*
    When you're adding new integrity test, please:
    - make sure room schema path is added to `androidTest` flavour
    - make sure your module has `androidTestImplementation project(path: ':testing-espresso-base')` dependency
    - please override this method in your test class annotate it with @Test
    - please invoke `migrateAll()` inside of it
    - at the end, please make sure marathon invokes your test
     */
    abstract fun checkDatabaseIntegrity()

    protected fun migrateAll() {
        initializeDatabaseWith(1) {}

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            context(),
            databaseClass.java,
            databaseName
        ).addMigrations(
            *migrationsFrom1ToSchemaVersion
        ).build().apply {
            getOpenHelper().getWritableDatabase()
            close()
        }
    }
}
