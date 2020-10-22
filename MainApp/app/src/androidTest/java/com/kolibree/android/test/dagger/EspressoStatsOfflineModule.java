/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.dagger.SingleThreadScheduler;
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.charts.di.StatsRepositoryModule;
import com.kolibree.statsoffline.StatsOfflineFeatureToggleModule;
import com.kolibree.statsoffline.StatsOfflineLocalBrushingsProcessorImpl;
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository;
import com.kolibree.statsoffline.models.api.AggregatedStatsRepositoryImpl;
import com.kolibree.statsoffline.persistence.BrushingSessionStatDao;
import com.kolibree.statsoffline.persistence.StatsOfflineDao;
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.reactivex.Scheduler;

@SuppressWarnings("KotlinInternalInJava")
@Module(includes = {StatsRepositoryModule.class, StatsOfflineFeatureToggleModule.class})
public abstract class EspressoStatsOfflineModule {

  public static boolean useMockRepository = false;

  @AppScope
  @Provides
  static AggregatedStatsRepository providesAggregatedStatsRepository(
      StatsOfflineDao dao, @SingleThreadScheduler Scheduler scheduler) {
    if (useMockRepository) {
      useMockRepository = false;
      return mock(AggregatedStatsRepository.class);
    }
    return new AggregatedStatsRepositoryImpl(dao, scheduler);
  }

  @Binds
  abstract LocalBrushingsProcessor bindsStatsOfflineProcessor(
      StatsOfflineLocalBrushingsProcessorImpl impl);

  @Binds
  @IntoSet
  abstract Truncable bindsTruncableAggregatedStatsRepository(AggregatedStatsRepository impl);

  @Provides
  @AppScope
  static StatsOfflineRoomAppDatabase providesStatsOfflineDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, StatsOfflineRoomAppDatabase.class)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  static BrushingSessionStatDao providesBrushingSessionStatDao(
      StatsOfflineRoomAppDatabase appDatabase) {
    return appDatabase.sessionStatDao();
  }

  @Provides
  static StatsOfflineDao providesStatsOfflineDao(StatsOfflineRoomAppDatabase appDatabase) {
    return appDatabase.statsOfflineDao();
  }
}
