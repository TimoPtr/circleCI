package com.kolibree.sdkws.room

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.sdkws.brushing.persistence.dao.BrushingDao
import com.kolibree.sdkws.data.model.gopirate.GoPirateDao
import com.kolibree.sdkws.internal.OfflineUpdateDao
import com.kolibree.sdkws.room.ApiRoomDatabase.Companion.migrations
import dagger.Module
import dagger.Provides

@Module
internal object ApiRoomModule {
    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesAppDatabase(context: Context): ApiRoomDatabase =
        Room.databaseBuilder(
            context,
            ApiRoomDatabase::class.java,
            ApiRoomDatabase.DATABASE_NAME
        )
            .addMigrations(*migrations)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providesBrushingDao(appDatabase: ApiRoomDatabase): BrushingDao = appDatabase.brushingDao()

    @Provides
    fun providesOfflineUpdateDao(appDatabase: ApiRoomDatabase): OfflineUpdateDao =
        appDatabase.offlineUpdateDao()

    @Provides
    fun providesGoPirateDao(appDatabase: ApiRoomDatabase): GoPirateDao = appDatabase.goPirateDao()

    @Provides
    fun providesSupportSQLiteOpenHelper(appDatabase: ApiRoomDatabase): SupportSQLiteOpenHelper =
        appDatabase.openHelper
}
