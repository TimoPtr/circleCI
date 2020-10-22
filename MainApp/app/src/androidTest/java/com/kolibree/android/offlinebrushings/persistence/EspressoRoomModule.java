package com.kolibree.android.offlinebrushings.persistence;

import android.content.Context;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.commons.interfaces.Truncable;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public abstract class EspressoRoomModule {

  @Provides
  @AppScope
  static OfflineBrushingsRoomDatabase providesAppDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, OfflineBrushingsRoomDatabase.class)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  static SupportSQLiteOpenHelper provideSupportSqliteOpenHelper(
      OfflineBrushingsRoomDatabase appDatabase) {
    return appDatabase.getOpenHelper();
  }

  @Provides
  static OrphanBrushingDao providesOrphanBrushingDao(OfflineBrushingsRoomDatabase appDatabase) {
    return appDatabase.orphanBrushingDao();
  }

  @Binds
  @IntoSet
  abstract Truncable bindsOrphanBrushingRepositoryTruncable(OrphanBrushingRepository repository);
}
