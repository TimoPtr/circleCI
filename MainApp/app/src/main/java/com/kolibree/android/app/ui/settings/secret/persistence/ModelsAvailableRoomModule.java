package com.kolibree.android.app.ui.settings.secret.persistence;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ModelsAvailableRoomModule {

  // name use for the Room DB uses for the models available only
  private static final String ROOM_DB_NAME = "kolibree-room-model-available.db";

  @Provides
  @AppScope
  static ModelsAvailableAppDatabase providesStatDatabase(Context context) {
    return Room.databaseBuilder(context, ModelsAvailableAppDatabase.class, ROOM_DB_NAME).build();
  }

  @Provides
  static ModelsAvailableDao providesStatDao(ModelsAvailableAppDatabase appDatabase) {
    return appDatabase.modelsAvailableDao();
  }
}
