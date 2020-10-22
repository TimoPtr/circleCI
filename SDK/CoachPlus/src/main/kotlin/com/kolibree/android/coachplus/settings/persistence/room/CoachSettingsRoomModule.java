package com.kolibree.android.coachplus.settings.persistence.room;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.coachplus.settings.persistence.dao.CoachSettingsDao;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class CoachSettingsRoomModule {

  // name use for the Room DB uses for the charts only
  private static final String ROOM_DB_NAME = "kolibree-room-coach-settings.db";

  @Provides
  @AppScope
  static CoachSettingsRoomAppDatabase provideTutorialDatabase(Context context) {
    return Room.databaseBuilder(context, CoachSettingsRoomAppDatabase.class, ROOM_DB_NAME).build();
  }

  @Provides
  static CoachSettingsDao providesCoachSettingsDao(CoachSettingsRoomAppDatabase appDatabase) {
    return appDatabase.coachSettingsDao();
  }
}
