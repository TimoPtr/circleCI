/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.di;

import android.content.Context;
import androidx.annotation.Keep;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.network.retrofit.DeviceParameters;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.unity.core.UnityAndroidSdkApplication;
import com.kolibree.android.unity.core.UnityCredentials;
import com.kolibree.sdkws.appdata.AppDataManager;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.core.OnUserLoggedInCallback;
import com.kolibree.sdkws.core.SynchronizationScheduler;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@AppScope
@Component(modules = {AndroidInjectionModule.class, UnityAndroidSdkModule.class})
@Keep
public interface UnityAndroidSdkComponent {

  Context context();

  IKolibreeConnector kolibreeConnector();

  SynchronizationScheduler syncScheduler();

  CheckupCalculator checkupCalculator();

  AppDataManager appDataManager();

  void inject(UnityAndroidSdkApplication application);

  @Keep
  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder context(Context context);

    @BindsInstance
    Builder onUserLoggedInCallback(OnUserLoggedInCallback callback);

    @BindsInstance
    Builder credentials(UnityCredentials credentials);

    @BindsInstance
    Builder deviceParameters(DeviceParameters parameters);

    UnityAndroidSdkComponent build();
  }
}
