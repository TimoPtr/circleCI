package com.kolibree.android.test.dagger;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.dagger.EspresssoInOffBrushingsCountModule;
import com.kolibree.charts.di.StatsRepositoryModule;
import com.kolibree.charts.persistence.room.StatsRoomAppDatabase;
import com.kolibree.charts.persistence.room.StatsRoomDaoModule;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module(
    includes = {
      StatsRepositoryModule.class,
      StatsRoomDaoModule.class,
      EspresssoInOffBrushingsCountModule.class
    })
public abstract class EspressoStatsModule {

  @Provides
  @AppScope
  static StatsRoomAppDatabase providesStatDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, StatsRoomAppDatabase.class)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
  }
}
