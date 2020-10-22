/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.persistence.GamesRoomDatabase
import com.kolibree.android.test.BaseInstrumentationTest
import java.util.UUID
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameProgressDaoTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var db: GamesRoomDatabase

    override fun setUp() {
        super.setUp()
        db = Room.inMemoryDatabaseBuilder(context(), GamesRoomDatabase::class.java).build()
    }

    override fun tearDown() {
        super.tearDown()
        db.close()
    }

    @Test
    fun replaceEntities_truncate_and_insert() {
        val dao = db.gameProgressDao()
        val profileId = 1L
        val profileId2 = 2L
        val gameId1 = "hello"
        val gameId2 = "wolrd"
        val uuid = UUID.randomUUID()
        val gameProgressToPreserve = GameProgressEntity(profileId2, gameId2, "Keep me safe", TrustedClock.getNowZonedDateTimeUTC(), uuid)
        val oldEntities = listOf(
            GameProgressEntity(profileId, gameId1, "", TrustedClock.getNowZonedDateTimeUTC(), uuid),
            GameProgressEntity(profileId, gameId2, "", TrustedClock.getNowZonedDateTimeUTC(), uuid),
            gameProgressToPreserve
        )
        val newProgress = "hell of a progress"
        val newEntity = GameProgressEntity(profileId, gameId1, newProgress, TrustedClock.getNowZonedDateTimeUTC(), uuid)

        dao.insertEntity(oldEntities)

        dao.replaceEntities(profileId, listOf(newEntity))

        val progressProfile2 = dao.getGameProgressEntitiesForProfile(profileId2)

        assertEquals(1, progressProfile2.size)
        assertEquals(gameProgressToPreserve.progress, progressProfile2.first().progress)

        val progressProfile1 = dao.getGameProgressEntitiesForProfile(profileId)

        assertEquals(1, progressProfile1.size)
        assertEquals(newEntity.progress, progressProfile1.first().progress)
    }
}
