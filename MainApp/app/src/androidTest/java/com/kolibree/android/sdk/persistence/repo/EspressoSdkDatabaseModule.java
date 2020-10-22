/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.persistence.repo;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.sdk.persistence.room.AccountToothbrushDao;
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomAppDatabase;
import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/** Created by miguelaragues on 28/9/17. */
@SuppressWarnings("KotlinInternalInJava")
@Module(includes = {ToothbrushSDKRepositoryModule.class})
public class EspressoSdkDatabaseModule {

  private ToothbrushSDKRoomAppDatabase appDatabase;

  @Provides
  static AccountToothbrushDao providesAccountToothbrushDao(
      ToothbrushSDKRoomAppDatabase appDatabase) {
    return appDatabase.accountToothbrushDao();
  }

  @Provides
  ToothbrushSDKRoomAppDatabase providesToothbrushSDKRoomAppDatabase(Context context) {
    if (appDatabase == null) {
      synchronized (this) {
        if (appDatabase == null) {
          Timber.d("Creating PairingRoomAppDatabase");
          appDatabase =
              Room.inMemoryDatabaseBuilder(context, ToothbrushSDKRoomAppDatabase.class)
                  .allowMainThreadQueries()
                  .fallbackToDestructiveMigration()
                  .build();
        }
      }
    }

    return appDatabase;
  }
}
