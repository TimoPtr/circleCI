/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test;

import android.app.Application;
import com.kolibree.sdkws.test.dagger.WebServicesSDKEspressoComponent;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import javax.inject.Inject;

/** Created by miguelaragues on 15/3/18. */
public class ApiSDKTestApp extends Application implements HasAndroidInjector {

  public static WebServicesSDKEspressoComponent apiSDKIntegrationcomponent;
  @Inject DispatchingAndroidInjector<Object> dispatchingServiceAndroidInjector;

  @Override
  public AndroidInjector<Object> androidInjector() {
    return dispatchingServiceAndroidInjector;
  }
}
