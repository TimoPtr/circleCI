/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.annotation.VisibleForApp
import kotlin.reflect.KClass
import org.junit.Rule

@VisibleForApp
abstract class BaseRoomMigrationTest<DB : RoomDatabase>(
    databaseClass: KClass<DB>,
    private val databaseName: String
) : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @JvmField
    @Rule
    val testHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        databaseClass.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    protected fun initializeDatabaseWith(
        schemaVersion: Int,
        execute: SupportSQLiteDatabase.() -> Unit
    ) {
        val db = testHelper.createDatabase(
            databaseName,
            schemaVersion
        )

        execute(db)
        db.close()
    }

    protected fun runMigrationAndCheck(
        vararg migrations: Migration,
        check: SupportSQLiteDatabase.() -> Unit
    ) {
        testHelper.runMigrationsAndValidate(
            databaseName,
            migrations.last().endVersion,
            true,
            *migrations
        ).check()
    }
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun Boolean.sqlBool(): Int = if (this /* == true */) 1 else 0

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun SupportSQLiteDatabase.queryExistingTable(tableName: String, execute: (Cursor) -> Unit) =
    query("SELECT * FROM $tableName")?.let { execute(it) }

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun SupportSQLiteDatabase.tableExists(tableName: String): Boolean = try {
    query(
        "SELECT DISTINCT tbl_name FROM sqlite_master WHERE type='table' AND tbl_name = '$tableName'"
    )?.let { it.count > 0 } ?: false
} catch (e: SQLiteException) {
    false
}
