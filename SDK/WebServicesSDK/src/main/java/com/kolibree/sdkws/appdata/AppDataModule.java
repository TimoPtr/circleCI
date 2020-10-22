/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata;

import com.kolibree.sdkws.appdata.persistence.AppDataPersistenceModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * App data module
 *
 * <p>Provides the [AppDataManager] class
 */
@Module(includes = AppDataPersistenceModule.class)
public abstract class AppDataModule {

  @Provides
  static AppDataApi provideAppDataApi(Retrofit retrofit) {
    return retrofit.create(AppDataApi.class);
  }

  @Binds
  abstract AppDataManager provideAppDataManager(AppDataManagerImpl impl);
}
