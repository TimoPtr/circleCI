/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.core;

import android.app.Application;
import androidx.annotation.Keep;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.feature.FeatureToggleSetExtKt;
import com.kolibree.android.network.NetworkLogFeature;
import com.kolibree.android.network.retrofit.DeviceParameters;
import com.kolibree.android.unity.di.DaggerUnityAndroidSdkComponent;
import com.kolibree.android.unity.di.UnityAndroidSdkComponent;
import com.kolibree.sdkws.core.OnUserLoggedInCallback;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.Set;
import javax.inject.Inject;

@Keep
public abstract class UnityAndroidSdkApplication extends Application implements HasAndroidInjector {
  private UnityAndroidSdkComponent sdkComponent;

  protected UnityAndroidSdkComponent sdkComponent() {
    return sdkComponent;
  }

  @Inject Consumer<Throwable> errorHandler;

  @Inject Set<FeatureToggle<?>> featureToggles;

  @Inject DispatchingAndroidInjector<Object> androidInjector;

  @Override
  public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    sdkComponent =
        DaggerUnityAndroidSdkComponent.builder()
            .onUserLoggedInCallback(provideUserLoggedInCallback())
            .credentials(credentials())
            .deviceParameters(deviceParameters())
            .context(this)
            .build();
    sdkComponent.inject(this);
    FeatureToggleSetExtKt.toggleForFeature(featureToggles, NetworkLogFeature.INSTANCE)
        .setValue(enableNetworkLogs());
    RxJavaPlugins.setErrorHandler(errorHandler);
  }

  private DeviceParameters deviceParameters() {
    return DeviceParameters.Companion.create(versionName(), versionCode());
  }

  protected abstract UnityCredentials credentials();

  protected abstract String versionName();

  protected abstract int versionCode();

  protected abstract OnUserLoggedInCallback provideUserLoggedInCallback();

  protected abstract boolean enableNetworkLogs();
}
