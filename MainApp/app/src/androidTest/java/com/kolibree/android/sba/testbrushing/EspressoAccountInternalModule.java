/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.accountinternal.AccountRoomDatabase;
import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProvider;
import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProviderImpl;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.CurrentProfileProviderImpl;
import com.kolibree.android.accountinternal.persistence.dao.AccountDao;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastoreImpl;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastoreImpl;
import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao;
import com.kolibree.android.app.dagger.AppScope;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.JvmStatic;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoAccountInternalModule {

  @Provides
  @AppScope
  @JvmStatic
  static AccountRoomDatabase providesAppDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, AccountRoomDatabase.class)
        .allowMainThreadQueries()
        .build();
  }

  @Provides
  @JvmStatic
  static ProfileDao providesProfileDao(AccountRoomDatabase appDatabase) {
    return appDatabase.profileDao();
  }

  @Provides
  @JvmStatic
  static AccountDao providesAccountDao(AccountRoomDatabase appDatabase) {
    return appDatabase.accountDao();
  }

  @Provides
  static ProfileDatastore providesProfileDatastore(ProfileDao profileDao) {
    return new ProfileDatastoreImpl(profileDao, Schedulers.io());
  }

  @Binds
  abstract AccountDatastore bindsAccountDataStore(AccountDatastoreImpl accountDataStore);

  @Binds
  @AppScope
  abstract CurrentProfileProvider bindsAccountProvider(CurrentProfileProviderImpl accountProvider);

  @Binds
  abstract CurrentAccountAndProfileIdsProvider bindsCurrentAccountAndProfileIdsProvider(
      CurrentAccountAndProfileIdsProviderImpl provider);
}
