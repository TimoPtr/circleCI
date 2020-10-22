/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk;

import com.kolibree.android.sdk.dagger.SdkComponent;

/** Created by miguelaragues on 5/12/17. */
@SuppressWarnings("KotlinInternalInJava")
public abstract class TestSdkDaggerWrapper {

  private TestSdkDaggerWrapper() {}

  public static void setSdkComponent(SdkComponent sdkComponent) {
    KolibreeAndroidSdk.setSdkComponent(sdkComponent);
  }
}
