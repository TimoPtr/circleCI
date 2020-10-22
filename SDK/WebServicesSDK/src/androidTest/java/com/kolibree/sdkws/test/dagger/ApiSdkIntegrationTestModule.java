/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 14/3/18. */
@Module
class ApiSdkIntegrationTestModule {

  @Provides
  @AppScope
  static AvroFileUploader providesAvroFileUploader() {
    return mock(AvroFileUploader.class);
  }
}
