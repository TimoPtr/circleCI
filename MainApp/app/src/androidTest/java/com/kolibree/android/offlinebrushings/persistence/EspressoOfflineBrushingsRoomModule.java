/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence;

import android.content.Context;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.kolibree.android.app.dagger.AppScope;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoOfflineBrushingsRoomModule {

  @Provides
  @AppScope
  static OfflineBrushingsRoomDatabase providesOfflineBrushingDb(Context context) {
    return Room.inMemoryDatabaseBuilder(context, OfflineBrushingsRoomDatabase.class)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  static SupportSQLiteOpenHelper providesSupportSqliteOpenHelper(OfflineBrushingsRoomDatabase db) {
    return db.getOpenHelper();
  }

  @Provides
  static OrphanBrushingDao providesOrphanBrushingDao(OfflineBrushingsRoomDatabase db) {
    return db.orphanBrushingDao();
  }
}
