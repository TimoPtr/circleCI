/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.android.game.gameprogress.data.api.GameProgressApi;
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao;
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository;
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepositoryImpl;
import com.kolibree.android.game.persistence.GamesRoomDatabase;
import com.kolibree.android.game.shorttask.data.api.ShortTaskApi;
import com.kolibree.android.game.shorttask.data.persistence.ShortTaskDao;
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepository;
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoGameModule {

  @Provides
  @AppScope
  static GamesRoomDatabase providesGamesDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, GamesRoomDatabase.class)
        .allowMainThreadQueries()
        .build();
  }

  // region GameProgress

  @Provides
  static GameProgressApi provideGameProgressApi() {
    return mock(GameProgressApi.class);
  }

  @Provides
  static GameProgressDao provideGameProgressDao(GamesRoomDatabase gamesRoomDatabase) {
    return gamesRoomDatabase.gameProgressDao();
  }

  @Binds
  @IntoSet
  abstract Truncable bindsGameProgressTruncable(GameProgressDao dao);

  @Binds
  abstract GameProgressRepository bindsGameProgressRepository(GameProgressRepositoryImpl impl);

  // endregion

  // region ShortTask

  @Provides
  static ShortTaskApi providesShortTaskApi() {
    return mock(ShortTaskApi.class);
  }

  @Provides
  static ShortTaskDao providesShortTaskDao(GamesRoomDatabase database) {
    return database.shortTaskDao();
  }

  @Binds
  abstract Truncable bindsShortTaskTruncable(ShortTaskDao dao);

  @Binds
  abstract ShortTaskRepository bindsShortTaskRepository(ShortTaskRepositoryImpl impl);

  // endregion

}
