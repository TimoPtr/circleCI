/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.core.AvatarCache;
import com.kolibree.sdkws.core.EspressoCoreModule;
import com.kolibree.sdkws.core.NoOpAvatarCache;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import com.kolibree.sdkws.di.ApiSdkBindingModule;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 16/1/18. */
@Module(
    includes = {
      EspressoApiSdkUtilsModule.class,
      EspressoCoreModule.class,
      ApiSdkBindingModule.class,
      EspressoWSRepositoriesModule.class,
      EspressoNetworkModule.class
    })
public abstract class EspressoWebServicesSdkModule {

  @Provides
  @AppScope
  static AvroFileUploader providesAvroFileUploader() {
    return mock(AvroFileUploader.class);
  }

  @Provides
  static AvatarCache bindsAvatarCache() {
    return NoOpAvatarCache.INSTANCE;
  }
}
