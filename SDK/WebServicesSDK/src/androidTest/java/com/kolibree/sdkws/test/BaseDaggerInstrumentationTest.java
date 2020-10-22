/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.kolibree.android.test.BaseInstrumentationTest;
import com.kolibree.sdkws.test.dagger.WebServicesSDKEspressoComponent;
import org.junit.Before;
import org.junit.runner.RunWith;

/** Created by miguelaragues on 14/3/18. */
@RunWith(AndroidJUnit4.class)
public abstract class BaseDaggerInstrumentationTest extends BaseInstrumentationTest {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    ApiSDKTestApp.apiSDKIntegrationcomponent = ApiSDKDaggerInitializer.initialize(context());

    ApiSDKTestApp.apiSDKIntegrationcomponent.inject(
        (ApiSDKTestApp) context().getApplicationContext());
  }

  @NonNull
  @Override
  protected final Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  protected WebServicesSDKEspressoComponent component() {
    return ApiSDKTestApp.apiSDKIntegrationcomponent;
  }
}
