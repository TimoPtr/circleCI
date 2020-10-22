/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.sdkws.utils.ApiSDKUtils;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 16/1/18. */
@Module
public class EspressoApiSdkUtilsModule {

  private final ApiSDKUtils apiSdkUtils;

  public EspressoApiSdkUtilsModule() {
    apiSdkUtils = mock(ApiSDKUtils.class);
  }

  @Provides
  ApiSDKUtils bindsApiSDKUtils() {
    return apiSdkUtils;
  }
}
