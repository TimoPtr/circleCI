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

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.network.core.AccessTokenManager;
import com.kolibree.android.network.core.AccessTokenManagerImpl;
import com.kolibree.android.network.core.CancelHttpRequestsUseCase;
import com.kolibree.android.network.environment.DefaultEnvironment;
import com.kolibree.android.network.environment.Environment;
import com.kolibree.android.network.errorhandler.NetworkErrorHandler;
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetector;
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetectorImpl;
import com.kolibree.android.network.utils.NetworkChecker;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
abstract class EspressoNetworkModule {

  @Provides
  static Environment providesEnvironment() {
    return Environment.STAGING;
  }

  @Provides
  static DefaultEnvironment providesDefaultEnvironment(Environment environment) {
    return new DefaultEnvironment(environment);
  }

  @Provides
  @AppScope
  static NetworkChecker providesNetworkChecker() {
    return mock(NetworkChecker.class);
  }

  @Provides
  @AppScope
  static CancelHttpRequestsUseCase providesCancelHttpRequestsUseCase() {
    return mock(CancelHttpRequestsUseCase.class);
  }

  @Binds
  abstract NetworkErrorHandler bindsErrorHandler(RemoteAccountDoesNotExistDetectorImpl impl);

  @Binds
  abstract RemoteAccountDoesNotExistDetector bindsRemoteAccountDoesNotExistDetector(
      RemoteAccountDoesNotExistDetectorImpl impl);

  @Binds
  @AppScope
  abstract AccessTokenManager bindsAccessTokenManager(AccessTokenManagerImpl accessTokenManager);
}
