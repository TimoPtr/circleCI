package com.kolibree.android.sdk;

import com.kolibree.android.sdk.dagger.SdkComponent;

/** Helper class that gives us access to setSdkComponent Created by miguelaragues on 29/9/17. */
public final class EspressoSDKDaggerWrapper {

  private EspressoSDKDaggerWrapper() {}

  public static void setSdkComponent(SdkComponent newSdkComponent) {
    KolibreeAndroidSdk.setSdkComponent(newSdkComponent);
  }
}
