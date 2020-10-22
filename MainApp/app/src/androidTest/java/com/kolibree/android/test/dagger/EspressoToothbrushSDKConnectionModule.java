/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.sdk.core.InternalKLTBConnectionPoolManager;
import com.kolibree.android.sdk.core.KLTBConnectionPool;
import com.kolibree.android.sdk.dagger.ToothbrushSDKScope;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
abstract class EspressoToothbrushSDKConnectionModule {
  @Provides
  @ToothbrushSDKScope
  static InternalKLTBConnectionPoolManager providesInternalConnectionPoolManager() {
    return mock(InternalKLTBConnectionPoolManager.class);
  }

  @Binds
  abstract KLTBConnectionPool providesConnectionPoolManager(InternalKLTBConnectionPoolManager impl);
}
