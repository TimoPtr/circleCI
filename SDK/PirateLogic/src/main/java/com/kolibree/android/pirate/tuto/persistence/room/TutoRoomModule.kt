package com.kolibree.android.pirate.tuto.persistence.room

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.pirate.tuto.persistence.dao.TutorialDao
import com.kolibree.android.pirate.tuto.persistence.room.TutoRoomAppDatabase.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides

@Module
object TutoRoomModule {

    @Provides
    @AppScope
    internal fun provideTutorialDatabase(context: Context): TutoRoomAppDatabase {
        return Room.databaseBuilder(
            context,
            TutoRoomAppDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    internal fun providesStatDao(appDatabase: TutoRoomAppDatabase): TutorialDao = appDatabase.tutorialDao()
}
