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

import com.kolibree.android.sdk.dagger.ToothbrushSDKScope;
import com.kolibree.android.sdk.plaqless.DspAwaker;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
public class EspressoPlaqlessModule {

  @ToothbrushSDKScope
  @Provides
  static DspAwaker providesDspAwaker() {
    return mock(DspAwaker.class);
  }
}
