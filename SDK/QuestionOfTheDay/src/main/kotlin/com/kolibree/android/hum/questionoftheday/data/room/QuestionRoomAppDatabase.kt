/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.questionoftheday.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.hum.questionoftheday.data.room.QuestionRoomAppDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.questionoftheday.data.room.dao.QuestionDao
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity

@Database(entities = [QuestionEntity::class, AnswerEntity::class], version = DATABASE_VERSION)
internal abstract class QuestionRoomAppDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "kolibree-question-of-the-day.db"
        const val DATABASE_VERSION = 1

        val migrations = arrayOf<Migration>()
    }

    abstract fun questionDao(): QuestionDao
}
