package com.kolibree.sdkws.appdata.persistence

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Room
import dagger.Module
import dagger.Provides

/**
 * App data persistence module
 *
 * Provides [AppDataDao] internally
 */
@Module
internal class AppDataPersistenceModule {

    // App data dedicated database
    @Keep
    private companion object {
        const val ROOM_DB_NAME = "kolibree-room-app-data.db"
    }

    @Provides
    fun provideAppDataDao(context: Context): AppDataDao {
        return Room.databaseBuilder(context, AppDataRoomDatabase::class.java, ROOM_DB_NAME)
            .fallbackToDestructiveMigration()
            .build().appDataDao()
    }
}
