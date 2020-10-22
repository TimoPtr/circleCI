/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.appdata.AppDataManager;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 12/3/18. */
@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoCoreModule {

  @Provides
  @AppScope
  static InternalKolibreeConnector providesKolibreeConnector() {
    return mock(InternalKolibreeConnector.class);
  }

  @Provides
  @AppScope
  static AppDataManager provideAppDataManager() {
    return mock(AppDataManager.class);
  }

  @Binds
  abstract IKolibreeConnector bindsKolibreeConnector(InternalKolibreeConnector kolibreeConnector);

  @Binds
  abstract SynchronizationScheduler bindsSynchronizationScheduler(
      SynchronizationSchedulerImpl impl);
}
